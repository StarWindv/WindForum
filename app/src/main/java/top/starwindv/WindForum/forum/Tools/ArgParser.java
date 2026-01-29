package top.starwindv.WindForum.forum.Tools;


import picocli.CommandLine;
import picocli.CommandLine.Option;


@SuppressWarnings({"unused", "FieldMayBeFinal", "FieldCanBeLocal"})
@CommandLine.Command(name = "[Run WindForum Command]", mixinStandardHelpOptions = true,
    description = "WindForum Runtime Config")
public class ArgParser implements Runnable {

    public final static ArgParser instance = new ArgParser();

    @Option(names = {"-p", "--port"}, defaultValue="7000", description = "Server Bind Port")
    private String port;

    @Option(names = {"-h", "--host"}, defaultValue="0.0.0.0", description = "Server Bind Host")
    private String host;

    @Option(names = {"-f", "--feature"}, description = "Use New Logger Feature or Not, Default: true")
    private boolean useFeature=true;

    @Option(names = {"-d", "--debug"}, description = "Start with Debug Mode")
    private boolean debug=false;

    public String port() { return this.port; }
    public String host() { return this.host; }
    public boolean useFeature() { return this.useFeature; }
    public boolean debug() { return this.debug; }

    @Override
    public void run() {}

}
