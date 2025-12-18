package top.starwindv;


import java.util.*;
import java.sql.*;


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
        new ColumnConfig.Builder("pswd_hash", "VARCHAR")
            .length(255)
            .notNull()
            .build(),
        new ColumnConfig.Builder("last_log_ip", "VARCHAR")
            .length(45)
            .build(),
        new ColumnConfig.Builder("regist_ip", "VARCHAR")
            .length(45)
            .notNull()
            .build(),
        new ColumnConfig.Builder("reg_time", "TIMESTAMP")
            .notNull()
            .defaultValue("CURRENT_TIMESTAMP")
            .build(),
        new ColumnConfig.Builder("last_log_time", "TIMESTAMP")
            .build(),
        new ColumnConfig.Builder("permission", "INTEGER")
            .notNull()
            .defaultValue(Permission.Normal.code)
            .build(),
        new ColumnConfig.Builder("is_deleted", "INTEGER")
            .defaultValue(Status.Active)
            .notNull()
            .build()
    );
    
    public Users() {
        this.db = new SQLite("db.sqlite");
        init();
    }
    
    private void init() {
        try {
            List<Map<String, Object>> tables = db.query(
                "sqlite_master", 
                "name", 
                "type='table' AND name=?", 
                Values.from(TABLE_NAME)
            );
            
            if (tables.isEmpty()) {
                db.createTable(TABLE_NAME, TABLE_COLUMNS);
                
                db.exec("CREATE UNIQUE INDEX idx_users_username ON users(user_name)");
                db.exec("CREATE UNIQUE INDEX idx_users_email ON users(email_str)");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize users table: " + e.getMessage(), e);
        }
    }
    
    public Values register(String userName, String emailStr, String pswdHash, String registIP) {
        try {
            if (userName == null || userName.trim().isEmpty()) {
                return Values.from(false, "用户名不能为空");
            }
            if (emailStr == null || emailStr.trim().isEmpty()) {
                return Values.from(false, "邮箱不能为空");
            }
            if (pswdHash == null || pswdHash.trim().isEmpty()) {
                return Values.from(false, "密码哈希不能为空");
            }

            userName = userName.trim();
            emailStr = emailStr.trim();
            pswdHash = pswdHash.trim();
            
            List<Map<String, Object>> existingUser = db.query(
                TABLE_NAME, 
                "user_id", 
                "user_name = ?", 
                Values.from(userName)
            );
            if (!existingUser.isEmpty()) {
                return Values.from(false, "用户名已存在");
            }
            
            List<Map<String, Object>> existingEmail = db.query(
                TABLE_NAME, 
                "user_id", 
                "email_str = ?", 
                Values.from(emailStr)
            );
            if (!existingEmail.isEmpty()) {
                return Values.from(false, "邮箱已被注册");
            }
            
            int affectedRows = db.insert(
                TABLE_NAME, 
                "(user_name, email_str, pswd_hash, regist_ip)", 
                Values.from(userName, emailStr, pswdHash, registIP)
            );
            
            if (affectedRows > 0) {
                List<Map<String, Object>> newUser = db.query(
                    TABLE_NAME, 
                    "*", 
                    "user_name = ?", 
                    Values.from(userName)
                );
                
                if (!newUser.isEmpty()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("user_id", newUser.get(0).get("user_id"));
                    result.put("user_name", userName);
                    result.put("email_str", emailStr);
                    result.put("reg_time", newUser.get(0).get("reg_time"));
                    return Values.from(true, "注册成功", result);
                }
            }
            
            return Values.from(false, "注册失败，未知错误");
            
        } catch (Exception e) {
            return Values.from(false, "注册失败: " + e.getMessage());
        }
    }
    
    public Values login(String identifier, String pswdHash, String loginIP) {
        try {
            if (identifier == null || identifier.trim().isEmpty()) {
                return Values.from(false, "用户名/邮箱不能为空");
            }
            if (pswdHash == null || pswdHash.trim().isEmpty()) {
                return Values.from(false, "密码哈希不能为空");
            }
            
            identifier = identifier.trim();
            pswdHash = pswdHash.trim();

            List<Map<String, Object>> users = db.query(
                TABLE_NAME, 
                "*", 
                "(user_name = ? OR email_str = ?) AND pswd_hash = ?", 
                Values.from(identifier, identifier, pswdHash)
            );
            
            if (users.isEmpty()) {
                return Values.from(false, "用户名/邮箱或密码错误");
            }
            
            Map<String, Object> user = users.get(0);
            
            updateLoginInfo(user.get("user_id"), loginIP);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("user_id", user.get("user_id"));
            userInfo.put("user_name", user.get("user_name"));
            userInfo.put("email_str", user.get("email_str"));
            userInfo.put("last_log_ip", user.get("last_log_ip"));
            userInfo.put("regist_ip", user.get("regist_ip"));
            userInfo.put("reg_time", user.get("reg_time"));
            userInfo.put("last_log_time", user.get("last_log_time"));
            
            return Values.from(true, "登录成功", userInfo);
            
        } catch (Exception e) {
            return Values.from(false, "登录失败: " + e.getMessage());
        }
    }
    
    private void updateLoginInfo(Object userId, String loginIP) {
        try {
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            db.update(
                TABLE_NAME,
                "last_log_ip = ?, last_log_time = ?",
                "user_id = ?",
                Values.from(loginIP, currentTime),
                Values.from(userId)
            );
        } catch (Exception e) {
            System.err.println("更新登录信息失败: " + e.getMessage());
        }
    }
    
    public Values getUserInfo(Object userId) {
        try {
            List<Map<String, Object>> users = db.query(
                TABLE_NAME, 
                "user_id, user_name, email_str, regist_ip, reg_time, last_log_ip, last_log_time", 
                "user_id = ?", 
                Values.from(userId)
            );
            
            if (users.isEmpty()) {
                return Values.from(false, "用户不存在");
            }
            
            return Values.from(true, "获取成功", users.get(0));
            
        } catch (Exception e) {
            return Values.from(false, "获取用户信息失败: " + e.getMessage());
        }
    }
    
    public Values updateUserInfo(Object userId, Map<String, Object> updates) {
        try {
            if (updates == null || updates.isEmpty()) {
                return Values.from(false, "没有要更新的信息");
            }
            
            List<Map<String, Object>> existing = db.query(
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
                    !key.equals("pswd_hash") && 
                    !key.equals("reg_time") && 
                    !key.equals("regist_ip")) {
                    
                    if (setClause.length() > 0) {
                        setClause.append(", ");
                    }
                    setClause.append(key).append(" = ?");
                    values.add(value);
                    
                    if (key.equals("user_name") || key.equals("email_str")) {
                        List<Map<String, Object>> duplicate = db.query(
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
            
            int affectedRows = db.update(
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

    public Values deleteUser(Object userId, String pswdHash) {
        try {
            List<Map<String, Object>> users = db.query(
                TABLE_NAME, 
                "user_id", 
                "user_id = ? AND pswd_hash = ?", 
                Values.from(userId, pswdHash)
            );
            
            if (users.isEmpty()) {
                return Values.from(false, "密码错误或用户不存在");
            }
            
            int affectedRows = db.delete(
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
            db.close();
        }
    }
}
