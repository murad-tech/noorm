package com.muratj.noorm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Immutable clause state; validation scope grows with later tasks. */
final class Query implements SelectStep, FromStep {

    private final Connection connection;
    private final String projection;
    private final String table;

    private Query(Connection connection, String projection, String table) {
        this.connection = connection;
        this.projection = projection;
        this.table = table;
    }

    static Query withColumns(String projection, Connection connection) {
        return new Query(connection, projection, null);
    }

    @Override
    public FromStep from(String table) {
        return new Query(connection, projection, Identifiers.validate(table));
    }

    @Override
    public String build() {
        return "SELECT " + projection + " FROM " + table;
    }

    @Override
    public ResultSet execute() {
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(build());
        } catch (SQLException e) {
            throw new IllegalStateException("Query failed: " + build(), e);
        }
    }
}
