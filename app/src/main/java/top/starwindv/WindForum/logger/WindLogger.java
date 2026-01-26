package top.starwindv.WindForum.logger;

import top.starwindv.WindForum.logger.Abstract.API;
import top.starwindv.WindForum.logger.Colorful.Colors;
import top.starwindv.WindForum.logger.Config.WindConfig;
import top.starwindv.WindForum.logger.Colorful.Rich;

import java.util.Arrays;
import java.util.function.Consumer;

//import org.apache.commons.lang3.StringEscapeUtils;


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

    public void inbound(String ip, String method, String path) {
        Rich.out(
            WindConfig.inbound_template()
                .replace(WindConfig.ipPH, ip)
                .replace(WindConfig.methodPH, method)
                .replace(WindConfig.pathPH, path)
        );
    }

    public void outbound(Integer code, String ip) {
        String color=code < 300 ? Colors.Green : (code < 400 ? Colors.Yellow : Colors.Red);
//        System.err.println(StringEscapeUtils.escapeJava("|>" + color));
        String result = WindConfig.outbound_template()
            .replace(WindConfig.ipPH, ip)
            .replace(WindConfig.statusColorPH, color)
            .replace(WindConfig.statusPH, code.toString());
//        System.err.println(StringEscapeUtils.escapeJava("|>" + result));
        Rich.out(result);
    }

    public void title(String colorTag) {
        Rich.out(WindConfig.title().replace(WindConfig.titleColorPH, colorTag));
    }
}
