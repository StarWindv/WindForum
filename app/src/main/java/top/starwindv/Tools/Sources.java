package top.starwindv.Tools;


import org.apache.commons.lang3.StringUtils;

import io.javalin.Javalin;

import java.nio.file.Paths;
import java.nio.file.Files;


public class Sources {
    /**
     * we don't need powerful render
     * only sources returner is enough
     */
    public final String srcRoot;
    public final String template;
    public final String staticFile;
    
    public Sources(String srcRoot) {
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

    /**
     * Register a static files routes
     * to return static dependence
     * such as scripts.js
     * or robots.txt
     */
    public void init(Javalin server) {

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