package com.muratj.noorm;

import java.sql.ResultSet;

/** LIMIT is set; OFFSET may follow, or the select can be built or executed. */
public interface LimitReady {

    /** Adds OFFSET (single-use).
     * @param n number of rows to skip, must be non-negative */
    OffsetReady offset(int n);

    /** Renders the select as a SQL string. */
    String build();

    /** Runs the built SELECT and returns the live ResultSet; read it, then
     * close the {@link DB} that owns the connection. */
    ResultSet execute();
}
