package top.starwindv;


class UGTable {
    public static final String TableName = "UserGroup";
    public static final 
        List<ColumnConfig> Table = Arrays.asList(
            new ColumnConfig.Builder("user_id", "INTEGER")
                .primaryKey()
                .autoIncrement()
                .build(),
            new ColumnConfig.Builder("permision", "INTEGER")
                .notNull()
                .length(20)
                .defaultValue(Permision.Normal)
                .build()
    );
}


public class UserGroup {
    public final String dbFilePath;

    private SQLite SQL;
    
    public UserGroup(String dbFilePath) {
        this.dbFilePath = dbFilePath;
        this.initialize();
    }

    private void initialize() {
        this.SQL = new SQLite(this.dbFilePath);
    }

}
