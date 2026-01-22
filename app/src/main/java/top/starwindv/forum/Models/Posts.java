package top.starwindv.forum.Models;


import top.starwindv.forum.DTO.PostDTO;
import top.starwindv.forum.Utils.ColumnConfig;
import top.starwindv.forum.SQL.SQLite;
import top.starwindv.forum.Utils.Status;
import top.starwindv.forum.Utils.Values;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class Posts {
    private static final List<ColumnConfig> TABLE_COLUMNS = Arrays.asList(
        new ColumnConfig.Builder("post_id", "INTEGER")
            .autoIncrement()
            .primaryKey()
            .build(),
        new ColumnConfig.Builder("email_str", "INTEGER")
            .notNull()
            .build(),
        new ColumnConfig.Builder("title", "VARCHAR")
            .notNull()
            .build(),
        new ColumnConfig.Builder("content", "TEXT")
            .notNull()
            .build(),
        new ColumnConfig.Builder("status", "INTEGER")
            .defaultValue(Status.Active)
            .notNull()
            .build()
    );

    public final String dbName;
    private final SQLite db;
    private static final String TABLE_NAME = "posts";

    public Posts(String dbName) {
        this.dbName = dbName;
        this.db = new SQLite(dbName);
        this.init();
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
                db.exec("CREATE TABLE IF NOT EXISTS "
                    + TABLE_NAME
                    + " ("
                    + "    post_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "    email_str VARCHAR(100) NOT NULL,"
                    + "    title VARCHAR NOT NULL,"
                    + "    content TEXT NOT NULL,"
                    + "    status INTEGER NOT NULL DEFAULT "
                    + Status.Active
                    + ", "
                    + "CONSTRAINT fk_email FOREIGN KEY (email_str) REFERENCES users(email_str)"
                    + ");");
                db.exec("CREATE INDEX idx_posts_email ON " + TABLE_NAME + "(email_str)");
                db.exec("ALTER TABLE " + TABLE_NAME + " ADD COLUMN create_time DATETIME NOT NULL DEFAULT (CAST((julianday('now', 'utc') - 2440587.5) * 86400000 + 0.5 AS INTEGER));");
                db.exec("CREATE INDEX idx_posts_create ON " + TABLE_NAME + "(create_time)");
                db.exec(
                    "ALTER TABLE "
                        + TABLE_NAME
                        + " ADD COLUMN last_update_time DATETIME NOT NULL DEFAULT"
                        + " (CAST((julianday('now', 'utc') - 2440587.5) * 86400000 + 0.5 AS INTEGER));"
                );
                db.exec("CREATE INDEX idx_posts_update ON " + TABLE_NAME + "(last_update_time)");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize users table: " + e.getMessage(), e);
        }
    }

    public Values addPost(PostDTO PostInformation) {
        try {
            int affectedRows = this.db.insert(
                TABLE_NAME,
                "(email_str, title, content)",
                Values.from(PostInformation.userEmail(), PostInformation.title(), PostInformation.content())
            );
            if (affectedRows > 0) {
                return Values.from(true, "Post added success");
            }
            return Values.from(false, "Unknown Error ");
        } catch (Exception e) {
//            e.printStackTrace();
            return Values.from(false, "Failed to add post: " + e.getMessage());
        }
    }

    public Values getPost(String postId) {
        try {
            List<Map<String, Object>> posts = this.db.query(
                TABLE_NAME,
                "post_id, email_str, title, content, status, create_time, last_update_time",
                "post_id=?",
                Values.from(postId)
            );
            if (posts.isEmpty()) {
                return Values.from(false, "Post not found");
            }
            return Values.from(true, "", posts.getFirst());
        } catch (Exception e) {
            return Values.from(false, "Failed to get post: " + e.getMessage());
        }
    }

    public Values AllPostOfOneUser(String user_email) {
        try {
            List<Map<String, Object>> posts = this.db.query(
                TABLE_NAME,
                "post_id, email_str, title, content, status, create_time, last_update_time",
                "email_str=?",
                Values.from(user_email)
            );
            if (posts.isEmpty()) {
                return Values.from(false, "Post not found");
            }

            return Values.from(true, "", posts);
        } catch (Exception e) {
            return Values.from(false, "Failed to get post: " + e.getMessage());
        }
    }

    public Values getAllPosts(boolean isAsc, int limit) {
        try {
            List<Map<String, Object>> posts = this.db.query(
                TABLE_NAME,
                "post_id, email_str, title, content, status, create_time, last_update_time",
                "create_time",
                isAsc,
                limit

            );
            if (posts.isEmpty())
                return Values.from(false, "Post not found");

            return Values.from(true, "", posts);
        } catch (Exception e) {
            return Values.from(false, "Failed to get post: " + e.getMessage());
        }
    }

    public Values getFromTo(String orderBy, boolean isAsc, int from, int to) {
        try {
            List<Map<String, Object>> posts = this.db.queryFromTo(
                TABLE_NAME,
                "post_id, email_str, title, content, status, create_time, last_update_time",
                orderBy,
                isAsc,
                from,
                to,
                "where status="+Status.Active
            );
            if (posts.isEmpty())
                return Values.from(false, "Post not found");

            return Values.from(true, "", posts);
        } catch (Exception e) {
            return Values.from(false, "Failed to get post: " + e.getMessage());
        }
    }
}
