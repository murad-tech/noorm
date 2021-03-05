package com.muratj.noorm;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Immutable CREATE TABLE state; columns join until the statement is forced. */
final class TableQuery implements CreateTableStep, ColumnStep {

    private final Connection connection;
    private final String table;
    private final String columns;

    private TableQuery(Connection connection, String table, String columns) {
        this.connection = connection;
        this.table = table;
        this.columns = columns;
    }

    static TableQuery create(String table, Connection connection) {
        return new TableQuery(connection, table, null);
    }

    private TableQuery withColumn(String column) {
        return new TableQuery(connection, table, columns == null ? column : columns + ", " + column);
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
        return "CREATE TABLE " + table + " (" + columns + ")";
    }

    @Override
    public void execute() {
        try (Statement statement = connection.createStatement()) {
            statement.execute(build());
        } catch (SQLException e) {
            throw new IllegalStateException("Statement failed: " + build(), e);
        }
    }
}
