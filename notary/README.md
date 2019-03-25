# HDS Notary - Server

## Maven

### Build & Run

```sh
cd interface
mvn install

cd notary
mvn compile # Build
mvn exec:java # Run
```

### Generate Eclipse project

```sh
mvn eclipse:eclipse
```

## SQLite

Tutorial: <http://www.sqlitetutorial.net/>

Download and install: <http://www.sqlitetutorial.net/download-install-sqlite/>

SQLite Browser (GUI): <https://sqlitebrowser.org/>

### Create database

Inspired by: <https://alvinalexander.com/android/sqlite-create-table-insert-syntax-examples>

```sh
cd notary
sqlite3 notary.db
.read create.sql
.quit
```

### Populate database

```sh
sqlite3 notary.db
.read populate.sql
.quit
```

### View tables

```sh
.tables
```

### SQLite JDBC

Tutorial: <http://www.sqlitetutorial.net/sqlite-java/sqlite-jdbc-driver/>

