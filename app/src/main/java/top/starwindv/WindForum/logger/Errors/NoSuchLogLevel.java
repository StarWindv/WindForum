package top.starwindv.WindForum.logger.Errors;


@SuppressWarnings("unused")
public class NoSuchLogLevel extends IllegalArgumentException {
    private static final String prefix = "No Such Log Level or Not Implements: ";
    public NoSuchLogLevel(String message) {
        super(prefix+message);
    }
    public NoSuchLogLevel(Throwable e) {
        super(e);
    }
}
