package top.starwindv.WindForum.logger.Config;

import io.javalin.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.nio.file.Files;


@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class WindConfig {
    private static String appName = "WindApplication";
    private static JsonMapper jsonMapper;
    private static Long cutSize = (long) (500 * 1024);
    private static Boolean toFile = false;
    private static Boolean toTerminal = true;
    private static Boolean both = false;
    private static Boolean debugMode = false;
    private static Path logPath = Paths.get(
                                        System.getProperty("user.home")
                                    ).normalize().resolve(appName);
    private static Path logFilePath = logPath.resolve("log.sqlite");

    public static final String msgPH = "$msg";
    public static final String ipPH = "$ip";
    public static final String methodPH = "$method";
    public static final String pathPH = "$path";
    public static final String statusPH = "$status";
    public static final String statusColorPH = "$statusColor";
    public static final String titleColorPH = "$titleColor";

    private final static String title = titleColorPH+"""
            ██╗    ██╗ ██╗ ███╗   ██╗ ██████╗     ███████╗  ██████╗  ██████╗  ██╗   ██╗ ███╗   ███╗
            ██║    ██║ ██║ ████╗  ██║ ██╔══██╗    ██╔════╝ ██╔═══██╗ ██╔══██╗ ██║   ██║ ████╗ ████║
            ██║ █╗ ██║ ██║ ██╔██╗ ██║ ██║  ██║    █████╗   ██║   ██║ ██████╔╝ ██║   ██║ ██╔████╔██║
            ██║███╗██║ ██║ ██║╚██╗██║ ██║  ██║    ██╔══╝   ██║   ██║ ██╔══██╗ ██║   ██║ ██║╚██╔╝██║
            ╚███╔███╔╝ ██║ ██║ ╚████║ ██████╔╝    ██║      ╚██████╔╝ ██║  ██║ ╚██████╔╝ ██║ ╚═╝ ██║
             ╚══╝╚══╝  ╚═╝ ╚═╝  ╚═══╝ ╚═════╝     ╚═╝       ╚═════╝  ╚═╝  ╚═╝  ╚═════╝  ╚═╝     ╚═╝
            """;
    private static String customTitle=null;

    private static String info_template ="[,255,255]<Bold>[INFO ] <Reset><TIMESTAMP> <Bold>[#8DF6E8]"+msgPH;
    private static String err_template  = "[#DC143C]<Bold>[Err  ] <Reset><TIMESTAMP> <Bold>[#FF656F]"+msgPH;
    private static String warn_template = "[#FFDA00]<Bold>[Warn ] <Reset><TIMESTAMP> <Bold>[#FFDA00]"+msgPH;
    private static String debug_template= "[#1E90FF]<Bold>[Debug] <Reset><TIMESTAMP> <Bold>[#1E90FF]"+msgPH;

    private static String time_template = "[#607D8B]<Bold>[TIME ] <Reset><TIMESTAMP> <Bold>[#607D8B]"+msgPH;

    private static String  inbound_template="\n<Bold>[,255,255][->] <Reset><TIMESTAMP> ["+methodPH+"] ["+ipPH+"] ["+pathPH+"]";
    private static String outbound_template="<Bold>[255,215,][<-] <Reset><TIMESTAMP> ["+statusColorPH+"<Bold>"+statusPH+"<Reset>] ["+ipPH+"]";

    private static String start_msg_template = " * Serve on: " + ipPH;

    public void cutSize(Long us) { cutSize=us; }

    public Path logFilePath() { return logFilePath; }
    public void logFilePath(Path newPath) { logFilePath=newPath; }

    public void info_template(String us) { info_template = us;}
    public String info_template() { return info_template; }

    public void err_template(String us) { err_template = us;}
    public String err_template() { return err_template; }

    public void warn_template(String us) { warn_template = us;}
    public String warn_template() { return warn_template; }

    public void debug_template(String us) { debug_template = us;}
    public String debug_template() { return debug_template; }

    public void time_template(String us) { time_template = us;}
    public String time_template() { return time_template; }

    public void inbound_template(String us) { inbound_template = us;}
    public String inbound_template() { return inbound_template; }

    public void outbound_template(String us) { outbound_template = us;}
    public String outbound_template() { return outbound_template; }

    public void start_msg_template(String us) { start_msg_template = us;}
    public String start_msg_template() { return start_msg_template; }

    public void applyConfig(Consumer<WindConfig> userConfig) {
        userConfig.accept(this);
        try {
            Files.createDirectories(logPath);
        } catch (IOException e) {
            System.err.println("[WindConfig] Cannot create log folder: " + logPath);
            System.err.println("             Logs will be written to the terminal");
            toFile = false;
            toTerminal = true;
            both = false;
        }
    }

    public void jsonMapper(JsonMapper userMapper) { jsonMapper = userMapper; }

    /** @param us User Select */
    public void toFile(boolean us) { toFile = us; }
    public boolean toFile() { return toFile; }

    public void toTerminal(boolean us) { toTerminal = us; }
    public boolean toTerminal() { return toTerminal; }

    public void both(boolean us) {
        both = us;
        toFile(true);
        toTerminal(true);
    }
    public boolean both() { return both; }

    public void useDebug(boolean us) { debugMode = us; }
    public boolean useDebug() { return debugMode; }

    public void setTitle(String us) { customTitle = us; }
    public String setTitle() { return customTitle; }

    public void logPath(String us) { logPath= Path.of(us); }
    public String logPath() { return logPath.toString(); }

    public void appName(String us) { appName=us; }
    public String appName() { return appName; }

    @Override
    public String toString() {
        String result = """
        WindConfig:
         - appName: %s
           Type   : %s

         - logPath: %s
           Type   : %s
        
         - cutSize: %s
           Type   : %s

         - title  : %s
           Type   : %s

         - toFile : %s
           Type   : %s

         - both   : %s
           Type   : %s

         - toTerminal : %s
           Type       : %s

         - debugMode  : %s
           Type       : %s

         - customTitle: %s
           Value      : %s
           Type       : %s
        """;
        result = String.format(
            result,
            appName, appName.getClass(),
            logPath, logPath.getClass(),
            cutSize, cutSize.getClass(),
            customTitle, customTitle.getClass(),
            toFile, toFile.getClass(),
            both, both.getClass(),
            toTerminal, toTerminal.getClass(),
            debugMode, debugMode.getClass(),
            title.equals(customTitle), customTitle, customTitle.getClass()
        );
        return result;
    }

    public String title() {
        if (customTitle != null) {
            return customTitle;
        } return title;
    }
}
