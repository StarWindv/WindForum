//DEPS io.javalin:javalin:6.7.0
//DEPS org.slf4j:slf4j-simple:2.0.7
//DEPS org.xerial:sqlite-jdbc:3.51.1.0
//DEPS org.apache.commons:commons-lang3:3.20.0

//SOURCES Utils/FStyles.java
//SOURCES Utils/Values.java
//SOURCES Utils/Users.java
//SOURCES Utils/SQLite.java
package top.starwindv.WindForum.forum;


import java.nio.file.Path;

import io.javalin.util.JavalinBindException;

import picocli.CommandLine;

import top.starwindv.WindForum.forum.Server.Forum;
import top.starwindv.WindForum.forum.Tools.ArgParser;
import top.starwindv.WindForum.forum.Server.BaseServer;
import top.starwindv.WindForum.logger.WindLogger;



public class Main {
    private final static BaseServer server = new BaseServer();
    private static void start(
        String ip,
        int port,
        boolean useFeature,
        boolean debug,
        boolean time
    ) {
        server.Logger = new WindLogger(
            config -> {
                config.logFilePath(Path.of("Data/log.db"));
                config.useDebug(debug);
                config.useTimeLog(time);
            }
        );
        server.start(ip, port);
        BaseServer.UseLogFeature(useFeature);
        new Forum("Wind", server.instance(), server.Src, server.Logger);
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(ArgParser.instance);
        int status = cmd.execute(args);
        if (status!=0) { System.exit(status);}
        if (ArgParser.instance.help()) { return; }
        try {
            start(
                ArgParser.instance.host(),
                Integer.parseInt(ArgParser.instance.port()),
                ArgParser.instance.useFeature(),
                ArgParser.instance.debug(),
                ArgParser.instance.time()
            );
        } catch (JavalinBindException e) {
            System.err.println(" x This Port Has Been Bind");
            System.err.println(" x EXIT");
        }
    }
}
