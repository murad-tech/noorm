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
    void neGtGteLtLteProduceSymbols() {
        assertEquals("SELECT * FROM users WHERE name <> 'john'",
                db.select("*").from("users").where("name").ne("john").build());
        assertEquals("SELECT * FROM users WHERE age > 30",
                db.select("*").from("users").where("age").gt(30).build());
        assertEquals("SELECT * FROM users WHERE age >= 30",
                db.select("*").from("users").where("age").gte(30).build());
        assertEquals("SELECT * FROM users WHERE age < 30",
                db.select("*").from("users").where("age").lt(30).build());
        assertEquals("SELECT * FROM users WHERE age <= 30",
                db.select("*").from("users").where("age").lte(30).build());
    }

    @Test
    void likeEscapesQuote() {
        assertEquals("SELECT * FROM users WHERE name LIKE 'jo%'",
                db.select("*").from("users").where("name").like("jo%").build());
        assertEquals("SELECT * FROM users WHERE name LIKE 'O''Brien'",
                db.select("*").from("users").where("name").like("O'Brien").build());
    }

    @Test
    void notLikeProducesNotLike() {
        String sql = db.select("*").from("users").where("name").notLike("jo%").build();
        assertEquals("SELECT * FROM users WHERE name NOT LIKE 'jo%'", sql);
    }

    @Test
    void inAcceptsMultipleValues() {
        String sql = db.select("*").from("users").where("name").in("john", "ann").build();
        assertEquals("SELECT * FROM users WHERE name IN ('john', 'ann')", sql);
    }

    @Test
    void inWithNumbersUnquoted() {
        String sql = db.select("*").from("users").where("age").in(30, 42).build();
        assertEquals("SELECT * FROM users WHERE age IN (30, 42)", sql);
    }

    @Test
    void notInProducesNot() {
        String sql = db.select("*").from("users").where("name").notIn("john").build();
        assertEquals("SELECT * FROM users WHERE name NOT IN ('john')", sql);
    }

    @Test
    void emptyInRejected() {
        assertThrows(IllegalArgumentException.class, () -> db.select("*").from("users").where("name").in());
    }

    @Test
    void betweenProducesBetween() {
        String sql = db.select("*").from("users").where("age").between(20, 40).build();
        assertEquals("SELECT * FROM users WHERE age BETWEEN 20 AND 40", sql);
    }

    @Test
    void notBetweenProducesNot() {
        String sql = db.select("*").from("users").where("age").notBetween(20, 40).build();
        assertEquals("SELECT * FROM users WHERE age NOT BETWEEN 20 AND 40", sql);
    }

    @Test
    void isNullAndIsNotNull() {
        assertEquals("SELECT * FROM users WHERE name IS NULL",
                db.select("*").from("users").where("name").isNull().build());
        assertEquals("SELECT * FROM users WHERE name IS NOT NULL",
                db.select("*").from("users").where("name").isNotNull().build());
    }

    @Test
    void operatorsMixWithAnd() {
        String sql = db.select("*").from("users")
                .where("name").notIn("john")
                .and("age").between(20, 40)
                .and("city").isNotNull()
                .build();
        assertEquals("SELECT * FROM users WHERE name NOT IN ('john') AND age BETWEEN 20 AND 40 AND city IS NOT NULL", sql);
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
