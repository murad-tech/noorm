package com.muratj.noorm;

/** A SELECT statement has been opened; only the name of the queried table is legal next. */
public interface SelectStep {

    /** Names the queried table. */
    FromStep from(String table);
}
