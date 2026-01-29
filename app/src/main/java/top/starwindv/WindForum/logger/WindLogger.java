package top.starwindv.WindForum.logger;


import org.jetbrains.annotations.NotNull;
import io.javalin.http.Context;

import top.starwindv.WindForum.logger.Abstract.LoggerAPI;
import top.starwindv.WindForum.logger.Colorful.Colors;
import top.starwindv.WindForum.logger.Config.WindConfig;
import top.starwindv.WindForum.logger.Colorful.Rich;
import top.starwindv.WindForum.logger.Errors.TraceUtil;
import top.starwindv.WindForum.logger.Feature.LogComponent;
import top.starwindv.WindForum.logger.File.ToFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

//import org.apache.commons.lang3.StringEscapeUtils;


@SuppressWarnings("unused")
public class WindLogger extends LoggerAPI {
    protected WindConfig windConfig=new WindConfig();
    protected static ToFile fm;
    private final static Map<String, Long> timecache = new ConcurrentHashMap<>();

    public WindLogger(Consumer<WindConfig> userConfig) {
        this.windConfig.applyConfig(userConfig);
        fm = new ToFile(this.windConfig.logFilePath());
    }

    @Override
    public void info (@NotNull Object... obj) {
        StringBuilder message = new StringBuilder();
        for (var ele: obj) {
            message.append(ele);
        }
        if (this.windConfig.both() || this.windConfig.toTerminal()) {
            Rich.out(this.windConfig.info_template().replace(WindConfig.msgPH, message));
        }
        if (this.windConfig.both() || this.windConfig.toFile()) {
            fm.info(String.valueOf(message));
        }
    }

    @Override
    public void warn (Object... obj) {
        String message = Arrays.toString(obj);
        if (this.windConfig.both() || this.windConfig.toTerminal()) {
            Rich.out(this.windConfig.warn_template().replace(WindConfig.msgPH, message));
        }
        if (this.windConfig.both() || this.windConfig.toFile()) {
            fm.warn(message);
        }
    }

    @Override
    public void err (Object... obj) {
        String message = Arrays.toString(obj);
        if (this.windConfig.both() || this.windConfig.toTerminal()) {
            Rich.out(this.windConfig.err_template().replace(WindConfig.msgPH, message));
        }
        if (this.windConfig.both() || this.windConfig.toFile()) {
            fm.err(message);
        }
    }
    @Override
    public void debug (Object... obj) {
        if  (this.windConfig.useDebug()) {
            StringBuilder message = new StringBuilder();
            for (var ele: obj) {
                message.append(ele);
            }
            if (this.windConfig.both() || this.windConfig.toTerminal()) {
                Rich.out(this.windConfig.debug_template().replace(WindConfig.msgPH, message.toString()));
            }
            if (this.windConfig.both() || this.windConfig.toFile()) {
                fm.debug(message.toString());
            }
        }
    }

    public void trace(Throwable e) {
        Map<String, String> result = TraceUtil.Map(e, Colors.Yellow, Colors.Red, Colors.Cyan);
        if (this.windConfig.both() || this.windConfig.toTerminal()) {
            Rich.out(result.get(TraceUtil.colorful));
        }
        if(!fm.trace(result.get(TraceUtil.noColor))) {
            fm.err("TraceBackStack Insert Error");
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

    /**
     * <h3>
     *     Feature: 使用上下文组件来进行出入站记录
     * </h3>
     * <h5>_f 前缀: _feature_xxx </h5>
     * <h5>原理: </h5>
     * 在对应会话的上下文中新建一个日志零件对象
     * <br> 在入站时写入请求方法, IP 和请求路径
     * <br> 时间由组件新建时自动写入
     * <br> 之后在出站时写入出站数据
     * <br> 并直接用 toString 转换为自定义字符串
     * <br> 这样来解决日志输出错位的问题
     * */
    public void _f_inbound(Context ctx) {
        ctx.attribute(
            "logger", new LogComponent(
                ctx.method().toString(),
                ctx.attribute("IP"),
                ctx.path()
            )
        );
    }

    public void _f_outbound(Context ctx) {
        LogComponent temp = ctx.attribute("logger");
        int code = ctx.statusCode();
        String color=code < 300 ? Colors.Green : (code < 400 ? Colors.Yellow : Colors.Red);
        if (temp != null) {
            temp.outbound(code, color);
            Rich.out(temp.toString());
        }
    }

    private void timeInfo(@NotNull Object... obj) {
        StringBuilder message = new StringBuilder();
        for (var ele: obj) {
            message.append(ele);
        }
        Rich.out(this.windConfig.time_template().replace(WindConfig.msgPH, message));
    }

    public void time() {
        String threadName = Thread.currentThread().getName();
        if (!timecache.containsKey(threadName)) {
            timecache.put(threadName, System.currentTimeMillis());
        } else {
            this.timeInfo(
                "Thread ",
                threadName,
                " Elapsed Time: [",
                System.currentTimeMillis()-timecache.get(threadName),
                " ms]"
            );
            timecache.remove(threadName);
        }
    }
}
