package top.starwindv.WindForum.logger.File.Model;


import top.starwindv.WindForum.SQL.SQLite;
import top.starwindv.WindForum.forum.Utils.Values;
import top.starwindv.WindForum.logger.File.LogLevel;


@SuppressWarnings("unused")
public class LogDB {
    private String LogDBPath;
    private final SQLite db;
    private static final String tableName="log";

    public LogDB(String LogDBPath) {
        this.LogDBPath = LogDBPath;
        this.db = new SQLite(this.LogDBPath);
        this.init();
    }
    public void LogDBPath(String newPath) { this.LogDBPath = newPath; }
    public String LogDBPath() { return LogDBPath; }

    private void init() {
        this.db.exec(
            String.format("""
                CREATE TABLE IF NOT EXISTS %s (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    level TEXT NOT NULL,
                    timestamp DATETIME NOT NULL DEFAULT (
                        CAST(
                            (julianday('now', 'utc') - 2440587.5) * 86400000 + 0.5 AS INTEGER
                        )
                    ),
                    msg TEXT NOT NULL,
                    thread_name TEXT
                )
                """, tableName)
        );
        this.db.exec(String.format("""
            CREATE INDEX IF NOT EXISTS idx_level ON %s(level)
            """, tableName));
        this.db.exec(String.format("""
            CREATE INDEX IF NOT EXISTS idx_timestamp ON %s(timestamp)
            """, tableName));
        this.db.exec(String.format("""
            CREATE INDEX IF NOT EXISTS idx_msg ON %s(msg)
            """, tableName));
    }

    public boolean insert(String msg, LogLevel level) {
        return this.db.insert(
            tableName,
            "(msg, level)",
            Values.from(msg, level)
        ) > 0;
    }

    public boolean traceInsert(String msg, LogLevel level) {
        return this.db.insert(
            tableName,
            "(msg, level, thread_name)",
            Values.from(msg, level, Thread.currentThread().getName())
        ) > 0;
    }
}
