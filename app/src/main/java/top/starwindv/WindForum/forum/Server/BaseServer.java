package top.starwindv.WindForum.forum.Server;

import io.javalin.Javalin;
import io.javalin.http.Context;

import top.starwindv.WindForum.forum.DTO.GsonMapper;
import top.starwindv.WindForum.forum.Tools.Sources;
import top.starwindv.WindForum.forum.Utils.ServerPlaceHolder;
import top.starwindv.WindForum.logger.WindLogger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;


@SuppressWarnings("unused")
public class BaseServer {
    private Javalin server;
    public WindLogger Logger = null;

    public Sources Src;

    public static String errorPage = "err/error.html";

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
        for (int status: ServerPlaceHolder.statusCode.keySet()) {
            this.server.error(
                status, ctx -> ctx.async(
                    () -> {
                        int code = ctx.status().getCode();
                        Forum.Logger()._f_middle(ctx);
                        ctx.html(
                            this.Src.template(errorPage)
                                .replace(
                                    ServerPlaceHolder.html_inject, ServerPlaceHolder.msgUpdater(code)
                                )
                        );
                    }
                )
            );
        }
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
