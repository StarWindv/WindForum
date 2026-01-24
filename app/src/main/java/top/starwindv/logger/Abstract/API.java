package top.starwindv.logger.Abstract;

import top.starwindv.logger.config.WindConfig;


@SuppressWarnings("unused")
public abstract class API {
    protected WindConfig config;
    public abstract void info(Object... obj);
    public abstract void warn(Object... obj);
    public abstract void err(Object... obj);
    public abstract void debug(Object... obj);
}
