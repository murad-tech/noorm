package com.muratj.noorm;

import java.sql.ResultSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** orderBy/limit/offset produce clause suffix SQL and order rows. */
@Tag("unit")
class OrderTest {

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
    void orderByAloneAfterFrom() {
        String sql = db.select("*").from("users")
                .orderBy("age", Order.DESC)
                .build();
        assertEquals("SELECT * FROM users ORDER BY age DESC", sql);
    }

    @Test
    void orderByAfterWhere() {
        String sql = db.select("*").from("users")
                .where("name").eq("john")
                .orderBy("age", Order.DESC)
                .build();
        assertEquals("SELECT * FROM users WHERE name = 'john' ORDER BY age DESC", sql);
    }

    @Test
    void orderByIsRepeatable() {
        String sql = db.select("*").from("users")
                .orderBy("age", Order.DESC)
                .orderBy("name", Order.ASC)
                .build();
        assertEquals("SELECT * FROM users ORDER BY age DESC, name ASC", sql);
    }

    @Test
    void limitAfterConditions() {
        String sql = db.select("*").from("users")
                .where("name").eq("john")
                .limit(10)
                .build();
        assertEquals("SELECT * FROM users WHERE name = 'john' LIMIT 10", sql);
    }

    @Test
    void limitAloneAfterOrderBy() {
        String sql = db.select("*").from("users")
                .orderBy("age", Order.DESC)
                .limit(10)
                .build();
        assertEquals("SELECT * FROM users ORDER BY age DESC LIMIT 10", sql);
    }

    @Test
    void fullChainWithOffset() {
        String sql = db.select("*").from("users")
                .where("name").eq("john")
                .orderBy("age", Order.DESC)
                .limit(10)
                .offset(5)
                .build();
        assertEquals("SELECT * FROM users WHERE name = 'john' ORDER BY age DESC LIMIT 10 OFFSET 5", sql);
    }

    @Test
    void hostileOrderColumnRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> db.select("*").from("users")
                        .orderBy("age; --", Order.DESC));
    }

    @Test
    void executesOrderAndLimit() throws Exception {
        db.createTable("order_test")
                .column("name", "VARCHAR(50)")
                .column("age", "INTEGER")
                .execute();
        db.insertInto("order_test").value("name", "john").value("age", 30).execute();
        db.insertInto("order_test").value("name", "ann").value("age", 42).execute();
        db.insertInto("order_test").value("name", "fred").value("age", 22).execute();

        try (ResultSet rs = db.select("name").from("order_test")
                .orderBy("age", Order.ASC)
                .limit(2)
                .execute()) {
            assertEquals(true, rs.next());
            assertEquals("fred", rs.getString("name"));
            assertEquals(true, rs.next());
            assertEquals("john", rs.getString("name"));
            assertEquals(false, rs.next());
        }
    }
}
