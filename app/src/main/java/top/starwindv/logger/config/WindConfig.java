package top.starwindv.logger.config;

import io.javalin.json.JsonMapper;


@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class WindConfig {
    private JsonMapper jsonMapper;
    public long cutSize = 500 * 1024;
    private boolean toFile = false;
    private boolean toTerminal = true;
    private boolean both = false;
    private boolean debugMode = false;
    private String title = """
            ██╗    ██╗ ██╗ ███╗   ██╗ ██████╗     ███████╗  ██████╗  ██████╗  ██╗   ██╗ ███╗   ███╗
            ██║    ██║ ██║ ████╗  ██║ ██╔══██╗    ██╔════╝ ██╔═══██╗ ██╔══██╗ ██║   ██║ ████╗ ████║
            ██║ █╗ ██║ ██║ ██╔██╗ ██║ ██║  ██║    █████╗   ██║   ██║ ██████╔╝ ██║   ██║ ██╔████╔██║
            ██║███╗██║ ██║ ██║╚██╗██║ ██║  ██║    ██╔══╝   ██║   ██║ ██╔══██╗ ██║   ██║ ██║╚██╔╝██║
            ╚███╔███╔╝ ██║ ██║ ╚████║ ██████╔╝    ██║      ╚██████╔╝ ██║  ██║ ╚██████╔╝ ██║ ╚═╝ ██║
             ╚══╝╚══╝  ╚═╝ ╚═╝  ╚═══╝ ╚═════╝     ╚═╝       ╚═════╝  ╚═╝  ╚═╝  ╚═════╝  ╚═╝     ╚═╝                                                                                                                 \s
        """;

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
    public void setTitle(String us) { this.title = us; }
}
