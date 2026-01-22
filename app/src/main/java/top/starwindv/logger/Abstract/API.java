package top.starwindv.logger.Abstract;

import top.starwindv.logger.config.WindConfig;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public abstract class API {
    protected WindConfig config;

    protected abstract void applyConfig(Consumer<WindConfig> userConfig);
    public abstract void info(Object... obj);
    public abstract void warn(Object... obj);
    public abstract void err(Object... obj);
    public abstract void debug(Object... obj);
}
