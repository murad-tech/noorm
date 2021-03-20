package com.muratj.noorm;

/** A where column is chosen; the next call picks its operator. */
public interface WhereStep {

    /** Adds {@code column = value} with the value escaped through the
     * literal rules (see {@code Values}). */
    ConditionReady eq(Object value);
}
