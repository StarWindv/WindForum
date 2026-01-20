//DEPS io.javalin:javalin:6.7.0
//DEPS org.slf4j:slf4j-simple:2.0.7
//DEPS org.xerial:sqlite-jdbc:3.51.1.0
//DEPS org.apache.commons:commons-lang3:3.20.0

//SOURCES Utils/FStyles.java
//SOURCES Utils/Values.java
//SOURCES Utils/Users.java
//SOURCES Utils/SQLite.java
package top.starwindv;


import java.net.InetAddress;
import java.net.NetworkInterface;

import java.util.*;
import java.text.SimpleDateFormat;
import java.nio.file.Paths;

import io.javalin.util.JavalinBindException;
import io.javalin.Javalin;

import picocli.CommandLine;

import top.starwindv.Utils.*;
import top.starwindv.Tools.*;
import top.starwindv.DTO.GsonMapper;
import top.starwindv.Tools.ArgParser;


class BaseServer {
    private final SimpleDateFormat Formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final FStyles Color = new FStyles();
    private Javalin server;
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
        String jbangCwd = System.getenv("JBANG_CWD");

        String projectRoot = jbangCwd != null ? jbangCwd : System.getProperty("user.dir");

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
                    Color.BCyan,
                    "->",
                    Color.Reset,
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
                    Color.BYellow,
                    "<-",
                    Color.Reset,
                    this.Formatter.format(new java.util.Date()),
                    code < 300 ? Color.Green : (code < 400 ? Color.BYellow : Color.Red),
                    Color.Bold,
                    ctx.status().getCode(),
                    Color.Reset,
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
            // 遍历所有的网络接口
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration<InetAddress> inetAddress = iface.getInetAddresses(); inetAddress.hasMoreElements();) {
                    InetAddress inetAddr = inetAddress.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
//                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
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
            this.Color.Bold,
            this.Color.frontFrom(159, 43, 104),
            this.Color.Reset,
            this.Color.frontFrom(255, 246, 75),
            code < 500 ? this.page4xx : this.page5xx,
            this.Color.Reset
        );
    }
}


class Services {
    private final BaseServer server = new BaseServer();
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
