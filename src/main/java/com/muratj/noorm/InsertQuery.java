package com.muratj.noorm;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Immutable INSERT state; column/value pairs join until the row is forced. */
final class InsertQuery implements InsertStep, InsertValueStep {

    private final Connection connection;
    private final String table;
    private final String columns;
    private final String values;

    private InsertQuery(Connection connection, String table, String columns, String values) {
        this.connection = connection;
        this.table = table;
        this.columns = columns;
        this.values = values;
    }

    static InsertQuery create(String table, Connection connection) {
        return new InsertQuery(connection, table, null, null);
    }

    private InsertQuery withValue(String column, String literal) {
        String newColumns = columns == null ? column : columns + ", " + column;
        String newValues = values == null ? literal : values + ", " + literal;
        return new InsertQuery(connection, table, newColumns, newValues);
    }

    @Override
    public InsertValueStep value(String column, Object value) {
        return withValue(Identifiers.validate(column), Values.literal(value));
    }

    @Override
    public String build() {
        if (columns == null) {
            throw new IllegalStateException("INSERT needs at least one value");
        }
        return "INSERT INTO " + table + " (" + columns + ") VALUES (" + values + ")";
    }

    @Override
    public int execute() {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(build());
        } catch (SQLException e) {
            throw new IllegalStateException("Statement failed: " + build(), e);
        }
    }
}
