package top.starwindv;


import io.javalin.Javalin;

import top.starwindv.DTO.*;
import top.starwindv.Backend.Register;
import top.starwindv.Models.Users;
import top.starwindv.Tools.Sources;


public class Forum {
    public final String AppName;
    public final Javalin server;
    public final Register register;
    public final Sources Src;
    public final Users UsersTool = new Users("Data/WindForum.db");
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
        this.register = new Register(this.UsersTool, 6, this.Poster);
        this.init();
    }

    private void init() {
        this.register();
    }

    private void register() {
        this.server.post(
            "/api/register", 
            ctx -> {
                UserDTO RegisterInfo = ctx.bodyAsClass(UserDTO.class);
                System.out.println(RegisterInfo.viewer.Stringify(RegisterInfo));
                this.register.register(
                    RegisterInfo.username(),
                    RegisterInfo.codeHash(),
                    RegisterInfo.email(),
                    ctx.attribute("IP")
                );
            }
        );
    }

}
