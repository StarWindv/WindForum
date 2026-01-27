package top.starwindv.WindForum.SQL;


import top.starwindv.WindForum.forum.Utils.ColumnConfig;
import top.starwindv.WindForum.forum.Utils.Values;

import java.util.*;
import java.sql.*;
import java.util.stream.Collectors;


@SuppressWarnings("UnusedReturnValue")
public class SQLite {
    /**
     * Fool-proofing Sqlite DataBase Manager
     */
    private final String dbUrl;
    private Connection conn;

    public SQLite(String dbFilePath) {
        if (dbFilePath == null || dbFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("DB Path Cannot be Empty.");
        }
        this.dbUrl = "jdbc:sqlite:" + dbFilePath.trim();
        initConnection();
    }

    public static SQLite inMemory() {
        return new SQLite(":memory:");
    }

    private void initConnection() {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    return;
                }
            } catch (SQLException ignored) {}
        }

        try {
            this.conn = DriverManager.getConnection(dbUrl);
            this.conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Connect DB Failed: ", e);
        }
    }

    public void createTable(String tableName, boolean ifNotExists, List<ColumnConfig> columns) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table Name Cannot be Empty.");
        }
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Columns Cannot be Empty.");
        }

        StringBuilder createSql = new StringBuilder("CREATE TABLE ");
        if (ifNotExists) {
            createSql.append("IF NOT EXISTS ");
        }
        createSql.append(tableName.trim()).append(" (");

        String columnSql = columns.stream()
                .map(ColumnConfig::toSqlFragment)
                .collect(Collectors.joining(", "));
        createSql.append(columnSql).append(");");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createSql.toString());
        } catch (SQLException e) {
            throw new RuntimeException("Create Failed: ", e);
        }
    }

    public void createTable(String tableName, List<ColumnConfig> columns) {
        createTable(tableName, true, columns);
    }

    public int insert(String tableName, String columns, Values values) {
        validateTableAndColumns(tableName, columns);
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Values of Insert Cannot be Empty.");
        }

        int columnCount = countColumns(columns);
        if (columnCount != values.size()) {
            throw new IllegalArgumentException(
                    String.format(
                        "Column Counts: %d, But Got: %d",
                            columnCount,
                            values.size()
                    )
            );
        }

        String placeholders = generatePlaceholders(values.size());
        String insertSql = String.format("INSERT INTO %s %s VALUES (%s)", tableName.trim(), columns, placeholders);

        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            setPreparedStatementParams(pstmt, values);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Insert Failed: ", e);
        }
    }

    public int update(
        String tableName,
        String setClause,
        String whereClause,
        Values setValues,
        Values whereValues
    ) {
        validateTableAndColumns(tableName, setClause);
        if (
            whereClause == null
                ||
                whereClause.trim().isEmpty()
        ) {
            throw new IllegalArgumentException("Update Method Required Conditions.");
        }
        if (setValues == null || setValues.isEmpty()) {
            throw new IllegalArgumentException("Values of Update Cannot be Empty.");
        }

        String updateSql = String.format(
            "UPDATE %s SET %s WHERE %s",
                tableName.trim(),
                setClause,
                whereClause
        );

        Values allValues = mergeValues(setValues, whereValues);

        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            setPreparedStatementParams(pstmt, allValues);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update Failed: ", e);
        }
    }

    public int delete(String tableName, String whereClause, Values whereValues) {
        validateTableAndColumns(tableName, whereClause);
        if (whereClause == null || whereClause.trim().isEmpty()) {
            throw new IllegalArgumentException("Delete Method Required Conditions.");
        }

        String deleteSql = String.format("DELETE FROM %s WHERE %s", tableName.trim(), whereClause);
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            setPreparedStatementParams(pstmt, whereValues);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete Failed: ", e);
        }
    }

    public Values exec(String SQL) {
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            return Values.onlyResult(pstmt.execute());
        } catch (SQLException e) {
//            e.printStackTrace();
            throw new RuntimeException("Exec Failed: ", e);
        }
    }

    public List<Map<String, Object>> query(
        String tableName,
        String selectColumns,
        String whereClause,
        Values whereValues
    ) {
        validateTableAndColumns(tableName, selectColumns);
        List<Map<String, Object>> result = new ArrayList<>();

        StringBuilder querySql = new StringBuilder("SELECT ");
        querySql.append(
            selectColumns == null
                ||
            selectColumns.trim()
                .isEmpty()
                ? "*" : selectColumns.trim()
            );

        querySql.append(" FROM ").append(tableName.trim());
        if (
            whereClause != null
                &&
                !whereClause.trim().isEmpty())
        {
            querySql.append(" WHERE ").append(whereClause.trim());
        }

        try (
            PreparedStatement pstmt = conn.prepareStatement(
                querySql.toString()
            )
        ) {
            if (whereValues != null && !whereValues.isEmpty()) {
                setPreparedStatementParams(pstmt, whereValues);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    result.add(row);
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Select Failed: ", e);
        }
    }

    private void validateTableAndColumns(String tableName, String columns) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("表名不能为空！");
        }
        if (columns != null && columns.trim().isEmpty()) {
            throw new IllegalArgumentException("字段字符串不能为空（若查所有字段请传 null）！");
        }
    }

    private int countColumns(String columns) {
        if (columns == null || columns.trim().isEmpty()) {
            return 0;
        }
        String clean = columns.trim()
                .replace("(", "")
                .replace(")", "")
                .replace(" ", "")
                .replace("?", "")
                .replace("=", "");
        return clean.split(",").length;
    }

    private String generatePlaceholders(int count) {
        if (count <= 0) {
            return "";
        }
        return String.join(", ", Collections.nCopies(count, "?"));
    }

    private void setPreparedStatementParams(PreparedStatement pstmt, Values values) throws SQLException {
        int paramIndex = 1;
        for (Object value : values) {
            switch (value) {
                case null -> pstmt.setNull(paramIndex, Types.NULL);
                case Integer i -> pstmt.setInt(paramIndex, i);
                case Long l -> pstmt.setLong(paramIndex, l);
                case String s -> pstmt.setString(paramIndex, s);
                case Double v -> pstmt.setDouble(paramIndex, v);
                case Float v -> pstmt.setFloat(paramIndex, v);
                case Boolean b -> pstmt.setBoolean(paramIndex, b);
                case java.util.Date date ->
                    pstmt.setTimestamp(paramIndex, new Timestamp(date.getTime()));
                default -> pstmt.setString(paramIndex, value.toString());
            }
            paramIndex++;
        }
    }

    private Values mergeValues(Values v1, Values v2) {
        List<Object> merged = new ArrayList<>();
        if (v1 != null) {
            v1.forEach(merged::add);
        }
        if (v2 != null) {
            v2.forEach(merged::add);
        }
        return Values.from(merged.toArray());
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close();
                } catch (SQLException ignored) {
                }
        }
    }

    public List<Map<String, Object>> query(String tableName, String selectColumns) {
        validateTableAndColumns(tableName, selectColumns);
        List<Map<String, Object>> result = new ArrayList<>();

        String querySql = "SELECT " +
            (selectColumns == null
                ||
                selectColumns.trim().isEmpty()
                ? "*" : selectColumns.trim()) +
            " FROM " + tableName.trim();

        try (
            PreparedStatement pstmt = conn.prepareStatement(querySql)
        ) {
            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    result.add(row);
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Select Failed: ", e);
        }
    }

    public List<Map<String, Object>> query(
        String tableName,
        String selectColumns,
        String orderByColumn,
        boolean isAsc,
        int limit,
        String whereCondition
    ) {
        validateTableAndColumns(tableName, selectColumns);

        if (orderByColumn == null || orderByColumn.trim().isEmpty()) {
            throw new IllegalArgumentException("Order by column cannot be empty");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be a positive integer");
        }

        List<Map<String, Object>> result = new ArrayList<>();

        String querySql = "SELECT " +
            (
                selectColumns == null || selectColumns.trim().isEmpty()
                    ? "*" : selectColumns.trim()
            )
            + " FROM " + tableName.trim()
            + "where " + whereCondition
            + " ORDER BY " + orderByColumn.trim()
            + (isAsc ? " ASC" : " DESC")
            + " LIMIT ?";

        try (PreparedStatement pstmt = conn.prepareStatement(querySql)) {
            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    result.add(row);
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Query top N data failed: ", e);
        }
    }

    public List<Map<String, Object>> queryFromTo(
        String tableName,
        String selectColumns,
        String orderByColumn,
        boolean isAsc,
        int from,
        int to,
        String whereCondition
    ) {
        validateTableAndColumns(tableName, selectColumns);

        if (orderByColumn == null || orderByColumn.trim().isEmpty()) {
            throw new IllegalArgumentException("Order by column cannot be empty");
        }

        if (from < 1) {
            throw new IllegalArgumentException("From must be a positive integer (start from 1)");
        }
        if (to < from) {
            throw new IllegalArgumentException("To cannot be less than from");
        }

        List<Map<String, Object>> result = new ArrayList<>();

        int offset = from - 1;
        int rowCount = to - from + 1;

        String querySql = "SELECT " +
            (selectColumns == null || selectColumns.trim().isEmpty()
                ? "*" : selectColumns.trim())
            + " FROM " + tableName.trim()
            + " " + whereCondition
            + " ORDER BY "
            + orderByColumn.trim()
            + (isAsc ? " ASC" : " DESC")
            + " LIMIT ?, ? ";

        try (PreparedStatement pstmt = conn.prepareStatement(querySql)) {
            pstmt.setInt(1, offset);
            pstmt.setInt(2, rowCount);

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    result.add(row);
                }
            }
            return result;
        } catch (SQLException e) {
//            e.printStackTrace();
            throw new RuntimeException("Query range data failed: ", e);
        }
    }
}
