package top.starwindv.Utils;


public class ColumnConfig {
    private final String name;
    private final String type;
    private Integer length;
    private Object defaultValue;
    private boolean notNull = false;
    private boolean primaryKey = false; 
    private boolean autoIncrement = false;

    private ColumnConfig(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.length = builder.length;
        this.defaultValue = builder.defaultValue;
        this.notNull = builder.notNull;
        this.primaryKey = builder.primaryKey;
        this.autoIncrement = builder.autoIncrement;

        if (
            autoIncrement 
                && 
                (
                    !primaryKey 
                        || 
                        !"INT".equalsIgnoreCase(type) 
                            && 
                            !"INTEGER".equalsIgnoreCase(type)
                    )
        ) {
            throw new IllegalArgumentException(
                "The auto-increment field must be the primary key of type INT/INTEGER"
            );
        }
    }

    public static class Builder {
        private final String name;
        private final String type;
        private Integer length;
        private Object defaultValue;
        private boolean notNull;
        private boolean primaryKey;
        private boolean autoIncrement;

        public Builder(String name, String type) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Column's Name Cannot be Empty.");
            }
            if (type == null || type.trim().isEmpty()) {
                throw new IllegalArgumentException("Column Type Cannot be Empty.");
            }
            this.name = name.trim();
            this.type = type.trim().toUpperCase();
        }

        public Builder length(int length) {
            if (length <= 0) {
                throw new IllegalArgumentException("Column Length Cannot Less than Zero.");
            }
            this.length = length;
            return this;
        }

        public Builder defaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder notNull() {
            this.notNull = true;
            return this;
        }

        public Builder primaryKey() {
            this.primaryKey = true;
            return this;
        }

        public Builder autoIncrement() {
            this.autoIncrement = true;
            return this;
        }

        public ColumnConfig build() {
            return new ColumnConfig(this);
        }
    }

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
            sb.append(" PRIMARY KEY");
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
