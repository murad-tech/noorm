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
        return condition("=", value);
    }

    @Override
    public ConditionReady ne(Object value) {
        return condition("<>", value);
    }

    @Override
    public ConditionReady gt(Object value) {
        return condition(">", value);
    }

    @Override
    public ConditionReady gte(Object value) {
        return condition(">=", value);
    }

    @Override
    public ConditionReady lt(Object value) {
        return condition("<", value);
    }

    @Override
    public ConditionReady lte(Object value) {
        return condition("<=", value);
    }

    @Override
    public ConditionReady like(Object value) {
        return condition("LIKE", value);
    }

    @Override
    public ConditionReady notLike(Object value) {
        return condition("NOT LIKE", value);
    }

    @Override
    public ConditionReady in(Object... values) {
        return inCondition(true, values);
    }

    @Override
    public ConditionReady notIn(Object... values) {
        return inCondition(false, values);
    }

    @Override
    public ConditionReady between(Object low, Object high) {
        return betweenCondition(true, low, high);
    }

    @Override
    public ConditionReady notBetween(Object low, Object high) {
        return betweenCondition(false, low, high);
    }

    @Override
    public ConditionReady isNull() {
        return finish(pendingColumn + " IS NULL");
    }

    @Override
    public ConditionReady isNotNull() {
        return finish(pendingColumn + " IS NOT NULL");
    }

    private ConditionReady condition(String operator, Object value) {
        return finish(pendingColumn + " " + operator + " " + Values.literal(value));
    }

    private ConditionReady inCondition(boolean positive, Object[] values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("IN needs at least one value");
        }
        StringBuilder literals = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                literals.append(", ");
            }
            literals.append(Values.literal(values[i]));
        }
        String operator = positive ? "IN" : "NOT IN";
        return finish(pendingColumn + " " + operator + " (" + literals + ")");
    }

    private ConditionReady betweenCondition(boolean positive, Object low, Object high) {
        String keyword = positive ? "BETWEEN" : "NOT BETWEEN";
        return finish(pendingColumn + " " + keyword + " " + Values.literal(low)
                + " AND " + Values.literal(high));
    }

    private ConditionReady finish(String condition) {
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
