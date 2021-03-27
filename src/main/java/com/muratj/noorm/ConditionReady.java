package com.muratj.noorm;

import java.sql.ResultSet;

/** A condition is set; a new condition may start, or the select can be
 * built or executed. Conditions combine with implicit AND. */
public interface ConditionReady {

    WhereStep and(String column);

    /** Adds an ORDER BY key; repeatable via {@link OrderReady#orderBy}.
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
