package top.starwindv;


import io.javalin.Javalin;

import top.starwindv.DTO.*;


public class Forum {
    public final String AppName;
    public final Javalin server;
    public Forum(String AppName, Javalin instance) {
        this.AppName = AppName;
        this.server = instance;
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
            }
        );
    }

}
