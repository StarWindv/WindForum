package top.starwindv.WindForum.logger.Feature;


import java.text.SimpleDateFormat;


@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class LogComponent {
    private String inbound;
    private Long inboundTimeStamp;

    private String outbound;
    private Long outboundTimeStamp;

    private String ip;
    private String method;
    private String path;

    private Integer statusCode;
    private String statusColor;

    private static final SimpleDateFormat Formatter = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");

    public static final String ipPH = "$ip";
    public static final String methodPH = "$method";
    public static final String pathPH = "$path";
    public static final String statusPH = "$status";
    public static final String statusColorPH = "$statusColor";
    public static final String timestampPH = "$TIMESTAMP";
    public static final String timeUsedPH = "$TIME_USED";

    private static String  inbound_template="\n<Bold>[,255,255][->] <Reset>"+timestampPH+" ["+methodPH+"] ["+ipPH+"] ["+pathPH+"]\n";
    private static String outbound_template=  "<Bold>[255,215,][<-] <Reset>"+timestampPH+" ["+statusColorPH+"<Bold>"+statusPH+"<Reset>] ["+ipPH+"] ["+timeUsedPH+" ms]";

    public void inbound(String requestMethod, String ip, String requestPath) {
        this.inboundTimeStamp = System.currentTimeMillis();
        this.inbound = Formatter.format(new java.util.Date());
        this.method = requestMethod;
        this.ip = ip;
        this.path = requestPath;
    }
    public void outbound(int statusCode, String statusColor) {
        this.outbound = Formatter.format(new java.util.Date());
        this.outboundTimeStamp = System.currentTimeMillis();
        this.statusCode = statusCode;
        this.statusColor = statusColor;
    }
    public static void inbound_template(String newTemplate) {
        inbound_template = newTemplate;
    }
    public static void outbound_template(String newTemplate) {
        outbound_template = newTemplate;
    }

    public LogComponent() {}
    public LogComponent(
        String requestMethod,
        String ip,
        String requestPath
    ) {
        this.inbound(requestMethod, ip, requestPath);
    }

    @Override
    public String toString() {
        String inboundResult  = inbound_template
            .replace(timestampPH, this.inbound)
            .replace(methodPH, this.method)
            .replace(ipPH, this.ip)
            .replace(pathPH, this.path);
        String outboundResult = outbound_template
            .replace(timestampPH, this.outbound)
            .replace(statusColorPH, this.statusColor)
            .replace(statusPH, this.statusCode.toString())
            .replace(ipPH, this.ip)
            .replace(timeUsedPH, String.valueOf(this.outboundTimeStamp-this.inboundTimeStamp));
        return inboundResult+outboundResult;
    }
}
