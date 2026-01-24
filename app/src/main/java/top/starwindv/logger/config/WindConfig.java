package top.starwindv.logger.config;

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
    public void jsonMapper(JsonMapper userMapper) { this.jsonMapper = userMapper; }
    /** @param us User Select */
    public void toFile(boolean us) { this.toFile = us; }
    public void toTerminal(boolean us) { this.toTerminal = us; }
    public void both(boolean us) {
        this.both = us;
        this.toFile(true);
        this.toTerminal(true);
    }
    public void useDebug(boolean us) { this.debugMode = us; }
    public void setTitle(String us) { this.customTitle = us; }
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

    public void logPath(String us) {
        this.logPath=us;
    }
    public void appName(String us) {
        this.appName=us;
    }

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
