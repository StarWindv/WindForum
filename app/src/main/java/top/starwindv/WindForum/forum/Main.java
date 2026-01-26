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

import java.util.*;
import java.text.SimpleDateFormat;
import java.nio.file.Paths;

import io.javalin.util.JavalinBindException;
import io.javalin.Javalin;

import picocli.CommandLine;

import top.starwindv.WindForum.forum.Tools.Sources;
import top.starwindv.WindForum.forum.DTO.GsonMapper;
import top.starwindv.WindForum.forum.Tools.ArgParser;
import top.starwindv.WindForum.logger.Colorful.Colors;
import top.starwindv.WindForum.logger.WindLogger;


class BaseServer {
    private final SimpleDateFormat Formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Javalin server;
    public final WindLogger logger = new WindLogger(
        cfg -> {

        }
    );

    public Sources Src;

    public String page4xx = "err/4n.html";
    public String page5xx = "err/4n.html";

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
        String msg = """
            ██╗    ██╗██╗███╗   ██╗██████╗     ███████╗ ██████╗ ██████╗ ██╗   ██╗███╗   ███╗
            ██║    ██║██║████╗  ██║██╔══██╗    ██╔════╝██╔═══██╗██╔══██╗██║   ██║████╗ ████║
            ██║ █╗ ██║██║██╔██╗ ██║██║  ██║    █████╗  ██║   ██║██████╔╝██║   ██║██╔████╔██║
            ██║███╗██║██║██║╚██╗██║██║  ██║    ██╔══╝  ██║   ██║██╔══██╗██║   ██║██║╚██╔╝██║
            ╚███╔███╔╝██║██║ ╚████║██████╔╝    ██║     ╚██████╔╝██║  ██║╚██████╔╝██║ ╚═╝ ██║
             ╚══╝╚══╝ ╚═╝╚═╝  ╚═══╝╚═════╝     ╚═╝      ╚═════╝ ╚═╝  ╚═╝ ╚═════╝ ╚═╝     ╚═╝                                                                                                                 \s
        """;
        if (host.equals("0.0.0.0")) {
            msg += String.format(" * Serve on: http://%s:%s\n", "localhost", port);
            msg += String.format(" * Serve on: http:/%s:%s\n", this.getLocalHost(), port);
        } else {
            msg = String.format(" * Serve on: http://%s:%d", host, port);
        }
        this.Src.init(this.server);
        System.out.println(msg);
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
                String realIP = Optional
                    .ofNullable(
                        ctx.header("CF-Connecting-IP")
                    ).orElse(ctx.ip());
                if (
                    realIP.equals(
                        "[0:0:0:0:0:0:0:1]"
                    )
                ) {
                    realIP = "localhost";
                }
                ctx.attribute("IP", realIP);
                String msg = String.format(
                    "%s[%s]%s [%s] [%s] [%s] [%s]\n",
                    Colors.BCyan,
                    "->",
                    Colors.Reset,
                    this.Formatter.format(new java.util.Date()),
                    ctx.method(),
                    realIP,
                    ctx.path()
                );
                System.out.printf(msg);
            }
        );

        this.server.after(
            ctx -> {
                String realIP = Optional
                    .ofNullable(
                        ctx.header("CF-Connecting-IP")
                    ).orElse(ctx.ip());
                if (
                    realIP.equals(
                        "[0:0:0:0:0:0:0:1]"
                    )
                ) {
                    realIP = "localhost";
                }
                int code = ctx.status().getCode();
                String msg = String.format(
                    "%s[%s]%s [%s] [%s%s%d%s] [%s]\n\n",
                    Colors.BYellow,
                    "<-",
                    Colors.Reset,
                    this.Formatter.format(new java.util.Date()),
                    code < 300 ? Colors.Green : (code < 400 ? Colors.BYellow : Colors.Red),
                    Colors.Bold,
                    ctx.status().getCode(),
                    Colors.Reset,
                    realIP
                );
                System.out.printf(msg);
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
}


class Services {
    public final BaseServer server = new BaseServer();
    public void initialize() {
        /*
        * 或许会有更多的需要注册的类
        * 所以有了这个整合接口
        */
        this.initForum();
    }
    private void initForum() {
        new Forum("Wind", this.server.instance(), this.server.Src);
    }
    public void start(String ip, int port) {
        this.server.start(ip, port);
        this.initialize();
    }
}


public class Main {
    private final static Services services = new Services();

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(ArgParser.instance);
        int status = cmd.execute(args);
        if (status!=0) { System.exit(status);}
        services.server.logger.info("TEST");
        try {
            services.start(
                ArgParser.instance.host(),
                Integer.parseInt(ArgParser.instance.port())
            );
        } catch (JavalinBindException e) {
            System.err.println(" x This Port Has Been Bind");
            System.err.println(" x EXIT");
        }
    }
}
