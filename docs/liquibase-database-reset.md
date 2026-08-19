# Liquibase database reset

Use this procedure only when the current development data can be discarded.
Do not enable `spring.liquibase.drop-first` in application configuration.

## 1. Stop the application

No application instance should be connected while the schema is being reset.

## 2. Optional backup

Export the `gamesphere_db` database from the database client if any current data
may be needed later.

## 3. Reset the public schema

Connect to `gamesphere_db` as its owner and execute:

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO gamesphere;
GRANT ALL ON SCHEMA public TO public;
```

This removes the old Hibernate-created tables together with any old Liquibase
metadata. It does not delete the PostgreSQL database itself.

## 4. Start the application

On startup Liquibase reads:

```text
classpath:db/changelog/db.changelog-master.yaml
```

It creates the application schema and then Hibernate validates that the schema
matches the entity model. Hibernate no longer creates or changes tables.

## 5. Verify Liquibase

```sql
SELECT id, author, filename, dateexecuted, orderexecuted
FROM databasechangelog
ORDER BY orderexecuted;

SELECT id, locked, lockgranted, lockedby
FROM databasechangeloglock;
```

Six rows should exist in `databasechangelog`, and the lock row should have
`locked = false`.

The seeded roles should also be present:

```sql
SELECT id, name
FROM roles
ORDER BY id;
```

Expected role names:

```text
ROLE_USER
ROLE_SELLER
ROLE_ADMIN
```