package top.starwindv.Backend;


import top.starwindv.Utils.SQLite;
import top.starwindv.Utils.Values;
import top.starwindv.Tools.VerifyCode;


class SessionDB {
    public final String dbName;
    public final SQLite instance;
    public final String tableName = "sessions";
    public SessionDB(String dbName) {
        this.dbName = dbName;
        this.instance = new SQLite(this.dbName);
        this.init();

    }
    private void init() {
        this.instance.exec(
            "CREATE TABLE IF NOT EXISTS "
            + this.tableName
            + "("
            + "    session_id VARCHAR(16) NOT NULL"
            + ","
            + "    user_email VARCHAR(100) NOT NULL"
            + ");"
        );
        instance.exec(
            "ALTER TABLE "
                + tableName
                + " ADD COLUMN create_time DATETIME NOT NULL"
                + " DEFAULT ("
                + "     CAST((julianday('now', 'utc') - 2440587.5) * 86400000 + 0.5 AS INTEGER));"
        ); // 个人习惯, 喜欢用标准时间戳
        // 儒略日换算法
        this.instance.exec(
            "CREATE UNIQUE INDEX idx_session_id ON "+tableName+" (session_id)"
        ); // 允许多端登录所以只给session上索引
        this.instance.exec(
            "ALTER TABLE "
                + tableName
                + " ADD CONSTRAINT fk_email "
                + "FOREIGN KEY (user_email) REFERENCES users(email_str); "
        );
    }
}


public class SessionController {
    private final SessionDB db;
    public final static int sessionLength = 16;
    private final static VerifyCode CodeGen = new VerifyCode(sessionLength);

    public SessionController(String dbName) {
        this.db = new SessionDB(dbName);
    }

    public Values loggedInBySessionID(String session_id) {
        try {
            if (
                !this.db.instance.query(
                    this.db.tableName,
                    "user_email",
                    "where session_id = ?",
                    Values.from(session_id)
                ).isEmpty()
            ) {
                return Values.from(true, "User has been login");
            } else {
                return Values.from(false, "User is not login");
            }
        } catch (Exception e) {
            return Values.from(false, "Query Failed: " + e.getMessage());
        }
    }

    public Values addSession(String user_email) {
        String session_id = CodeGen.generate();
        // TODO: 存在性检验
        // 别问, 问就是我写不完了
        try {
            if (this.db.instance.insert(
                this.db.tableName,
                "(session_id, user_email)",
                Values.from(session_id, user_email)
            ) > 0) { return Values.from(true, "Add Session Success", session_id);}
            else { return Values.from(false, "Add Session Failed"); }
        } catch (Exception e) {
            return Values.from(false, "Add Session Failed: " + e.getMessage());
        }
    }

    public Values removeBySessionID(String session_id) {
        try {
            if (this.db.instance.delete(
                this.db.tableName,
                "where user_email = ?",
                Values.from(session_id)
            ) > 0) {
                return Values.from(true, "logout success");
            } else {return Values.from(false, "failed remove session by session_id"); }
        } catch (Exception e) {
            return Values.from(false, "Error when remove session by session_id: " + e.getMessage());
        }
    }

    public Values removeByEmail(String user_email) {
        try {
            if (this.db.instance.delete(
                this.db.tableName,
                "where user_email = ?",
                Values.from(user_email)
            ) > 0) {
                return Values.from(true, "logout success");
            } else {return Values.from(false, "failed remove session by email"); }
        } catch (Exception e) {
            return Values.from(false, "Error when remove session by email: " + e.getMessage());
        }
    }
}
