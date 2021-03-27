package com.muratj.noorm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Immutable clause state; the fluent step interfaces gate which methods are
 * legal at each point. */
final class Query implements SelectStep, FromStep, WhereStep, ConditionReady,
        OrderReady, LimitReady, OffsetReady {

    private final Connection connection;
    private final String projection;
    private final String table;
    private final String conditions;
    /** Set while a where/and column waits for its operator. */
    private final String pendingColumn;
    /** Joined sort keys; null until the first orderBy. */
    private final String ordering;
    private final Integer limit;
    private final Integer offset;

    private Query(Connection connection,
                  String projection,
                  String table,
                  String conditions,
                  String pendingColumn,
                  String ordering,
                  Integer limit,
                  Integer offset) {
        this.connection = connection;               // SQL connection
        this.projection = projection;               // list of columns or "*"
        this.table = table;
        this.conditions = conditions;
        this.pendingColumn = pendingColumn;
        this.ordering = ordering;
        this.limit = limit;
        this.offset = offset;
    }

    static Query withColumns(String projection, Connection connection) {
        return new Query(connection,
                projection,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private Query keep(String table, String conditions, String pendingColumn) {
        return new Query(connection,
                projection,
                table,
                conditions,
                pendingColumn,
                ordering,
                limit,
                offset);
    }

    private Query keepConditions(String pendingColumn) {
        return keep(table, conditions, pendingColumn);
    }

    @Override
    public FromStep from(String table) {
        return keep(Identifiers.validate(table), conditions, pendingColumn);
    }

    @Override
    public WhereStep where(String column) {
        return keepConditions(Identifiers.validate(column));
    }

    @Override
    public WhereStep and(String column) {
        return where(column);
    }

    private ConditionReady finish(String condition) {
        String joined = conditions == null ? condition : conditions + " AND " + condition;
        return keep(table, joined, null);
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

    @Override
    public OrderReady orderBy(String column, Order order) {
        String key = Identifiers.validate(column) + " " + order;
        String joined = ordering == null ? key : ordering + ", " + key;
        return new Query(connection, projection, table, conditions, null, joined, limit, offset);
    }

    @Override
    public LimitReady limit(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("LIMIT must be non-negative");
        }
        return new Query(connection, projection, table, conditions, null, ordering, n, offset);
    }

    @Override
    public OffsetReady offset(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("OFFSET must be non-negative");
        }
        return new Query(connection, projection, table, conditions, null, ordering, limit, n);
    }

    @Override
    public String build() {
        if (pendingColumn != null) {
            throw new IllegalStateException("WHERE has a column but no condition");
        }
        StringBuilder sql = new StringBuilder("SELECT ").append(projection).append(" FROM ").append(table);
        if (conditions != null) {
            sql.append(" WHERE ").append(conditions);
        }
        if (ordering != null) {
            sql.append(" ORDER BY ").append(ordering);
        }
        if (limit != null) {
            sql.append(" LIMIT ").append(limit);
            if (offset != null) {
                sql.append(" OFFSET ").append(offset);
            }
        }
        return sql.toString();
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
