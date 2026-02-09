package top.starwindv.WindForum.forum.Models;


import top.starwindv.WindForum.forum.DTO.ChannelDTO;
import top.starwindv.WindForum.forum.Server.Forum;
import top.starwindv.WindForum.forum.Utils.ColumnConfig;
import top.starwindv.WindForum.SQL.SQLite;
import top.starwindv.WindForum.forum.Utils.Status;
import top.starwindv.WindForum.forum.Utils.Values;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class Channel {
    private static final List<ColumnConfig> TABLE_COLUMNS = Arrays.asList(
        new ColumnConfig.Builder("channel_id", "INTEGER")
            .autoIncrement()
            .primaryKey()
            .build(),
        new ColumnConfig.Builder("channel_name", "TEXT")
            .notNull()
            .build(),
        new ColumnConfig.Builder("description", "TEXT")
            .notNull()
            .build(),
        new ColumnConfig.Builder("status", "INTEGER")
            .defaultValue(Status.Active)
            .notNull()
            .build()
    );

    public final String dbName;
    private final SQLite db;
    private static final String TABLE_NAME = "channel";

    public Channel(String dbName) {
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
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize users table: " + e.getMessage(), e);
        }
    }

    public Values addChannel(ChannelDTO channelInfo) {
        try {
            int affectedRows = this.db.insert(
                TABLE_NAME,
                "(channel_name, description)",
                Values.from(
                    channelInfo.channel_name(),
                    channelInfo.channel_desc()
                )
            );
            if (affectedRows > 0) {
                return Values.from(true, "Channel added success");
            }
            return Values.from(false, "Unknown Error ");
        } catch (Exception e) {
            Forum.Logger().trace(e);
            return Values.from(false, "Failed to add channel: " + e.getMessage());
        }
    }

    public Values getChannel() {
        try {
            List<Map<String, Object>> channels = this.db.query(
                TABLE_NAME,
                "channel_id, channel_name, description"
            );
            if (channels.isEmpty()) {
                return Values.from(false, "Channel not found");
            }
            return Values.from(true, "", channels);
        } catch (Exception e) {
            Forum.Logger().trace(e);
            return Values.from(false, "Failed to get channel: " + e.getMessage());
        }
    }
}
