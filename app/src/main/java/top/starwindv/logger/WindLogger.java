package top.starwindv.logger;

import top.starwindv.logger.Abstract.API;
import top.starwindv.logger.config.WindConfig;

import java.util.function.Consumer;

public class WindLogger extends API {
    protected WindConfig windConfig;

    public WindLogger() {}

    @Override
    protected void applyConfig(Consumer<WindConfig> userConfig) {
        this.windConfig = new WindConfig();
    }

    @Override
    public void info (Object... obj) {
        // TODO
    }
    @Override
    public void warn (Object... obj) {
        // TODO
    }
    @Override
    public void err (Object... obj) {
        // TODO
    }
    @Override
    public void debug (Object... obj) {
        // TODO
    }
}
