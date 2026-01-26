package top.starwindv.WindForum.logger;

import top.starwindv.WindForum.logger.Abstract.API;
import top.starwindv.WindForum.logger.Config.WindConfig;
import top.starwindv.WindForum.logger.Colorful.Rich;

import java.util.Arrays;
import java.util.function.Consumer;


@SuppressWarnings("unused")
public class WindLogger extends API {
    protected WindConfig windConfig=new WindConfig();

    public WindLogger(Consumer<WindConfig> userConfig) {
        this.windConfig.applyConfig(userConfig);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void info (Object... obj) {
        if (this.windConfig.both()) {}
        else if (this.windConfig.toTerminal()) {
            Rich.out(WindConfig.info_template().replace(WindConfig.msgPH, Arrays.toString(obj)));
        }
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
