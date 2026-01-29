package top.starwindv.WindForum.logger.Errors;


import top.starwindv.WindForum.logger.Colorful.Colors;

import java.util.HashMap;
import java.util.Map;


public class TraceUtil {
    /**
     * Place Holder
     * */
    public static String causeColorPH = "$causeColor";
    public static String classColorPH = "$classColor";
    public static String msgColorPH   = "$msgColorColor";

    public static String noColor = "noColor";
    public static String colorful = "colorful";

    private static String Map(
        Throwable e
    ) {
        if (e == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        buildStackTrace(e, sb, false);
        return sb.toString();
    }

    public static Map<String, String> Map(
        Throwable e,
        String causeColor,
        String classColor,
        String msgColor
    ) {
        Map<String, String> result = new HashMap<>();
        String rawStackTrace = Map(e);
        result.put(
            noColor,
            rawStackTrace.replace(causeColorPH, "")
                .replace(classColorPH, "")
                .replace(msgColorPH, "")
        );
        result.put(
            colorful,
            rawStackTrace.replace(causeColorPH, causeColor)
                .replace(classColorPH, classColor)
                .replace(msgColorPH, msgColor)
        );
        return result;
    }

    private static void buildStackTrace(
        Throwable throwable,
        StringBuilder sb,
        boolean isCause
    ) {
        if (isCause) {
            sb.append(causeColorPH)
                .append("Caused by: ")
                .append(Colors.Reset);
        }
        sb.append(classColorPH)
            .append(throwable.getClass().getName())
            .append(": ")
            .append(msgColorPH)
            .append(throwable.getMessage())
            .append(Colors.Reset)
            .append(System.lineSeparator());

        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            sb.append("\tat ")
                .append(element.getClassName())
                .append(".")
                .append(element.getMethodName())
                .append("(")
                .append(element.getFileName())
                .append(":")
                .append(element.getLineNumber())
                .append(")")
                .append(System.lineSeparator());
        }

        Throwable cause = throwable.getCause();
        if (cause != null) {
            buildStackTrace(cause, sb, true);
        }
    }
}
