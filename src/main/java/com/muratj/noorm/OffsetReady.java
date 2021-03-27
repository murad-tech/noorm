package com.muratj.noorm;

import java.sql.ResultSet;

/** The select is complete: build or execute. */
public interface OffsetReady {

    /** Renders the select as a SQL string. */
    String build();

    /** Runs the built SELECT and returns the live ResultSet; read it, then
     * close the {@link DB} that owns the connection. */
    ResultSet execute();
}
