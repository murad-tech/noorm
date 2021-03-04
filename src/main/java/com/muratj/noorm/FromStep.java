package com.muratj.noorm;

/** The FROM clause is set; the chain can render SQL or run it. */
public interface FromStep {

    /** Renders the query as a SQL string. */
    String build();

    /** Runs the built SELECT and returns the live ResultSet; read it, then
     * close the {@link Sql} that owns the connection. */
    java.sql.ResultSet execute();
}
