package top.starwindv.WindForum.logger;

import org.jetbrains.annotations.NotNull;

import top.starwindv.WindForum.logger.Abstract.LoggerAPI;
import top.starwindv.WindForum.logger.Colorful.Colors;
import top.starwindv.WindForum.logger.Config.WindConfig;
import top.starwindv.WindForum.logger.Colorful.Rich;
import top.starwindv.WindForum.logger.File.ToFile;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

//import org.apache.commons.lang3.StringEscapeUtils;


@SuppressWarnings("unused")
public class WindLogger extends LoggerAPI {
    protected WindConfig windConfig=new WindConfig();
    protected ToFile fm;

    public WindLogger(Consumer<WindConfig> userConfig) {
        this.windConfig.applyConfig(userConfig);
        if (this.windConfig.both() || this.windConfig.toFile()) {
            this.fm = new ToFile(this.windConfig.logFilePath());
        }
    }

    @Override
    public void info (@NotNull Object... obj) {
        String message = Arrays.toString(obj);
        if (this.windConfig.both() || this.windConfig.toTerminal()) {
            Rich.out(this.windConfig.info_template().replace(WindConfig.msgPH, message));
        }
        if (this.windConfig.both() || this.windConfig.toFile()) {
            this.fm.info(message);
        }
    }

    @Override
    public void warn (Object... obj) {
        String message = Arrays.toString(obj);
        if (this.windConfig.both() || this.windConfig.toTerminal()) {
            Rich.out(this.windConfig.warn_template().replace(WindConfig.msgPH, message));
        }
        if (this.windConfig.both() || this.windConfig.toFile()) {
            this.fm.warn(message);
        }
    }

    @Override
    public void err (Object... obj) {
        String message = Arrays.toString(obj);
        if (this.windConfig.both() || this.windConfig.toTerminal()) {
            Rich.out(this.windConfig.err_template().replace(WindConfig.msgPH, message));
        }
        if (this.windConfig.both() || this.windConfig.toFile()) {
            this.fm.err(message);
        }
    }
    @Override
    public void debug (Object... obj) {
        String message = Arrays.toString(obj);
        if (this.windConfig.both() || this.windConfig.toTerminal()) {
            Rich.out(this.windConfig.debug_template().replace(WindConfig.msgPH, message));
        }
        if (this.windConfig.both() || this.windConfig.toFile()) {
            this.fm.debug(message);
        }
    }

    public void inbound(String ip, String method, String path) {
        Rich.out(
            this.windConfig.inbound_template()
                .replace(WindConfig.ipPH, ip)
                .replace(WindConfig.methodPH, method)
                .replace(WindConfig.pathPH, path)
        );
    }

    public void outbound(Integer code, String ip) {
        String color=code < 300 ? Colors.Green : (code < 400 ? Colors.Yellow : Colors.Red);
//        System.err.println(StringEscapeUtils.escapeJava("|>" + color));
        String result = this.windConfig.outbound_template()
            .replace(WindConfig.ipPH, ip)
            .replace(WindConfig.statusColorPH, color)
            .replace(WindConfig.statusPH, code.toString());
//        System.err.println(StringEscapeUtils.escapeJava("|>" + result));
        Rich.out(result);
    }

    public void title(String colorTag) {
        Rich.out(this.windConfig.title().replace(WindConfig.titleColorPH, colorTag));
    }

    public void startMsg(List<String> IPList) {
        for(String ip: IPList) {
            Rich.out(this.windConfig.start_msg_template().replace(WindConfig.ipPH, ip));
        }
    }
}
