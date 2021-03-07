package com.muratj.noorm;

/** At least one value is set; more values may follow, or the statement can
 * be built or executed. */
public interface InsertValueStep {

    /** Adds another column/value pair.
     * @throws IllegalArgumentException when the column name is not a safe identifier */
    InsertValueStep value(String column, Object value);

    /** Renders the statement as a SQL string. */
    String build();

    /** Runs the INSERT and returns the number of affected rows. */
    int execute();
}
