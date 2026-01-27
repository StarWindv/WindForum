package top.starwindv.WindForum.forum.Models;


import top.starwindv.WindForum.forum.Utils.Values;
import top.starwindv.WindForum.SQL.SQLite;
import top.starwindv.WindForum.forum.Utils.ColumnConfig;
import top.starwindv.WindForum.forum.Utils.Permission;
import top.starwindv.WindForum.forum.Utils.Status;

import java.util.*;
import java.sql.*;


@SuppressWarnings("ClassCanBeRecord")
public class Users {
    private final SQLite db;
    
    private static final String TABLE_NAME = "users";
    private static final List<ColumnConfig> TABLE_COLUMNS = Arrays.asList(
        new ColumnConfig.Builder("user_id", "INTEGER")
            .primaryKey()
            .autoIncrement()
            .build(),
        new ColumnConfig.Builder("user_name", "VARCHAR")
            .length(50)
            .notNull()
            .build(),
        new ColumnConfig.Builder("email_str", "VARCHAR")
            .length(100)
            .notNull()
            .build(),
        new ColumnConfig.Builder("passwd_hash", "VARCHAR")
            .length(255)
            .notNull()
            .build(),
        new ColumnConfig.Builder("last_log_ip", "VARCHAR")
            .length(45)
            .build(),
        new ColumnConfig.Builder("register_ip", "VARCHAR")
            .length(45)
            .notNull()
            .build(),
/*
        new ColumnConfig.Builder("reg_time", "TIMESTAMP")
            .notNull()
            .defaultValue("CURRENT_TIMESTAMP")
            .build(),
        new ColumnConfig.Builder("last_log_time", "TIMESTAMP")
            .build(),

        迁移使用 exec 接口实现
 */
        new ColumnConfig.Builder("permission", "INTEGER")
            .notNull()
            .defaultValue(Permission.Normal.code)
            .build(),
        new ColumnConfig.Builder("status", "INTEGER")
            .defaultValue(Status.Active)
            .notNull()
            .build()
    );
    
    public Users(String dbName) {
        this.db = new SQLite(dbName);
        this.init();
    }
    
    private void init() {
        try {
            List<Map<String, Object>> tables = this.db.query(
                "sqlite_master", 
                "name", 
                "type='table' AND name=?", 
                Values.from(TABLE_NAME)
            );
            
            if (tables.isEmpty()) {
                this.db.createTable(TABLE_NAME, TABLE_COLUMNS);

                this.db.exec("CREATE UNIQUE INDEX idx_users_username ON users(user_name)");
                this.db.exec("CREATE UNIQUE INDEX idx_users_email ON users(email_str)");
                this.db.exec("ALTER TABLE users ADD COLUMN reg_time DATETIME NOT NULL DEFAULT (CAST((julianday('now', 'utc') - 2440587.5) * 86400000 + 0.5 AS INTEGER));");
                this.db.exec("ALTER TABLE users ADD COLUMN last_log_time DATETIME NOT NULL DEFAULT (CAST((julianday('now', 'utc') - 2440587.5) * 86400000 + 0.5 AS INTEGER));");
                this.db.exec("""
                    CREATE VIRTUAL TABLE users_fts USING fts5(
                        user_id UNINDEXED,
                        user_name,
                        content='users',
                        content_rowid='user_id'
                    );""");
                this.db.exec("""
                    CREATE TRIGGER users_ai AFTER INSERT ON users BEGIN
                      INSERT INTO users_fts(rowid, user_name) VALUES (new.user_id, new.user_name);
                    END;
                    """);
                this.db.exec("""
                    CREATE TRIGGER users_ad AFTER DELETE ON users BEGIN
                      INSERT INTO users_fts(users_fts, rowid, user_name) VALUES('delete', old.user_id, old.user_name);
                    END;
                    """);
                this.db.exec("""
                    CREATE TRIGGER users_au AFTER UPDATE ON users BEGIN
                      INSERT INTO users_fts(users_fts, rowid, user_name) VALUES('delete', old.user_id, old.user_name);
                      INSERT INTO users_fts(rowid, user_name) VALUES (new.user_id, new.user_name);
                    END;
                    """);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize users table: " + e.getMessage(), e);
        }
    }
    
    public Values register(String userName, String emailStr, String passwdHash, String registerIP) {
        try {
            if (userName == null || userName.trim().isEmpty()) {
                return Values.from(false, "The username cannot be empty");
            }
            if (emailStr == null || emailStr.trim().isEmpty()) {
                return Values.from(false, "Email cannot be empty");
            }
            if (passwdHash == null || passwdHash.trim().isEmpty()) {
                return Values.from(false, "Password cannot be empty");
            }

            userName = userName.trim();
            emailStr = emailStr.trim();
            passwdHash = passwdHash.trim();

            Values unionCheck = this.unionNameEmailCheck(userName, emailStr);
            if (!(boolean) unionCheck.get(0)) {
                return unionCheck;
            }
            
            int affectedRows = this.db.insert(
                TABLE_NAME, 
                "(user_name, email_str, passwd_hash, register_ip, reg_time)",
                Values.from(userName, emailStr, passwdHash, registerIP, System.currentTimeMillis())
            ); // 其实这里要不要手动时间戳都行
            // 主要是因为之前的时间戳实现有问题所以在服务器做了处理
            // 懒得改了
            
            if (affectedRows > 0) {
                List<Map<String, Object>> newUser = this.db.query(
                    TABLE_NAME, 
                    "*", 
                    "user_name = ?", 
                    Values.from(userName)
                );
                
                if (!newUser.isEmpty()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("user_id", newUser.getFirst().get("user_id"));
                    result.put("user_name", userName);
                    result.put("email_str", emailStr);
                    result.put("reg_time", newUser.getFirst().get("reg_time"));
                    return Values.from(true, "Success", result);
                }
            }
            
            return Values.from(false, "Register Failed: Unknown Error");
            
        } catch (Exception e) {
            return Values.from(false, "Register Failed: " + e.getMessage());
        }
    }

    public Values userNameAvailable(String userName) {
        List<Map<String, Object>> existingUser = this.db.query(
            TABLE_NAME,
            "user_id",
            "user_name = ?",
            Values.from(userName)
        );
        if (!existingUser.isEmpty()) {
            return Values.from(false, "The username is already exist");
        } return Values.from(true, "The username is available");
    }

    public Values emailAvailable(String emailStr) {
        List<Map<String, Object>> existingEmail = this.db.query(
            TABLE_NAME,
            "user_id",
            "email_str = ?",
            Values.from(emailStr)
        );
        if (!existingEmail.isEmpty()) {
            return Values.from(false, "The email is already exist");
        } return Values.from(true, "The email is available");
    }

    public Values unionNameEmailCheck(String userName, String email) {
        Values result=this.userNameAvailable(userName);
        if (!(boolean) result.get(0)) {return result;}
        result=this.emailAvailable(email);
        if (!(boolean) result.get(0)) {return result;}
        return Values.from(true, "The username and email are available");
    }

    /**
     * @param identifier 这里指的是用户邮箱或者是用户名
     *                   只要能代表用户身份就可以
     *                   不能是 user_id
     *
     * */
    public Values login(String identifier, String passwdHash, String loginIP) {
        try {
            identifier = identifier.trim();
            passwdHash = passwdHash.trim();

            List<Map<String, Object>> users = this.db.query(
                TABLE_NAME, 
                "*", 
                "(email_str = ? OR user_name = ?)AND passwd_hash = ?",
                Values.from(identifier, identifier, passwdHash)
            );
//            System.err.println(users);
            
            if (users.isEmpty()) {
                return Values.from(false, "Not match");
            }
            
            Map<String, Object> user = users.getFirst();
            
            this.updateLoginInfo(user.get("user_id"), loginIP);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("user_id", user.get("user_id"));
            userInfo.put("user_name", user.get("user_name"));
            userInfo.put("email_str", user.get("email_str"));
            userInfo.put("last_log_ip", user.get("last_log_ip"));
            userInfo.put("register_ip", user.get("register_ip"));
            userInfo.put("reg_time", user.get("reg_time"));
            userInfo.put("last_log_time", user.get("last_log_time"));
            
            return Values.from(true, "", userInfo);
            
        } catch (Exception e) {
            return Values.from(false, "Failed: " + e.getMessage());
        }
    }
    
    private void updateLoginInfo(Object userId, String loginIP) {
        try {
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            this.db.update(
                TABLE_NAME,
                "last_log_ip = ?, last_log_time = ?",
                "user_id = ?",
                Values.from(loginIP, currentTime),
                Values.from(userId)
            );
        } catch (Exception e) {
            System.err.println("Update Login Info Failed: " + e.getMessage());
        }
    }
    
    public Values getUserInfo(Object identity) {
        try {
            List<Map<String, Object>> users = this.db.query(
                TABLE_NAME, 
                "user_id, user_name, email_str, register_ip, reg_time, last_log_ip, last_log_time, permission, status",
                "(user_name = ? OR email_str = ?)",
                Values.from(identity, identity)
            );
            
            if (users.isEmpty()) {
                return Values.from(false, "用户不存在", "", true);
            }
            
            return Values.from(true, "获取成功", users);
            
        } catch (Exception e) {
            return Values.from(false, "获取用户信息失败: " + e.getMessage(), false);
        }
    }
    
    public Values updateUserInfo(Object userId, Map<String, Object> updates) {
        try {
            if (updates == null || updates.isEmpty()) {
                return Values.from(false, "Nothing to update");
            }
            
            List<Map<String, Object>> existing = this.db.query(
                TABLE_NAME, 
                "user_id", 
                "user_id = ?", 
                Values.from(userId)
            );
            if (existing.isEmpty()) {
                return Values.from(false, "用户不存在");
            }
            
            StringBuilder setClause = new StringBuilder();
            List<Object> values = new ArrayList<>();
            
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (!key.equals("user_id") && 
                    !key.equals("passwd_hash") && 
                    !key.equals("reg_time") && 
                    !key.equals("register_ip")) {
                    
                    if (!setClause.isEmpty()) {
                        setClause.append(", ");
                    }
                    setClause.append(key).append(" = ?");
                    values.add(value);
                    
                    if (key.equals("user_name") || key.equals("email_str")) {
                        List<Map<String, Object>> duplicate = this.db.query(
                            TABLE_NAME, 
                            "user_id", 
                            key + " = ? AND user_id != ?", 
                            Values.from(value, userId)
                        );
                        if (!duplicate.isEmpty()) {
                            return Values.from(false, 
                                key.equals("user_name") ? "用户名已存在" : "邮箱已被使用"
                            );
                        }
                    }
                }
            }
            
            if (values.isEmpty()) {
                return Values.from(false, "没有有效的更新字段");
            }
            
            int affectedRows = this.db.update(
                TABLE_NAME,
                setClause.toString(),
                "user_id = ?",
                Values.from(values.toArray()),
                Values.from(userId)
            );
            
            if (affectedRows > 0) {
                return Values.from(true, "更新成功");
            } else {
                return Values.from(false, "更新失败");
            }
            
        } catch (Exception e) {
            return Values.from(false, "更新失败: " + e.getMessage());
        }
    }

    public Values deleteUser(Object userId, String passwdHash) {
        try {
            List<Map<String, Object>> users = this.db.query(
                TABLE_NAME, 
                "user_id", 
                "user_id = ? AND passwd_hash = ?", 
                Values.from(userId, passwdHash)
            );
            
            if (users.isEmpty()) {
                return Values.from(false, "密码错误或用户不存在");
            }
            
            int affectedRows = this.db.delete(
                TABLE_NAME,
                "user_id = ?",
                Values.from(userId)
            );
            
            if (affectedRows > 0) {
                return Values.from(true, "用户删除成功");
            } else {
                return Values.from(false, "用户删除失败");
            }
            
        } catch (Exception e) {
            return Values.from(false, "删除失败: " + e.getMessage());
        }
    }

    public void close() {
        if (db != null) {
            this.db.close();
        }
    }
}
