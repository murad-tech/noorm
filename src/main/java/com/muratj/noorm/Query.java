package com.muratj.noorm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Immutable clause state; validation scope grows with later tasks. */
final class Query implements SelectStep, FromStep, WhereStep, ConditionReady {

    private final Connection connection;
    private final String projection;
    private final String table;
    private final String conditions;
    /** Set while a where/and column waits for its operator. */
    private final String pendingColumn;

    private Query(Connection connection, String projection, String table,
                  String conditions, String pendingColumn) {
        this.connection = connection;
        this.projection = projection;
        this.table = table;
        this.conditions = conditions;
        this.pendingColumn = pendingColumn;
    }

    static Query withColumns(String projection, Connection connection) {
        return new Query(connection, projection, null, null, null);
    }

    private Query copy(String table, String conditions, String pendingColumn) {
        return new Query(connection, projection, table, conditions, pendingColumn);
    }

    @Override
    public FromStep from(String table) {
        return copy(Identifiers.validate(table), conditions, pendingColumn);
    }

    @Override
    public WhereStep where(String column) {
        return copy(table, conditions, Identifiers.validate(column));
    }

    @Override
    public WhereStep and(String column) {
        return where(column);
    }

    @Override
    public ConditionReady eq(Object value) {
        String condition = pendingColumn + " = " + Values.literal(value);
        String joined = conditions == null ? condition : conditions + " AND " + condition;
        return copy(table, joined, null);
    }

    @Override
    public String build() {
        if (pendingColumn != null) {
            throw new IllegalStateException("WHERE has a column but no condition");
        }
        String sql = "SELECT " + projection + " FROM " + table;
        if (conditions != null) {
            sql += " WHERE " + conditions;
        }
        return sql;
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
