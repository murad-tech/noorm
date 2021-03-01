package com.muratj.noorm;

/** The FROM clause is set; the chain is complete and can render SQL. */
public interface FromStep {

    /** Renders the query as a SQL string. */
    String build();
}
