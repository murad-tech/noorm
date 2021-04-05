# NoORM

Typesafe, lightweight SQL query builder for Java (JDBC). A small, zero-dependency library that builds, escapes, and optionally executes plain SQL.

## Why

Traditional ORMs hide SQL behind entities and session machinery. Convenient until
you need a plain query, then everything gets hard. Raw SQL strings pasted into
JDBC go the other way: nothing protects you, and a typo or an unescaped quote
is a runtime surprise.

This project started as a SQL string builder: a fluent chain that renders the
exact SQL you had in mind. Testing it changed the plan. Every test needed its
own database wiring and connection handling, and that is exactly the setup
every user would have repeated by hand, so the execution step became part of
the library.

What noorm gives you:

- The compiler enforces clause order: `.from()` before `.select()` does not
  compile, and an unfinished `WHERE` cannot be followed by `ORDER BY`.
- Identifiers are validated and literals are escaped
  (`O'Brien` → `'O''Brien'`, `null` → `NULL`); there is no SQL injection path
  through the public API.
- `.build()` returns the string you would type into a database console.
  Read it, log it, run it anywhere.
- `DB.connect(...)` opens the JDBC connection you give it; `.execute()` runs
  the query and hands back a live `ResultSet`. No mapping layer, no magic.
- Zero dependencies beyond the JDK.

## Install

**Prerequsites:** Maven, Java 11+. 

The library is not on a remote repository (mvnrepository) yet, so install it
into your local `~/.m2` from source:

```bash
git clone git@github.com:murad-tech/noorm.git && cd noorm
mvn clean install
```

Then you in any project you can import it by adding dependency:

```xml
<dependency>
  <groupId>com.muratj</groupId>
  <artifactId>noorm</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>

<!-- replace dependency below with your preffered DB JDBC driver -->
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <version>1.4.200</version>
  <scope>test</scope>
</dependency>
```

## Usage

```java
import com.muratj.noorm.DB;
import com.muratj.noorm.Order;

try (DB db = DB.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")) {
    db.createTableIfNotExists("users")
      .column("name", "VARCHAR(50)")
      .column("age", "INTEGER")
      .execute();

    db.insertInto("users")
        .value("name", "john")
        .value("age", 30)
        .execute();

    String sql = db.select("name")
                   .from("users")
                   .where("age").gte(18)
                   .orderBy("age", Order.DESC)
                   .limit(10)
                   .build();
    // → SELECT name FROM users WHERE age >= 18 ORDER BY age DESC LIMIT 10

    try (ResultSet rs = db.select("name")
                            .from("users")
                            .where("age").gte(30)
                            .execute()) {
        while (rs.next()) {
            System.out.println(rs.getString("name"));   // output: john
        }
    }
}
```

## Coverage

- SELECT
- WHERE with equals/not-equals/comparison/LIKE/IN/BETWEEN/NULL check operators
- ORDER BY
- LIMIT
- DISTINCT
- COUNT
- CREATE TABLE (incl. `IF NOT EXISTS`)
- INSERT

On the list: ALTER and DROP table, OR groupings, typed row-mapping layer, GROUP BY + HAVING, DELETE and UPDATE record.

## Development

After cloning, run the one-line setup to enable the pre-commit hook that runs
unit tests on every commit:

```bash
./setup.sh
```

