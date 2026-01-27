package top.starwindv.WindForum.logger.File;

import top.starwindv.WindForum.logger.Errors.NoSuchLogLevel;

public enum LogLevel {
    DEBUG,
    INFO,
    ERR,
    ERROR,
    TRACE,
    WARN;

    public static LogLevel from(String name) {
        String upperName = name.toUpperCase();
        for (LogLevel level : LogLevel.values()) {
            if (level.name().equals(upperName)) {
                return level;
            }
        }
        throw new NoSuchLogLevel(upperName);
    }
}
