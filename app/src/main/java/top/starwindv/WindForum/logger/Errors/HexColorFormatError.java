package top.starwindv.WindForum.logger.Errors;


@SuppressWarnings("unused")
public class HexColorFormatError extends IllegalArgumentException{
    public static String prefix = "The incoming hex color code does not conform to the standard";
    public static String middle = ": ";

    public HexColorFormatError() {
        super(prefix);
    }
    public HexColorFormatError(String msg) {
        super(prefix+middle+msg);
    }

    public HexColorFormatError(String msg, Throwable e) {
        super(prefix+middle+msg, e);
    }
}
