package top.starwindv.SQL;


import top.starwindv.Utils.ColumnConfig;
import top.starwindv.Utils.Values;

import java.util.*;
import java.sql.*;
import java.util.stream.Collectors;


@SuppressWarnings("UnusedReturnValue")
public class PostgreSQL {
    /**
     * Fool-proofing PostgreSQL DataBase Manager
     */
    public static class ExtendsForColumnConfig extends ColumnConfig {
        public ExtendsForColumnConfig(Builder builder) {
            super(builder);
        }

        @Override
        public String toSqlFragment() {
            StringBuilder sb = new StringBuilder();
            sb.append(name).append(" ").append(type);

            if (
                length != null
                    &&
                    ("VARCHAR".equalsIgnoreCase(type)
                        || "CHAR".equalsIgnoreCase(type)
                    )
            ) {
                sb.append("(").append(length).append(")");
            }

            if (primaryKey) {
                sb.append(" GENERATED ALWAYS AS IDENTITY ");
            }

            if (autoIncrement) {
                sb.append(" AUTOINCREMENT");
            }

            if (notNull) {
                sb.append(" NOT NULL");
            }

            if (defaultValue != null) {
                sb.append(" DEFAULT ");
                if (defaultValue instanceof String) {
                    sb.append("'").append(defaultValue).append("'");
                } else {
                    sb.append(defaultValue);
                }
            }

            return sb.toString();
        }
    }

    private final String dbUrl;
    private final Properties connectionProps;
    private Connection conn;

    public PostgreSQL(String host, int port, String database, String username, String password) {
        if (host == null || host.trim().isEmpty() || database == null || database.trim().isEmpty()) {
            throw new IllegalArgumentException("Host and database name cannot be empty.");
        }

        this.dbUrl = String.format("jdbc:postgresql://%s:%d/%s",
            host.trim(), port, database.trim());

        this.connectionProps = new Properties();
        if (username != null && !username.trim().isEmpty()) {
            connectionProps.setProperty("user", username.trim());
        }
        if (password != null) {
            connectionProps.setProperty("password", password);
        }

        connectionProps.setProperty("ApplicationName", "PostgreSQL-Utils");
        connectionProps.setProperty("connectTimeout", "10");

        initConnection();
    }

    public PostgreSQL(String host, String database, String username, String password) {
        this(host, 5432, database, username, password);
    }

    public PostgreSQL(String host, String port, String database) {
        this(host, 5432, database, null, null);
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
            this.conn = DriverManager.getConnection(dbUrl, connectionProps);
            this.conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Connect to PostgreSQL failed: ", e);
        }
    }

    public void createTable(String tableName, boolean ifNotExists, List<ExtendsForColumnConfig> columns) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be empty.");
        }
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Columns cannot be empty.");
        }

        StringBuilder createSql = new StringBuilder("CREATE TABLE ");
        if (ifNotExists) {
            createSql.append("IF NOT EXISTS ");
        }

        String fullTableName = processTableName(tableName);
        createSql.append(fullTableName).append(" (");

        String columnSql = columns.stream()
            .map(col -> col.toSqlFragment().replace("AUTOINCREMENT", "SERIAL"))
            .collect(Collectors.joining(", "));
        createSql.append(columnSql).append(");");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createSql.toString());
        } catch (SQLException e) {
            throw new RuntimeException("Create table failed: ", e);
        }
    }

    public void createTable(String tableName, List<ExtendsForColumnConfig> columns) {
        createTable(tableName, true, columns);
    }

    public int insert(String tableName, String columns, Values values) {
        validateTableAndColumns(tableName, columns);
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Values for insert cannot be empty.");
        }

        int columnCount = countColumns(columns);
        if (columnCount != values.size()) {
            throw new IllegalArgumentException(
                String.format(
                    "Column counts: %d, but got: %d",
                    columnCount,
                    values.size()
                )
            );
        }

        String placeholders = generatePlaceholders(values.size());
        String fullTableName = processTableName(tableName);
        String insertSql = String.format("INSERT INTO %s %s VALUES (%s)",
            fullTableName, columns, placeholders);

        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            setPreparedStatementParams(pstmt, values);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Insert failed: ", e);
        }
    }

    public int insertReturningId(String tableName, String columns, Values values, String idColumn) {
        validateTableAndColumns(tableName, columns);
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Values for insert cannot be empty.");
        }

        int columnCount = countColumns(columns);
        if (columnCount != values.size()) {
            throw new IllegalArgumentException(
                String.format(
                    "Column counts: %d, but got: %d",
                    columnCount,
                    values.size()
                )
            );
        }

        String placeholders = generatePlaceholders(values.size());
        String fullTableName = processTableName(tableName);
        String insertSql = String.format("INSERT INTO %s %s VALUES (%s) RETURNING %s",
            fullTableName, columns, placeholders, idColumn);

        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            setPreparedStatementParams(pstmt, values);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return -1;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Insert with RETURNING failed: ", e);
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
        if (whereClause == null || whereClause.trim().isEmpty()) {
            throw new IllegalArgumentException("Update method requires conditions.");
        }
        if (setValues == null || setValues.isEmpty()) {
            throw new IllegalArgumentException("Values for update cannot be empty.");
        }

        String fullTableName = processTableName(tableName);
        String updateSql = String.format(
            "UPDATE %s SET %s WHERE %s",
            fullTableName,
            setClause,
            whereClause
        );

        Values allValues = mergeValues(setValues, whereValues);

        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            setPreparedStatementParams(pstmt, allValues);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update failed: ", e);
        }
    }

    public int delete(String tableName, String whereClause, Values whereValues) {
        validateTableAndColumns(tableName, whereClause);
        if (whereClause == null || whereClause.trim().isEmpty()) {
            throw new IllegalArgumentException("Delete method requires conditions.");
        }

        String fullTableName = processTableName(tableName);
        String deleteSql = String.format("DELETE FROM %s WHERE %s", fullTableName, whereClause);
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            setPreparedStatementParams(pstmt, whereValues);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete failed: ", e);
        }
    }

    public Values exec(String SQL) {
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            return Values.from(pstmt.execute());
        } catch (SQLException e) {
            throw new RuntimeException("Exec failed: ", e);
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
            selectColumns == null || selectColumns.trim().isEmpty()
                ? "*"
                : selectColumns.trim()
        );

        String fullTableName = processTableName(tableName);
        querySql.append(" FROM ").append(fullTableName);

        if (whereClause != null && !whereClause.trim().isEmpty()) {
            querySql.append(" WHERE ").append(whereClause.trim());
        }

        try (PreparedStatement pstmt = conn.prepareStatement(querySql.toString())) {
            if (whereValues != null && !whereValues.isEmpty()) {
                setPreparedStatementParams(pstmt, whereValues);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    result.add(row);
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Select failed: ", e);
        }
    }

    public List<Map<String, Object>> queryWithLimit(
        String tableName,
        String selectColumns,
        String whereClause,
        Values whereValues,
        int limit,
        int offset
    ) {
        validateTableAndColumns(tableName, selectColumns);
        List<Map<String, Object>> result = new ArrayList<>();

        StringBuilder querySql = new StringBuilder("SELECT ");
        querySql.append(
            selectColumns == null || selectColumns.trim().isEmpty()
                ? "*"
                : selectColumns.trim()
        );

        String fullTableName = processTableName(tableName);
        querySql.append(" FROM ").append(fullTableName);

        if (whereClause != null && !whereClause.trim().isEmpty()) {
            querySql.append(" WHERE ").append(whereClause.trim());
        }

        querySql.append(" LIMIT ? OFFSET ?");

        try (PreparedStatement pstmt = conn.prepareStatement(querySql.toString())) {
            int paramIndex = 1;
            if (whereValues != null && !whereValues.isEmpty()) {
                setPreparedStatementParams(pstmt, whereValues);
                paramIndex = whereValues.size() + 1;
            }

            pstmt.setInt(paramIndex++, limit);
            pstmt.setInt(paramIndex, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    result.add(row);
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Select with limit failed: ", e);
        }
    }

    private void validateTableAndColumns(String tableName, String columns) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be empty!");
        }
        if (columns != null && columns.trim().isEmpty()) {
            throw new IllegalArgumentException("Column string cannot be empty (if querying all columns, pass null)!");
        }
    }

    private String processTableName(String tableName) {
        String trimmed = tableName.trim();

        if (trimmed.contains(".")) {
            return trimmed;
        }

        return "public." + trimmed;
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
                case java.time.LocalDate localDate ->
                    pstmt.setDate(paramIndex, java.sql.Date.valueOf(localDate));
                case java.time.LocalDateTime localDateTime ->
                    pstmt.setTimestamp(paramIndex, java.sql.Timestamp.valueOf(localDateTime));
                default -> pstmt.setObject(paramIndex, value);
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

    public void beginTransaction() throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(false);
        }
    }

    public void commit() throws SQLException {
        if (conn != null) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    public void rollback() {
        if (conn != null) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException("Rollback failed: ", e);
            }
        }
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public boolean isConnectionValid() {
        if (conn == null) {
            return false;
        }
        try {
            return !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}