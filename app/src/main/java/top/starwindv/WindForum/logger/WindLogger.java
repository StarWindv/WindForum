package top.starwindv.WindForum.logger;

import top.starwindv.WindForum.logger.Abstract.API;
import top.starwindv.WindForum.logger.config.WindConfig;

import java.util.function.Consumer;


@SuppressWarnings("unused")
public class WindLogger extends API {
    protected WindConfig windConfig=new WindConfig();

    public WindLogger(Consumer<WindConfig> userConfig) {
        this.windConfig.applyConfig(userConfig);
    }

    @Override
    public void info (Object... obj) {
        if (this.windConfig.both()) {}
        else if (this.windConfig.toTerminal()) {}
        else if (this.windConfig.toFile()) {}
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
