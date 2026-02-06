package top.starwindv.WindForum.forum.Models;


import top.starwindv.WindForum.forum.DTO.PostDTO;
import top.starwindv.WindForum.forum.Forum;
import top.starwindv.WindForum.SQL.SQLite;
import top.starwindv.WindForum.forum.Utils.Status;
import top.starwindv.WindForum.forum.Utils.Values;

import java.util.List;
import java.util.Map;


public class Posts {
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
                db.exec("CREATE TABLE IF NOT EXISTS "
                    + TABLE_NAME
                    + " ("
                    + "    post_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "    email_str VARCHAR(100) NOT NULL,"
                    + "    title VARCHAR NOT NULL,"
                    + "    content TEXT NOT NULL,"
                    + "    channel_id TEXT NOT NULL,"
                    + "    status INTEGER NOT NULL DEFAULT "
                    + Status.Active
                    + ", "
                    + " last_update_time DATETIME NOT NULL DEFAULT"
                    + "(CAST((julianday('now', 'utc') - 2440587.5) * 86400000 + 0.5 AS INTEGER)),"
                    + " create_time DATETIME NOT NULL DEFAULT"
                    + "(CAST((julianday('now', 'utc') - 2440587.5) * 86400000 + 0.5 AS INTEGER)),"
                    + "CONSTRAINT fk_email FOREIGN KEY (email_str) REFERENCES users(email_str),"
                    + "FOREIGN KEY (channel_id) REFERENCES channel(channel_id)"
                    + ");");
                db.exec("CREATE INDEX idx_posts_email ON " + TABLE_NAME + "(email_str)");

                db.exec("CREATE INDEX idx_posts_create ON " + TABLE_NAME + "(create_time)");
                db.exec("CREATE INDEX idx_posts_channel ON " + TABLE_NAME + "(channel_id)");

                db.exec("CREATE INDEX idx_posts_update ON " + TABLE_NAME + "(last_update_time)");
                db.exec("""
                    CREATE TRIGGER trigger_posts_update_time
                    BEFORE UPDATE ON posts
                    FOR EACH ROW
                    BEGIN
                        UPDATE posts
                        SET last_update_time = CAST((julianday('now', 'utc') - 2440587.5) * 86400000 + 0.5 AS INTEGER)
                        WHERE rowid = NEW.rowid;
                    END;
                    """);
            }
        } catch (Exception e) {
            Forum.Logger().trace(e);
            throw new RuntimeException("Failed to initialize users table: " + e.getMessage(), e);
        }
    }

    public Values addPost(PostDTO PostInformation) {
        try {
            int affectedRows = this.db.insert(
                TABLE_NAME,
                "(email_str, title, content, channel_id)",
                Values.from(
                    PostInformation.userEmail(),
                    PostInformation.title(),
                    PostInformation.content(),
                    PostInformation.channel_id()
                )
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
            Forum.Logger().trace(e);
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
            Forum.Logger().trace(e);
            return Values.from(false, "Failed to get post: " + e.getMessage());
        }
    }

    public Values LatestPostOfOneUser(String user_email) {
        try {
            List<Map<String, Object>> posts = this.db.query(
                TABLE_NAME,
                "post_id, email_str, title, content, status, create_time, last_update_time",
                "email_str=? ORDER BY create_time DESC LIMIT 1",
                Values.from(user_email)
            );

            if (posts.isEmpty()) {
                return Values.from(false, "Post not found");
            }

            return Values.from(true, "", posts);
        } catch (Exception e) {
            Forum.Logger().trace(e);
            return Values.from(false, "Failed to get post: " + e.getMessage());
        }
    }

    public Values getAllPosts(boolean isAsc, int limit) {
        try {
            List<Map<String, Object>> posts = this.db.query(
                TABLE_NAME,
                "post_id, email_str, title, content, status, create_time, last_update_time, channel_id",
                "create_time",
                isAsc,
                limit,
                "status=" + Status.Active
            );
            if (posts.isEmpty())
                return Values.from(false, "Post not found");

            return Values.from(true, "", posts);
        } catch (Exception e) {
            Forum.Logger().trace(e);
            return Values.from(false, "Failed to get post: " + e.getMessage());
        }
    }

    public Values getFromTo(String orderBy, boolean isAsc, int from, int to, String channel_id) {
        try {
            String selectColumns = "post_id, "
                + TABLE_NAME
                + ".email_str, title, content, "
                + TABLE_NAME + ".status, create_time, last_update_time, u.user_name";
            List<Map<String, Object>> posts = this.db.queryFromTo(
                TABLE_NAME,
                selectColumns,
                orderBy,
                isAsc,
                from,
                to,
                "Inner Join users u ON " + TABLE_NAME + ".email_str=u.email_str ",
                "where " + TABLE_NAME + ".status="+Status.Active
                + String.format(" AND channel_id=%s", channel_id)
            );
            if (posts.isEmpty())
                return Values.from(false, "Post not found", "", true);
            Forum.Logger().debug(posts);
            return Values.from(true, "", posts);
        } catch (Exception e) {
            Forum.Logger().trace(e);
            return Values.from(false, "Failed to get post: " + e.getMessage(), "", false);
        }
    }
}
