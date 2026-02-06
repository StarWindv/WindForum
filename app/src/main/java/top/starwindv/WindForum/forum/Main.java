//DEPS io.javalin:javalin:6.7.0
//DEPS org.slf4j:slf4j-simple:2.0.7
//DEPS org.xerial:sqlite-jdbc:3.51.1.0
//DEPS org.apache.commons:commons-lang3:3.20.0

//SOURCES Utils/FStyles.java
//SOURCES Utils/Values.java
//SOURCES Utils/Users.java
//SOURCES Utils/SQLite.java
package top.starwindv.WindForum.forum;


import java.net.InetAddress;
import java.net.NetworkInterface;

import java.nio.file.Path;
import java.util.*;
import java.nio.file.Paths;

import io.javalin.http.Context;
import io.javalin.util.JavalinBindException;
import io.javalin.Javalin;

import picocli.CommandLine;

import top.starwindv.WindForum.forum.Tools.Sources;
import top.starwindv.WindForum.forum.DTO.GsonMapper;
import top.starwindv.WindForum.forum.Tools.ArgParser;
import top.starwindv.WindForum.forum.Utils.ServerPlaceHolder;
import top.starwindv.WindForum.logger.Colorful.Colors;

import top.starwindv.WindForum.logger.WindLogger;


@SuppressWarnings("unused")
class BaseServer {
    private Javalin server;
    public WindLogger Logger = null;

    public Sources Src;

    public String page4xx = "err/4n.html";
    public String page5xx = "err/4n.html";

    private static boolean useLogFeature = false;

    public static void UseLogFeature(boolean us) {
        useLogFeature = us;
    }
    public BaseServer(Runnable func) { func.run(); }
    public BaseServer() {}

    public Javalin instance() { return this.server; }

    private void init() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
        this.server = Javalin.create(
            config -> {
                config.showJavalinBanner = false;
                config.jsonMapper(new GsonMapper());
            }
        );
    }

    @SuppressWarnings("HttpUrlsUsage")
    public void start(String host, int port) {
        this.init();
        this.server.start(host, port);
        this.registerRoutes();
        this.registerHooks();
        this.registerErrHandlers();
        List<String> IPList = new ArrayList<>();
        if (host.equals("0.0.0.0")) {
            IPList.add("http://localhost:"+port);
            IPList.add(String.format("http:/%s:%s", this.getLocalHost(), port));
        } else {
            IPList.add(String.format("http://%s:%d", host, port));
        }
        this.Src.init(this.server);
        this.Logger.title("\n<Yellow>");
        this.Logger.startMsg(IPList);
    }

    private void registerRoutes() {
        String projectRoot = System.getProperty("user.dir");
        String assetsPath = Paths.get(projectRoot, "assets")
                                .toString()
                                .replace("\\", "/");
        this.Src = new Sources(assetsPath);

        this.server.get(
            "/", ctx -> ctx.html("Server is Running")
        );
    }

    private void registerHooks() {
        this.server.before(
            ctx -> {
                String realIP = this.getIP(ctx);
                if (!useLogFeature) {
                    this.Logger.inbound(realIP, ctx.method().toString(), ctx.path());
                } else {
                    this.Logger._f_inbound(ctx);
                }
            }
        );

        this.server.after(
            ctx -> {
                if (!useLogFeature) {
                    int code = ctx.status().getCode();
                    Logger.outbound(code, ctx.attribute("IP"));
                } else {
                    this.Logger._f_outbound(ctx);
                }
            }
        );
    }

    private void registerErrHandlers() {
        this.server.error(
            404, ctx->{
                int code = ctx.status().getCode();
                System.out.printf(this.EHMsg(code));
                ctx.html(this.Src.template(this.page4xx));
            }
        );
    }

    public final InetAddress getLocalHost() {
        try {
            InetAddress candidateAddress = null;
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = ifaces.nextElement();
                for (Enumeration<InetAddress> inetAddress = iface.getInetAddresses(); inetAddress.hasMoreElements();) {
                    InetAddress inetAddr = inetAddress.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                System.err.println("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            System.out.println("Failed to determine LAN address: " + e);
            return null;
        }
    }

    private String EHMsg(int code) {
        String msg = "%s%s[<>]%s Handled: [%s%s%s]\n";
        return String.format(
            msg, 
            Colors.Bold,
            Colors.frontFrom(159, 43, 104),
            Colors.Reset,
            Colors.frontFrom(255, 246, 75),
            code < 500 ? this.page4xx : this.page5xx,
            Colors.Reset
        );
    }

    private String getIP(Context ctx) {
        String realIP = Optional
            .ofNullable(
                ctx.header("CF-Connecting-IP")
            ).orElse(ctx.ip());
        if (realIP.equals("[0:0:0:0:0:0:0:1]")) {
            realIP = "localhost";
        }
        ctx.attribute(ServerPlaceHolder.IP, realIP);
        return realIP;
    }
}


public class Main {
    private final static BaseServer server = new BaseServer();

    private static void start(
        String ip,
        int port,
        boolean useFeature,
        boolean debug,
        boolean time
    ) {
        server.Logger = new WindLogger(
            config -> {
                config.logFilePath(Path.of("Data/log.db"));
                config.useDebug(debug);
                config.useTimeLog(time);
            }
        );
        server.start(ip, port);
        BaseServer.UseLogFeature(useFeature);
        new Forum("Wind", server.instance(), server.Src, server.Logger);
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(ArgParser.instance);
        int status = cmd.execute(args);
        if (status!=0) { System.exit(status);}
        if (ArgParser.instance.help()) { return; }
        try {
            start(
                ArgParser.instance.host(),
                Integer.parseInt(ArgParser.instance.port()),
                ArgParser.instance.useFeature(),
                ArgParser.instance.debug(),
                ArgParser.instance.time()
            );
        } catch (JavalinBindException e) {
            System.err.println(" x This Port Has Been Bind");
            System.err.println(" x EXIT");
        }
    }
}
