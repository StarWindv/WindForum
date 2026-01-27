package top.starwindv.WindForum.logger.File;


import top.starwindv.WindForum.logger.Errors.NoSuchLogLevel;
import top.starwindv.WindForum.logger.File.Model.LogDB;

import java.nio.file.Path;


@SuppressWarnings("unused")
public class ToFile extends ToFileAPI {
    private final LogDB db;
    public ToFile(String logFolderPath, String dbName) {
        this.folderPath = logFolderPath;
        this.db = new LogDB(dbName);
    }

    public ToFile(Path dbFullPath) {
        this.db = new LogDB(dbFullPath.toString());
    }

    @Override
    public boolean write(String msg, String level) {
        try {
            LogLevel _level = LogLevel.from(level.toUpperCase());
            return switch (_level) {
                case LogLevel.DEBUG -> this.debug(msg);
                case LogLevel.INFO -> this.info(msg);
                case LogLevel.WARN -> this.warn(msg);
                case LogLevel.ERR, LogLevel.ERROR -> this.err(msg);
                default -> throw new NoSuchLogLevel(level);
            };
        } catch (NoSuchLogLevel e) {
            throw new NoSuchLogLevel(e);
        }
    }

    @Override
    public void folderPath(String newPath) {
        this.folderPath = newPath;
    }

    @Override
    public boolean info(String msg) {
        return this.db.insert(msg, LogLevel.INFO);
    }

    @Override
    public boolean err(String msg) {
        return this.db.insert(msg, LogLevel.ERR);
    }

    @Override
    public boolean warn(String msg) {
        return this.db.insert(msg, LogLevel.WARN);
    }

    @Override
    public boolean debug(String msg) {
        return this.db.insert(msg, LogLevel.DEBUG);
    }
}
