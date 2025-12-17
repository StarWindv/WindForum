//DEPS io.javalin:javalin:6.7.0
//DEPS org.slf4j:slf4j-simple:2.0.7
//DEPS org.xerial:sqlite-jdbc:3.51.1.0
//DEPS org.apache.commons:commons-lang3:3.20.0

//SOURCES FStyles.java
//SOURCES Values.java
//SOURCES Users.java
//SOURCES SQLite.java
package top.starwindv;


import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.http.ContentType;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.text.SimpleDateFormat;

import java.nio.file.Paths;
import java.nio.file.Files;


class Sources {
    /**
     * we don't need powerful render
     * only sources returner is enough
     */
    public final String srcRoot;
    public final String template;
    public final String staticFile;
    
    Sources(String srcRoot) {
        srcRoot = srcRoot.replace("\\", "/");
        srcRoot = StringUtils.stripEnd(srcRoot, "/");
        this.srcRoot    = srcRoot;
        this.template   = srcRoot + "/templates";
        this.staticFile = srcRoot + "/static";
    }

    public String template(String filePath) throws Exception {
        return (
            Files.readString(Paths.get(this.template, filePath))
        );
    }

    public String staticFile(String filePath) throws Exception {
        return (
            Files.readString(Paths.get(this.staticFile, filePath))
        );
    }

    public byte[] staticMedia(String filePath) throws Exception {
        return (
            Files.readAllBytes(Paths.get(this.staticFile, filePath))
        );
    }

    public void init(Javalin server) {
        /**
         * Register a static files routes
         * to return static dependices
         * such as scripts.js
         * or robots.txt
         */
        server.get(
            "/static/*", ctx -> {
                String staticPath = StringUtils.substringAfter(ctx.path(), "/static/");
                String mimeType = Files.probeContentType(Paths.get(staticPath));
                ctx.contentType(mimeType != null ? mimeType : "text/plain");
                ctx.result(
                    this.staticFile(staticPath)
                );
            }
        );
    }

}


class Server {
    private SimpleDateFormat Formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private FStyles Color = new FStyles();
    private Javalin server;
    private Sources Src;

    public String page4xx = "err/4n.html";
    public String page5xx = "err/4n.html";

    private void init() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
        this.server = Javalin.create(
        config -> {
            config.showJavalinBanner = false;
        }
    );
    }

    public void start(String host, int port) {
        this.init();
        this.server.start(host, port);
        this.registerRoutes();
        this.registerHooks();
        this.registerErrHandlers();
        String msg;
        if (host.equals("0.0.0.0")) {
            msg = String.format(" * Serve on: http://%s:%s", "localhost", port);
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
            "/", ctx -> {
                ctx.html("Server is Running");
            }
        );

        this.server.get(
            "/index", ctx -> {
                ctx.html(Src.template("index.html"));
            }
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

    private String EHMsg(int code) {
        String msg = "%s%s[<>]%s Handled: [%s%s%s]\n";
        return String.format(
            msg, 
            this.Color.Bold,
            this.Color.ffrom(159, 43, 104),
            this.Color.Reset,
            this.Color.ffrom(255, 246, 75),
            code < 500 ? this.page4xx : this.page5xx,
            this.Color.Reset
        );
    }
}


public class Main {
    private static Server server = new Server();
    public static void main(String[] args) {

        server.start("0.0.0.0", 7000);
    }
}
