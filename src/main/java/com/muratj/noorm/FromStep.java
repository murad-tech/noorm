package com.muratj.noorm;

/** The FROM clause is set; the chain can render SQL, run it, or add a WHERE. */
public interface FromStep {

    /** Renders the query as a SQL string. */
    String build();

    /** Runs the built SELECT and returns the live ResultSet; read it, then
     * close the {@link DB} that owns the connection. */
    java.sql.ResultSet execute();

    /** Adds a WHERE condition on the given column; combine more with
     * {@code and(column)}.
     * @throws IllegalArgumentException when the column name is not a safe identifier */
    WhereStep where(String column);

    /** Adds an ORDER BY key; repeatable via {@link OrderReady#orderBy}.
     * @throws IllegalArgumentException when the column name is not a safe identifier */
    OrderReady orderBy(String column, Order order);
}
