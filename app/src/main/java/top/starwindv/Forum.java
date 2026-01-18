package top.starwindv;


import io.javalin.Javalin;

import top.starwindv.Backend.Authorizer;

import top.starwindv.Backend.SessionController;
import top.starwindv.DTO.*;
import top.starwindv.Models.Posts;
import top.starwindv.Models.Users;
import top.starwindv.Tools.Sources;
import top.starwindv.Utils.Values;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


class protectAPI {
    public final static Set<String> path = Set.of(
        "/dashboard",
        "/api/posts/upload",
        "/api/posts/comments",
        "/editor"
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


@SuppressWarnings("CallToPrintStackTrace")
public class Forum {
    public final String AppName;
    public final Javalin server;
    public final Authorizer authorizer;
    public final Sources Src;
    public final Users UsersTool = new Users("Data/WindForum.db");
    public final Posts PostsTool = new Posts("Data/WindForum.db");
    public final SessionController SessionOperator = new SessionController("Data/WindForum.db");
    public final Email Poster;

    public Forum(String AppName, Javalin instance, Sources Src) {
        this.AppName = AppName;
        this.server = instance;
        this.Src = Src;
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
        this.init();
    }

    private void init() {
        this.sessionRoute();
        this.registerMethodGroup();
        this.staticFile();
        this.loginMethodGroup();
        this.userUploadMethodGroup();
        this.obtainInfoMethodGroup();
        this.viewPage();
    }

    private void registerMethodGroup() {
        this.server.post(
            "/api/register", 
            ctx -> {
                UserDTO RegisterInfo = ctx.bodyAsClass(UserDTO.class);
                System.out.println(UserDTO.viewer.Stringify(RegisterInfo));
                Values result = this.authorizer.sendCode(
                    RegisterInfo.username(),
                    RegisterInfo.email()
                );
                if (!(boolean) result.get(0)) {
                    ctx.status(401);
                }
                System.out.println(result);
            }
        );

        this.server.post(
            "/api/verify",
            ctx -> {
                UserDTO RegisterInfo = ctx.bodyAsClass(UserDTO.class);
                System.out.println(
                    this.authorizer.verifyCodeAfterRegister(
                        RegisterInfo.email(),
                        RegisterInfo.username(),
                        RegisterInfo.verifyCode(),
                        RegisterInfo.codeHash(),
                        ctx.attribute("IP")
                    )
                );
            }
        );
    }

    private void loginMethodGroup() {
        this.server.post(
            "/api/login",
            ctx ->  {
                UserDTO LoginInfo = ctx.bodyAsClass(UserDTO.class);
                Values result = this.authorizer.login(
                    LoginInfo.email(),
                    LoginInfo.codeHash(),
                    ctx.attribute("IP")
                );
                if ((boolean) result.get(0)) {
                    String session_id = (String) result.get(2);
                    System.out.println("User      : " + LoginInfo.email() + "\nSession-ID: " + session_id);
                    Map<String, String> response = new HashMap<>();
                    response.put("Session-ID", session_id);
                    response.put("status", String.valueOf(true));
                    response.put("message", """
                        Please add the complete "Session-ID" field in the Header of subsequent requests, which will serve as your identity identifier.
                        """);
                    ctx.json(response);
                    ctx.contentType("text/plain");
                } else {
                    ctx.status(401);
                }
                /* result: (0, 1, 2)
                 * 0: boolean
                 * 1: message
                 * 2: session_id 当且仅当 0 为真时存在
                 * */
            }
        );
    }

    private void userUploadMethodGroup() {
        this.server.post(
            "/api/posts/upload",
            ctx -> {
                PostDTO PostInfo = ctx.bodyAsClass(PostDTO.class);
                if (!PostInfo.isEmpty()) {
                    Values postResult = this.PostsTool.addPost(PostInfo);
                    if (!(boolean) postResult.get(0)) {
                        ctx.status(400);
                    }
                    System.out.println(postResult);
                } else {
                    ctx.status(400);
                }
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
            "/editor",
            ctx -> ctx.html(Src.template("editor.html"))
        );

        this.server.get(
            "/login",
            ctx -> ctx.html(Src.template("login.html"))
        );
    }

    private void obtainInfoMethodGroup() {
        this.server.post(
            "/api/posts/get",
            ctx -> {
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

                    if (hasFromTo) {
                        Values posts = PostsTool.getFromTo(
                            "create_time",
                            needInfo.isArc(),
                            needInfo.from(),
                            needInfo.to()
                        );
                        response = Values.from(true, "", posts);
                    } else if (hasLimit) {
                        response = PostsTool.getAllPosts(needInfo.isArc(), needInfo.limit());
                    } else if (hasPostId) {
                        response = PostsTool.getPost(needInfo.post_id());
                    } else {
                        ctx.status(400);
                        return;
                    }
                    if (!(boolean) response.get(0)) {
                        ctx.status(404);
                    } else {
                        ctx.json(response.get(2));
                    }

                } catch (Exception e) {
                    ctx.status(400);
                     e.printStackTrace();
                }
            }
        );

        this.server.post(
            "/api/posts/getuser",
            ctx -> {
                try {
                    PostDTO postInfo = ctx.bodyAsClass(PostDTO.class);
                    Values result = this.PostsTool.AllPostOfOneUser(postInfo.userEmail());
                    if (!(boolean) result.get(0)) {
                        ctx.status(404);
                    }
                    ctx.json(result.get(2));
                } catch (Exception e) {
                    ctx.status(404);
                }
            }
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
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void sessionRoute() {
        this.server.before(
            ctx -> {
                String requestFor = ctx.path();
                if (protectAPI.isProtectedPath(requestFor)) {
                    String session_id = ctx.header("Session-ID");
                    boolean interception = session_id == null || session_id.isEmpty();
                    if ((boolean) this.SessionOperator.loggedInBySessionID(session_id).get(0)) { interception=false; }
                    System.out.println("Need Interception: " + interception);
                    if (interception) {
                        ctx.status(401);
                    }
                } else { /*放行*/ }
            }
        );
    }
}
