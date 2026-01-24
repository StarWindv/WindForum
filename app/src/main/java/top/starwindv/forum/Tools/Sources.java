package top.starwindv.forum.Tools;


import io.javalin.http.Context;
import org.apache.commons.lang3.StringUtils;

import io.javalin.Javalin;

//import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.Files;


@SuppressWarnings("unused")
public class Sources {
    /**
     * we don't need powerful render
     * only sources returner is enough
     */
    public final String srcRoot;
    public final String template;
    public final String staticFile;

    public static final String[] TextMimeTypes = {
        "application/javascript",
        "application/json",
        "application/xml",
        "application/xhtml+xml"
    };
    
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

    public String staticFile(String filePath, String encoding) throws Exception {
        return (
            Files.readString(Paths.get(this.staticFile, filePath), Charset.forName(encoding))
        );
    }

    public byte[] staticMedia(String filePath) throws Exception {
        return (
            Files.readAllBytes(Paths.get(this.staticFile, filePath))
        );
    }

    private void requestFile(
        String encoding,
        String mimeType,
        String staticPath,
        Context ctx
    ) throws Exception {
        boolean isTextType = mimeType.startsWith("text/");
        if (!isTextType) {
            for (String textMime : TextMimeTypes) {
                if (textMime.equals(mimeType)) {
                    isTextType = true;
                    break;
                }
            }
        }
        ctx.header("X-Content-Type-Options", "nosniff");
//        System.out.println("Request MimeType: " + mimeType);
        if (isTextType) {
            ctx.contentType(mimeType + "; charset=UTF-8")
                .result(this.staticFile(staticPath, encoding));
        } else {
            ctx.header("Content-Encoding", "identity");
            ctx.contentType(mimeType)
                .result(this.staticMedia(staticPath));
        }
    }

    /**
     * Register a static files routes
     * to return static dependence
     * such as scripts.js
     * or robots.txt
     */
    public final void init(Javalin server) {
        server.get(
            "/static/*", ctx -> {
                String staticPath = StringUtils.substringAfter(ctx.path(), "/static/");

                String encoding = "UTF-8";
                try {
                    String mimeType = Files.probeContentType(Paths.get(staticPath));
                    if (mimeType == null) {
                        mimeType = "text/plain";
                    }
                    this.requestFile(
                        encoding, mimeType, staticPath, ctx
                    );

                } catch (NoSuchFileException ignored) {
                    ctx.status(404);
                    System.err.println("No Such File: " + staticPath);
                } catch (MalformedInputException ignored) {
                    ctx.status(501);
                } catch (UnsupportedCharsetException ignored) {
                    ctx.status(400);
                }
            }
        );
    }

}