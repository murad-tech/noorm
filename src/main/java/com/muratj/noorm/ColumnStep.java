package com.muratj.noorm;

/** At least one column is set; more columns may follow, or the statement can
 * be built or executed. */
public interface ColumnStep {

    /** Adds another column.
     * @throws IllegalArgumentException when the column name is not a safe identifier */
    ColumnStep column(String name, String type);

    /** Renders the statement as a SQL string. */
    String build();

    /** Runs the CREATE TABLE against the connection opened by
     * {@link DB#connect}. */
    void execute();
}
