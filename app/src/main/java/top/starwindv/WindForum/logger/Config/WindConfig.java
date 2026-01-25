package top.starwindv.WindForum.logger.Config;

import io.javalin.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.nio.file.Files;


@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class WindConfig {
    private String appName = "[WindApplication]";
    private JsonMapper jsonMapper;
    public Long cutSize = (long) (500 * 1024);
    private Boolean toFile = false;
    private Boolean toTerminal = true;
    private Boolean both = false;
    private Boolean debugMode = false;
    private String logPath = System.getProperty("user.home") + appName;
    private final String title = """
            ██╗    ██╗ ██╗ ███╗   ██╗ ██████╗     ███████╗  ██████╗  ██████╗  ██╗   ██╗ ███╗   ███╗
            ██║    ██║ ██║ ████╗  ██║ ██╔══██╗    ██╔════╝ ██╔═══██╗ ██╔══██╗ ██║   ██║ ████╗ ████║
            ██║ █╗ ██║ ██║ ██╔██╗ ██║ ██║  ██║    █████╗   ██║   ██║ ██████╔╝ ██║   ██║ ██╔████╔██║
            ██║███╗██║ ██║ ██║╚██╗██║ ██║  ██║    ██╔══╝   ██║   ██║ ██╔══██╗ ██║   ██║ ██║╚██╔╝██║
            ╚███╔███╔╝ ██║ ██║ ╚████║ ██████╔╝    ██║      ╚██████╔╝ ██║  ██║ ╚██████╔╝ ██║ ╚═╝ ██║
             ╚══╝╚══╝  ╚═╝ ╚═╝  ╚═══╝ ╚═════╝     ╚═╝       ╚═════╝  ╚═╝  ╚═╝  ╚═════╝  ╚═╝     ╚═╝                                                                                                                 \s
        """;
    private String customTitle="";

    private String info_template ="";
    private String err_template  ="";
    private String warn_template ="";
    private String debug_template="";

    public void info_template(String us) { this.info_template = us;}
    public String info_template() { return this.info_template; }

    public void err_template(String us) { this.err_template = us;}
    public String err_template() { return this.err_template; }

    public void warn_template(String us) { this.warn_template = us;}
    public String warn_template() { return this.warn_template; }

    public void debug_template(String us) { this.debug_template = us;}
    public String debug_template() { return this.debug_template; }

    public void applyConfig(Consumer<WindConfig> userConfig) {
        userConfig.accept(this);
        try {
            Files.createDirectories(Path.of(this.logPath));
        } catch (IOException e) {
            System.err.println("[WindConfig] Cannot create log folder");
            System.err.println("             Logs will be written to the terminal");
            this.toFile = false;
            this.toTerminal = true;
            this.both = false;
        }
    }

    public void jsonMapper(JsonMapper userMapper) { this.jsonMapper = userMapper; }

    /** @param us User Select */
    public void toFile(boolean us) { this.toFile = us; }
    public boolean toFile() { return this.toFile; }

    public void toTerminal(boolean us) { this.toTerminal = us; }
    public boolean toTerminal() { return this.toTerminal; }

    public void both(boolean us) {
        this.both = us;
        this.toFile(true);
        this.toTerminal(true);
    }
    public boolean both() { return this.both; }

    public void useDebug(boolean us) { this.debugMode = us; }
    public boolean useDebug() { return this.debugMode; }

    public void setTitle(String us) { this.customTitle = us; }
    public String setTitle() { return this.customTitle; }

    public void logPath(String us) { this.logPath=us; }
    public String logPath() { return this.logPath; }

    public void appName(String us) { this.appName=us; }
    public String appName() { return this.appName; }

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
            this.appName, this.appName.getClass(),
            this.logPath, this.logPath.getClass(),
            this.cutSize, this.cutSize.getClass(),
            this.customTitle, this.customTitle.getClass(),
            this.toFile, this.toFile.getClass(),
            this.both, this.both.getClass(),
            this.toTerminal, this.toTerminal.getClass(),
            this.debugMode, this.debugMode.getClass(),
            this.title.equals(this.customTitle), this.customTitle, this.customTitle.getClass()
        );
        return result;
    }
}
