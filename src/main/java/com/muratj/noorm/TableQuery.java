package com.muratj.noorm;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Immutable CREATE TABLE state; columns join until the statement is forced. */
final class TableQuery implements CreateTableStep, ColumnStep {

    private final Connection connection;
    private final String table;
    private final String columns;
    private final boolean ifNotExists;

    private TableQuery(Connection connection, String table, String columns, boolean ifNotExists) {
        this.connection = connection;
        this.table = table;
        this.columns = columns;
        this.ifNotExists = ifNotExists;
    }

    static TableQuery create(String table, Connection connection, boolean ifNotExists) {
        return new TableQuery(connection, table, null, ifNotExists);
    }

    private TableQuery withColumn(String column) {
        return new TableQuery(connection, table, columns == null ? column : columns + ", " + column, ifNotExists);
    }

    @Override
    public ColumnStep column(String name, String type) {
        return withColumn(Identifiers.validate(name) + " " + type);
    }

    @Override
    public String build() {
        if (columns == null) {
            throw new IllegalStateException("CREATE TABLE needs at least one column");
        }
        return "CREATE TABLE " + (ifNotExists ? "IF NOT EXISTS " : "") + table + " (" + columns + ")";
    }

    @Override
    public void execute() {
        if (!ifNotExists && tableExists()) {
            throw new IllegalStateException("Table already exists: " + table
                    + " (use createTableIfNotExists to tolerate that)");
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute(build());
        } catch (SQLException e) {
            throw new IllegalStateException("Statement failed: " + build(), e);
        }
    }

    private boolean tableExists() {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            // some drivers keep identifiers in another case; check both spellings
            if (matches(meta, table)) {
                return true;
            }
            return matches(meta, table.toUpperCase());
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot check whether table exists: " + table, e);
        }
    }

    private boolean matches(DatabaseMetaData meta, String name) throws SQLException {
        try (ResultSet rs = meta.getTables(null, null, name, null)) {
            return rs.next();
        }
    }
}
