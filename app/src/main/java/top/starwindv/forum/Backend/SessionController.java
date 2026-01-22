package top.starwindv.forum.Backend;


import top.starwindv.forum.SQL.SQLite;
import top.starwindv.forum.Utils.Values;
import top.starwindv.forum.Tools.VerifyCode;

import java.util.List;
import java.util.Map;


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
        List<Map<String, Object>> tables = this.instance.query(
            "sqlite_master",
            "name",
            "type='table' AND name=?",
            Values.from(tableName)
        );
        if (!tables.isEmpty()) { return; }
        this.instance.exec(
            "CREATE TABLE IF NOT EXISTS "
            + this.tableName
            + "("
            + "    session_id VARCHAR(16) NOT NULL"
            + ","
            + "    user_email VARCHAR(100) NOT NULL,"
            + " CONSTRAINT fk_email FOREIGN KEY (user_email) REFERENCES users(email_str));"
        );
        this.instance.exec(
            "ALTER TABLE "
                + tableName
                + " ADD COLUMN create_time DATETIME NOT NULL"
                + " DEFAULT ("
                + "     CAST((julianday('now', 'utc') - 2440587.5) * 86400000 + 0.5 AS INTEGER)"
                + " );"
        ); // 个人习惯, 喜欢用标准时间戳
        // 儒略日换算法
        this.instance.exec(
            "CREATE UNIQUE INDEX idx_session_id ON "+tableName+" (session_id)"
        ); // 允许多端登录所以只给session上索引
    }
}


@SuppressWarnings("UnusedReturnValue")
public class SessionController {
    private final SessionDB db;
    public final static int sessionLength = 16;
    private final static VerifyCode CodeGen = new VerifyCode(sessionLength);
    public final static Long validityPeriod = 24 * 60  * 60 * 1000L; // 24 h * 60 min * 60 s = 1 day

    public SessionController(String dbName) {
        this.db = new SessionDB(dbName);
    }

    public Values loggedInBySessionID(String session_id) {
        try {
            Values result = this.isOutdate(session_id);
            System.err.println("isOutdate: " + result);
            if (!(boolean) result.getFirst()) {
                // 没过期
                return Values.from(true, "User has been login");
            }
            // 过期了
            // 因为刚才已经自动销毁旧session了, 所以就是没登录
            return Values.from(false, "User is not login");
        } catch (Exception e) {
            e.printStackTrace();
            return Values.from(false, "Query Failed: " + e.getMessage());
        }
    }

    public Values isOutdate(String user_identify) {
        List<Map<String, Object>> queryResult = this.db.instance.query(
            this.db.tableName,
            "session_id, create_time",
            "(session_id = ? or user_email=?)",
            Values.from(user_identify, user_identify)
        );
        System.err.println("Received ID: " + user_identify);
        if (queryResult.isEmpty()) { // 空的也就是没登录, 自然不会过期
            return Values.from(false, "User is not logged");
        }
        Map<String, Object> queryInsideResult = queryResult.getFirst();
        if (System.currentTimeMillis() - (Long)queryInsideResult.get("create_time") > validityPeriod) {
            this.makeExpire(user_identify);
            return Values.from(true, "This Session-ID is Outdate");
        } return Values.from(false, "This Session-ID is valid", queryInsideResult.get("session_id"));
    }

    public Values makeExpire(String session_id) {
        try {
            int affectRows = this.db.instance.delete(
                this.db.tableName,
                "(session_id = ? or user_email=?)",
                Values.from(session_id, session_id)
            );
            if (affectRows > 0) {
                return Values.from(true, "Expire Success");
            }
            return Values.from(false, "Expire Failed");
        } catch (Exception e) {
            return Values.from(false, "Expire Failed By: " + e.getMessage());
        }
    }

    public Values addSession(String user_email) {
        String session_id = CodeGen.generate();
        while (true) {
            List<Map<String, Object>> check = this.db.instance.query(
                this.db.tableName,
                "session_id",
                "session_id = ?",
                Values.from(session_id)
            );
            if (check.isEmpty()) {
                break; }
            session_id = CodeGen.generate();
        }
        try { // outdate check
            Values outdateCheck = this.isOutdate(user_email);
//            System.out.println("VALUES CONTAINS: " + outdateCheck);
            if (!(boolean) outdateCheck.get(0) && !outdateCheck.get(1).equals("User is not logged")) {
                return Values.from(
                    true,
                    "Session-ID is within the validity period",
                    outdateCheck.get(2)
                );
            }
        } catch (Exception e) {
            return Values.from(false, "Failed When Select Login Status");
        }
        try { // Insert Try-Catch
            if (
                this.db.instance.insert(
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
                "session_id = ?",
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
                "user_email = ?",
                Values.from(user_email)
            ) > 0) {
                return Values.from(true, "logout success");
            } else {return Values.from(false, "failed remove session by email"); }
        } catch (Exception e) {
            return Values.from(false, "Error when remove session by email: " + e.getMessage());
        }
    }
}
