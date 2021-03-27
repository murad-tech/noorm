package com.muratj.noorm;

import java.sql.ResultSet;

/** ORDER BY is set; more sort keys may follow, or the select can be limited,
 * built or executed. Conditions are final at this point — the fluent chain
 * no longer offers {@code where}/{@code and}. */
public interface OrderReady {

    /** Adds another sort key.
     * @throws IllegalArgumentException when the column name is not a safe identifier */
    OrderReady orderBy(String column, Order order);

    /** Adds LIMIT (single-use).
     * @param n maximum number of rows, must be non-negative */
    LimitReady limit(int n);

    /** Renders the select as a SQL string. */
    String build();

    /** Runs the built SELECT and returns the live ResultSet; read it, then
     * close the {@link DB} that owns the connection. */
    ResultSet execute();
}
