package top.starwindv.WindForum.logger.File;


@SuppressWarnings("unused")
public abstract class ToFileAPI {
    public static final String info ="info";
    public static final String warn ="warn";
    public static final String err  ="err";
    public static final String debug="debug";

    protected String folderPath;
    public abstract boolean write(String msg, String level);
    public abstract void folderPath(String newPath);
    public abstract boolean info (String msg);
    public abstract boolean err  (String msg);
    public abstract boolean warn (String msg);
    public abstract boolean debug(String msg);
}
