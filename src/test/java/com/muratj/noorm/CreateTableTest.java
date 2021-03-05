package com.muratj.noorm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** createTable chain produces DDL SQL and runs it. */
@Tag("unit")
class CreateTableTest {

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
    void buildsCreateTableStatement() {
        String sql = db.createTable("users")
                .column("name", "VARCHAR(50)")
                .column("age", "INTEGER")
                .build();
        assertEquals("CREATE TABLE users (name VARCHAR(50), age INTEGER)", sql);
    }

    @Test
    void singleColumnHasNoSeparator() {
        String sql = db.createTable("users").column("id", "INTEGER").build();
        assertEquals("CREATE TABLE users (id INTEGER)", sql);
    }

    @Test
    void hostileTableNameRejected() {
        assertThrows(IllegalArgumentException.class, () -> db.createTable("users; DROP TABLE users"));
    }

    @Test
    void hostileColumnNameRejected() {
        assertThrows(IllegalArgumentException.class, () -> db.createTable("users").column("name; --", "VARCHAR(50)"));
    }

    @Test
    void zeroColumnsCannotBuild() {
        // CreateTableStep does not expose build(); guard holds for misuse
        assertThrows(IllegalStateException.class, () -> ((TableQuery) db.createTable("users")).build());
    }

    @Test
    void executesAgainstRealDatabase() {
        db.createTable("users_ddl_test")
                .column("name", "VARCHAR(50)")
                .column("age", "INTEGER")
                .execute();
    }
}
