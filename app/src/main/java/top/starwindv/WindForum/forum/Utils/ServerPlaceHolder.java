package top.starwindv.WindForum.forum.Utils;


import java.util.Map;


@SuppressWarnings("unused")
public class ServerPlaceHolder {
    public static String Session_ID = "Session-ID";
    public static String Empty = "<|STV_Empty|>";
    public static String IP = "IP";
    public static String Logger = "logger";
    public static String user_email = "user_email";
    public static String html_inject = "/* <|method|> */";

    private final static String code = "code";
    private final static String msg  = "msg";
    private final static String msgUpdater = "updateErrorPage(" + code + ", 'Opus', '" + msg + "')";
    private static String msgUpdater(Integer status, String tips) {
        return msgUpdater.replace(code, status.toString())
            .replace(msg, tips);
    }

    public static String msgUpdater(int status) {
        if (statusCode.containsKey(status)) {
            return msgUpdater(status, statusCode.get(status));
        } return "";
    }

    public final static Map<Integer, String> statusCode = Map.ofEntries(
        Map.entry(400, "错误请求 (Bad Request)"),
        Map.entry(401, "未授权 (Unauthorized)"),
        Map.entry(402, "需要付款 (Payment Required)"),
        Map.entry(403, "禁止访问 (Forbidden)"),
        Map.entry(404, "未找到 (Not Found)"),
        Map.entry(405, "方法不允许 (Method Not Allowed)"),
        Map.entry(406, "不接受 (Not Acceptable)"),
        Map.entry(407, "需要代理认证 (Proxy Authentication Required)"),
        Map.entry(408, "请求超时 (Request Timeout)"),
        Map.entry(409, "冲突 (Conflict)"),
        Map.entry(410, "已删除 (Gone)"),
        Map.entry(411, "需要长度 (Length Required)"),
        Map.entry(412, "前置条件失败 (Precondition Failed)"),
        Map.entry(413, "请求实体过大 (Payload Too Large)"),
        Map.entry(414, "请求URI过长 (URI Too Long)"),
        Map.entry(415, "不支持的媒体类型 (Unsupported Media Type)"),
        Map.entry(416, "范围不满足 (Range Not Satisfiable)"),
        Map.entry(417, "期望失败 (Expectation Failed)"),
        Map.entry(418, "我是茶壶 (I'm a teapot，趣味状态码)"),
        Map.entry(421, "错误的请求 (Misdirected Request)"),
        Map.entry(422, "无法处理的实体 (Unprocessable Entity)"),
        Map.entry(423, "锁定 (Locked)"),
        Map.entry(424, "依赖失败 (Failed Dependency)"),
        Map.entry(425, "太早 (Too Early)"),
        Map.entry(426, "需要升级 (Upgrade Required)"),
        Map.entry(428, "前置条件要求 (Precondition Required)"),
        Map.entry(429, "请求过多 (Too Many Requests)"),
        Map.entry(431, "请求头字段过大 (Request Header Fields Too Large)"),
        Map.entry(451, "因法律原因不可用 (Unavailable For Legal Reasons)"),

        Map.entry(500, "内部服务器错误 (Internal Server Error)"),
        Map.entry(501, "未实现 (Not Implemented)"),
        Map.entry(502, "错误网关 (Bad Gateway)"),
        Map.entry(503, "服务不可用 (Service Unavailable)"),
        Map.entry(504, "网关超时 (Gateway Timeout)"),
        Map.entry(505, "HTTP版本不受支持 (HTTP Version Not Supported)"),
        Map.entry(506, "变体也协商 (Variant Also Negotiates)"),
        Map.entry(507, "存储空间不足 (Insufficient Storage)"),
        Map.entry(508, "检测到循环 (Loop Detected)"),
        Map.entry(510, "未扩展 (Not Extended)"),
        Map.entry(511, "需要网络认证 (Network Authentication Required)")
    );
}
