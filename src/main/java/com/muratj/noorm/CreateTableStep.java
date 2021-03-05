package com.muratj.noorm;

/** CREATE TABLE statement started; the first column comes next. */
public interface CreateTableStep {

    /** Adds the first column: identifier plus a SQL type fragment like VARCHAR(50).
     * @throws IllegalArgumentException when the column name is not a safe identifier */
    ColumnStep column(String name, String type);
}
