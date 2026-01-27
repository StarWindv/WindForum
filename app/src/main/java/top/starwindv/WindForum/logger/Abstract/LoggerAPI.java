package top.starwindv.WindForum.logger.Abstract;

import top.starwindv.WindForum.logger.Config.WindConfig;


@SuppressWarnings("unused")
public abstract class LoggerAPI {
    protected WindConfig config;
    public abstract void info(Object... obj);
    public abstract void warn(Object... obj);
    public abstract void err(Object... obj);
    public abstract void debug(Object... obj);
}
