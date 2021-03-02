package com.muratj.noorm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqlTest {

    private final Sql db = new Sql();

    @Test
    void buildsSelectFromQuery() {
        String sql = db.select("*").from("users").build();
        assertEquals("SELECT * FROM users", sql);
    }

    @Test
    void emptySelectDefaultsToAllColumns() {
        String sql = db.select().from("users").build();
        assertEquals("SELECT * FROM users", sql);
    }

    @Test
    void buildsQueryWithMultipleColumns() {
        String sql = db.select("id", "name").from("orders").build();
        assertEquals("SELECT id, name FROM orders", sql);
    }

    @Test
    void rejectsHostileTableIdentifier() {
        assertThrows(IllegalArgumentException.class, () -> db.select("*").from("users; DROP TABLE users"));
    }
}
