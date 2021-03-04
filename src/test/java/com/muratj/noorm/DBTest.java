package com.muratj.noorm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Test select/from chain produces SQL. */
@Tag("unit")
class DBTest {

    private static final String URL = "jdbc:h2:mem:noorm-unit;DB_CLOSE_DELAY=-1";

    private DB db;

    @BeforeEach
    void newConnection() {
        db = DB.connect(URL, "sa", "");
    }

    @AfterEach
    void releaseConnection() {
        db.close();
    }

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
