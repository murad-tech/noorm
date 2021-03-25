package com.muratj.noorm;

/** A where column is chosen; the next call picks its operator. All operators
 * escape values through the literal rules (see {@code Values}). */
public interface WhereStep {

    /** Adds {@code column = value}. */
    ConditionReady eq(Object value);

    /** Adds {@code column <> value}. */
    ConditionReady ne(Object value);

    /** Adds {@code column > value}. */
    ConditionReady gt(Object value);

    /** Adds {@code column >= value}. */
    ConditionReady gte(Object value);

    /** Adds {@code column < value}. */
    ConditionReady lt(Object value);

    /** Adds {@code column <= value}. */
    ConditionReady lte(Object value);

    /** Adds {@code column LIKE pattern}. */
    ConditionReady like(Object value);

    /** Adds {@code column NOT LIKE pattern}. */
    ConditionReady notLike(Object value);

    /** Adds {@code column IN (v1, v2, ...)}; needs at least one value. */
    ConditionReady in(Object... values);

    /** Adds {@code column NOT IN (v1, v2, ...)}; needs at least one value. */
    ConditionReady notIn(Object... values);

    /** Adds {@code column BETWEEN a AND b}. */
    ConditionReady between(Object low, Object high);

    /** Adds {@code column NOT BETWEEN a AND b}. */
    ConditionReady notBetween(Object low, Object high);

    /** Adds {@code column IS NULL}. */
    ConditionReady isNull();

    /** Adds {@code column IS NOT NULL}. */
    ConditionReady isNotNull();
}
