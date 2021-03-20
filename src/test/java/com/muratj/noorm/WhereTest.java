package com.muratj.noorm;

import java.sql.ResultSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** where/and chain produces WHERE SQL and filters rows. */
@Tag("unit")
class WhereTest {

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
    void buildsWhereEq() {
        String sql = db.select("*").from("users")
                .where("name").eq("john")
                .build();
        assertEquals("SELECT * FROM users WHERE name = 'john'", sql);
    }

    @Test
    void eqWithNumberIsUnquoted() {
        String sql = db.select("*").from("users")
                .where("age").eq(30)
                .build();
        assertEquals("SELECT * FROM users WHERE age = 30", sql);
    }

    @Test
    void eqEscapesQuote() {
        String sql = db.select("*").from("users")
                .where("name").eq("O'Brien")
                .build();
        assertEquals("SELECT * FROM users WHERE name = 'O''Brien'", sql);
    }

    @Test
    void andCombinesConditions() {
        String sql = db.select("*").from("users")
                .where("name").eq("john")
                .and("age").eq(30)
                .build();
        assertEquals("SELECT * FROM users WHERE name = 'john' AND age = 30", sql);
    }

    @Test
    void threeConditionsChain() {
        String sql = db.select("id", "name").from("users")
                .where("name").eq("john")
                .and("age").eq(30)
                .and("city").eq("chicago")
                .build();
        assertEquals("SELECT id, name FROM users WHERE name = 'john' AND age = 30 AND city = 'chicago'", sql);
    }

    @Test
    void hostileWhereColumnRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                db.select("*").from("users")
                        .where("name; --"));
    }

    @Test
    void hostileAndColumnRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                db.select("*").from("users")
                        .where("name").eq("john")
                        .and("age; DROP").eq(30));
    }

    @Test
    void executesWhereAndFiltersRows() throws Exception {
        db.createTable("where_test")
                .column("name", "VARCHAR(50)")
                .column("age", "INTEGER")
                .execute();
        db.insertInto("where_test")
                .value("name", "john")
                .value("age", 30)
                .execute();
        db.insertInto("where_test")
                .value("name", "ann")
                .value("age", 42)
                .execute();

        try (ResultSet rs = db.select("name").from("where_test")
                .where("name").eq("john")
                .execute()) {
            assertTrue(rs.next());
            assertEquals("john", rs.getString("name"));
            assertFalse(rs.next());
        }
    }
}
