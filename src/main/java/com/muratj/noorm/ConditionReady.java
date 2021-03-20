package com.muratj.noorm;

import java.sql.ResultSet;

/** A condition is set; a new condition may start, or the select can be
 * built or executed. Conditions combine with implicit AND. */
public interface ConditionReady {

    WhereStep and(String column);

    /** Renders the select as a SQL string. */
    String build();

    /** Runs the built SELECT and returns the live ResultSet; read it, then
     * close the {@link DB} that owns the connection. */
    ResultSet execute();
}
