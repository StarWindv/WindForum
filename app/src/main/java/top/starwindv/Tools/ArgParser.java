package top.starwindv.Tools;


import picocli.CommandLine;
import picocli.CommandLine.Option;


@SuppressWarnings("unused")
@CommandLine.Command(name = "[Run WindForum Command]", mixinStandardHelpOptions = true,
    description = "WindForum Runtime Config")
public class ArgParser implements Runnable {

    public final static ArgParser instance = new ArgParser();

    @Option(names = {"-p", "--port"}, defaultValue="7000", description = "Server Bind Port")
    private String port;

    @Option(names = {"-h", "--host"}, defaultValue="0.0.0.0", description = "Server Bind Host")
    private String host;

    public String port() { return this.port; }
    public String host() { return this.host; }

    @Override
    public void run() {
//        System.out.println("***** " + System.getProperty("sun.java.command"));
    }

}
