package com.muratj.noorm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** insertInto chain produces INSERT SQL, escapes literals, runs it. */
@Tag("unit")
class InsertTest {

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
    void buildsInsertStatement() {
        String sql = db.insertInto("users")
                .value("name", "john")
                .value("age", 30)
                .build();
        assertEquals("INSERT INTO users (name, age) VALUES ('john', 30)", sql);
    }

    @Test
    void singleValueHasNoSeparator() {
        String sql = db.insertInto("users")
                .value("name", "john")
                .build();
        assertEquals("INSERT INTO users (name) VALUES ('john')", sql);
    }

    @Test
    void quoteInStringIsDoubled() {
        String sql = db.insertInto("users")
                .value("name", "O'Brien")
                .build();
        assertEquals("INSERT INTO users (name) VALUES ('O''Brien')", sql);
    }

    @Test
    void nullBecomesUnquotedNull() {
        String sql = db.insertInto("users")
                .value("name", null)
                .build();
        assertEquals("INSERT INTO users (name) VALUES (NULL)", sql);
    }

    @Test
    void numberIsUnquoted() {
        String sql = db.insertInto("users")
                .value("age", 30)
                .build();
        assertEquals("INSERT INTO users (age) VALUES (30)", sql);
    }

    @Test
    void hostileTableNameRejected() {
        assertThrows(IllegalArgumentException.class, () -> db.insertInto("users; DROP TABLE users"));
    }

    @Test
    void hostileColumnNameRejected() {
        assertThrows(IllegalArgumentException.class, () -> db.insertInto("users").value("name; --", "john"));
    }

    @Test
    void zeroValuesCannotBuild() {
        // InsertStep does not expose build(); guard holds for misuse
        assertThrows(IllegalStateException.class, () -> ((InsertQuery) db.insertInto("users")).build());
    }

    @Test
    void executesAndReturnsRowCount() {
        db.createTable("insert_ddl_test")
                .column("name", "VARCHAR(50)")
                .execute();
        int rows = db.insertInto("insert_ddl_test")
                .value("name", "john")
                .execute();
        assertEquals(1, rows);
    }
}
