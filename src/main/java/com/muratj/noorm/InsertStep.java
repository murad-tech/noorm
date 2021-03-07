package com.muratj.noorm;

/** INSERT statement started; the first value comes next. */
public interface InsertStep {

    /** Adds the first column/value pair.
     * @throws IllegalArgumentException when the column name is not a safe identifier */
    InsertValueStep value(String column, Object value);
}
