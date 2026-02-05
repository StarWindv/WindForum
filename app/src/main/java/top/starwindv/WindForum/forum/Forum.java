package top.starwindv.WindForum.forum;


import io.javalin.Javalin;

import top.starwindv.WindForum.forum.Backend.Authorizer;

import top.starwindv.WindForum.forum.Backend.SessionController;
import top.starwindv.WindForum.forum.DTO.GetPostsDTO;
import top.starwindv.WindForum.forum.DTO.PostDTO;
import top.starwindv.WindForum.forum.DTO.UserDTO;
import top.starwindv.WindForum.forum.Models.Channel;
import top.starwindv.WindForum.forum.Models.Posts;
import top.starwindv.WindForum.forum.Models.Users;
import top.starwindv.WindForum.forum.Tools.Sources;
import top.starwindv.WindForum.forum.Utils.Values;
import top.starwindv.WindForum.logger.WindLogger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


class protectAPI {
    public final static Set<String> path = Set.of(
        "/api/posts/upload",
        "/api/posts/comments",
        "/editor",
        "/api/userinfo"
    );
    public static boolean isProtectedPath(String requestFor) {
        for (String pattern : path) {
            if (pattern.endsWith("/*")) {
                String prefix = pattern.substring(0, pattern.length() - 2);
                if (requestFor.startsWith(prefix)) { return true; }
            } else if (requestFor.equals(pattern)) { return true; }
        } return false;
    }
}


public class Forum {
    public final String AppName;
    public final Javalin server;
    public final Authorizer authorizer;
    public final Sources Src;

    private final Path dbDir = Paths.get(".", "Data");
    private final String dataDBName = dbDir.resolve("WindForum.db").toString();
    private Users UsersTool;
    private Posts PostsTool;
    private Channel ChannelTool;
    private SessionController SessionOperator;
    public final Email Poster;
    protected static WindLogger Logger;

    public Forum(String AppName, Javalin instance, Sources Src, WindLogger logger) {
        this.AppName = AppName;
        this.server = instance;
        this.Src = Src;
        Logger = logger;
        this.init();
        try {
            this.Poster = new Email(Src.template("email/mail.html"), "WindForum");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.authorizer = new Authorizer(
            this.UsersTool,
            6,
            this.Poster,
            this.SessionOperator
        );
    }

    public static WindLogger Logger() { return Logger; }

    private void registerTools() {
        UsersTool = new Users(dataDBName);
        PostsTool = new Posts(dataDBName);
        ChannelTool = new Channel(dataDBName);
        SessionOperator = new SessionController(dataDBName);
    }

    private void init() {
        this.registerTools();
        this.sessionRoute();
        this.staticFile();
        this.loginMethodGroup();
        this.userUploadMethodGroup();
        this.obtainInfoMethodGroup();
        this.viewPage();
    }

    private void loginMethodGroup() {
        this.server.post(
            "/api/register",
            ctx -> ctx.async(
                () -> {
                    UserDTO RegisterInfo = ctx.bodyAsClass(UserDTO.class);
                    Logger.debug(UserDTO.viewer.Stringify(RegisterInfo));
                    Values result = this.authorizer.sendCode(
                        RegisterInfo.username(),
                        RegisterInfo.email()
                    );
                    if (!(boolean) result.get(0)) {
                        ctx.status(400);
                    }
                    Logger.debug(result);
                }
            )
        );

        this.server.post(
            "/api/verify",
            ctx -> {
                UserDTO RegisterInfo = ctx.bodyAsClass(UserDTO.class);
                Values result = this.authorizer.verifyCodeAfterRegister(
                    RegisterInfo.email(),
                    RegisterInfo.username(),
                    RegisterInfo.verifyCode(),
                    RegisterInfo.codeHash(),
                    ctx.attribute("IP")
                );
                Logger.debug(result);
                if (!(boolean) result.get(0)) {
                    ctx.status(400);
                }
            }
        );

        this.server.post(
            "/api/login",
            ctx ->  {
                UserDTO LoginInfo = ctx.bodyAsClass(UserDTO.class);
//                Logger.debug(LoginInfo.viewer.toString(LoginInfo));
                Values result = this.authorizer.login(
                    LoginInfo.email(),
                    LoginInfo.codeHash(),
                    ctx.attribute("IP")
                );
//                Logger.debug(result);
                Map<String, String> response = new HashMap<>();
                if ((boolean) result.get(0)) {
                    String session_id = (String) result.get(2);
                    Logger.debug("User      : " + LoginInfo.email() + "\nSession-ID: " + session_id);
                    response.put("Session-ID", session_id);
                    response.put("status", String.valueOf(true));
                    response.put("message", """
                        Please add the complete "Session-ID" field in the Header of subsequent requests, which will serve as your identity identifier.
                        """);
                } else {
                    response.put("Session-ID", null);
                    response.put("status", String.valueOf(false));
                    response.put("message", """
                        Please check if your email address and verification code are correct.
                        """);
                    ctx.status(401);
                }
                ctx.contentType("application/json");
                ctx.json(response);
                /* result: (0, 1, 2)
                 * 0: boolean
                 * 1: message
                 * 2: session_id 当且仅当 0 为真时存在
                 * */
            }
        );

        this.server.post(
            "/api/SessionStatus",
            ctx -> {
                String session_id = ctx.header("Session-ID");
                if (session_id == null || session_id.isEmpty()) {
                    ctx.status(400);
                }
                Values result = this.SessionOperator.loggedInBySessionID(session_id);
                 Logger.debug("Session Route: " + result);
                if (result.getStatus()) {
                    ctx.status(200);
                    return;
                } ctx.status(401);

            }
        );
    }

    private void userUploadMethodGroup() {
        this.server.post(
            "/api/posts/upload",
            ctx -> {
                PostDTO PostInfo = ctx.bodyAsClass(PostDTO.class);
                Map<String, Object> result = new HashMap<>();
                if (!PostInfo.isEmpty()) {
                    Values postResult = this.PostsTool.addPost(PostInfo);
                    if (!(boolean) postResult.get(0)) {
                        result.put("status", false);
                        result.put("message", """
                                Server Error: Failed When Add Post
                                """);
                        ctx.status(400);
                    } else {
                        result.put("status", true);
                        result.put("message", "Success");
                    }
                    Logger.debug(postResult);
                } else {
                    result.put("status", false);
                    result.put("message", "Please check your data and ensure that is not empty");
                    ctx.status(400);
                }
                ctx.json(result);
            }
        );

        this.server.post(
            "/api/posts/comments",
            ctx -> {
                // TODO
            }
        );
    }

    private void viewPage() {
        this.server.get(
            "/index", ctx -> ctx.html(Src.template("index.html"))
        );

        this.server.get(
            "/dashboard",
            ctx -> ctx.html(Src.template("dashboard.html"))
        );

        this.server.get(
            "/user/{user_email}",
            ctx -> ctx.html(Src.template("user_show.html"))
        );

        this.server.get(
            "/login",
            ctx -> ctx.html(Src.template("login.html"))
        );
    }

    private void obtainInfoMethodGroup() {
        this.server.post(
            "/api/posts/get",
            ctx -> ctx.async(
                () -> {
                    Values response;
                    try {
                        GetPostsDTO needInfo = ctx.bodyAsClass(GetPostsDTO.class);

                        boolean hasFromTo = (needInfo.from() >= 0 && needInfo.to() >= 0);
                        boolean hasLimit = (needInfo.limit() > 0);
                        boolean hasPostId = (needInfo.post_id() != null && !needInfo.post_id().isEmpty());
                        boolean hasOnlyFrom = (needInfo.from() >= 0 && needInfo.to() < 0);
                        boolean hasOnlyTo = (needInfo.from() < 0 && needInfo.to() >= 0);

                        if (hasOnlyFrom || hasOnlyTo) {
                            ctx.status(400);
                            return;
                        }

                        int paramTypeCount = 0;
                        if (hasFromTo) paramTypeCount++;
                        if (hasLimit) paramTypeCount++;
                        if (hasPostId) paramTypeCount++;

                        if (paramTypeCount > 1) {
                            ctx.status(400);
                            return;
                        }
                        Logger.time();
                        if (hasFromTo) {
                            Values posts = PostsTool.getFromTo(
                                "create_time",
                                needInfo.isArc(),
                                needInfo.from(),
                                needInfo.to()
                            );
                            if (!(boolean) posts.getStatus()) {
                                ctx.status(500);
                                return;
                            }
                            response = Values.from(true, "", posts.getResult());
                        } else if (hasLimit) {
                            response = PostsTool.getAllPosts(needInfo.isArc(), needInfo.limit());
                        } else if (hasPostId) {
                            response = PostsTool.getPost(needInfo.post_id());
                        } else {
                            ctx.status(400);
                            return;
                        }
                        Logger.time();
                        if (!(boolean) response.getStatus()) {
                            ctx.status(404);
                        } else {
                            ctx.json(response);
                        }
                    } catch (Exception e) {
                        ctx.status(500);
                        Logger.trace(e);
                    }
                }
            )
        );

        this.server.post(
            "/api/posts/getUserPosts",
            ctx -> {
                try {
                    PostDTO postInfo = ctx.bodyAsClass(PostDTO.class);
                    Values result = this.PostsTool.AllPostOfOneUser(postInfo.userEmail());
                    if (!(boolean) result.getStatus()) {
                        ctx.status(404);
                        return;
                    }
                    Logger.debug(result);
                    Logger.debug(result.getStatus());
                    ctx.json(result.getResult());
                } catch (Exception e) {
                    ctx.status(500);
                    Logger.trace(e);
                }
            }
        );

        this.server.post(
            "/api/userinfo",
            ctx -> {
                UserDTO user_identity = ctx.bodyAsClass(UserDTO.class);
                String useful_info;
                if (user_identity.username() != null) {
                    useful_info = user_identity.username();
                } else if (user_identity.email()!=null) {
                    useful_info = user_identity.email();
                } else {
                    ctx.status(400);
                    return;
                }
                Values result = this.UsersTool.getUserInfo(useful_info);
                if (result.getStatus()) {
                    ctx.json(result.getResult());
                } else if ((boolean) result.getInnerStatus()) {
                    ctx.status(404);
                } else if (!(boolean) result.getInnerStatus()) {
                    ctx.status(500);
                }
            }
        );

        this.server.get(
            "/api/test",
            ctx -> ctx.json(Values.onlyResult(true))
        );

        this.server.get(
            "/api/channel",
            ctx -> ctx.async(
                () -> {
                    Object result = ChannelTool.getChannel().getResult();
                    Logger.debug(result);
                    ctx.json(result);
                }
            )
        );

    } // obtainArticleMethodGroup

    private void staticFile() {
        this.server.get(
            "favicon.ico",
            ctx -> {
                ctx.contentType("image/png");
                ctx.result(this.Src.staticMedia("image/head.png"));
            }
        );

        this.server.get(
            "robots.txt",
            ctx -> {
                ctx.contentType("text/plain");
                ctx.result(this.Src.staticFile("text/robots.txt"));
            }
        );

        this.server.get(
            "/.well-known/appspecific/com.chrome.devtools.json",
            ctx -> ctx.json("{}")
        );
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void sessionRoute() {
        this.server.before(
            ctx -> ctx.async(
                () -> {
                    String requestFor = ctx.path();
                    if (protectAPI.isProtectedPath(requestFor)) {
                        String session_id = ctx.header("Session-ID");
                        if (session_id == null || session_id.isEmpty()) {
                            ctx.status(401);
                            ctx.redirect("/login");
                        }
                        Values checkResult = this.SessionOperator.loggedInBySessionID(session_id);
                        Logger.debug("loggedInBySessionID: " + checkResult);
                        if (!(boolean) checkResult.getStatus()) {
                            ctx.status(401);
                            ctx.redirect("/login");
                        }
                    } else { /*放行*/ }
                }
            )
        );
    }
}
