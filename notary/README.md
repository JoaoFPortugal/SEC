# HDS Notary - Server

## Gradle

Generic tutorial: <https://spring.io/guides/gs/gradle/>

Tutorial [SQLite JDBC driver with Gradle](https://stackoverflow.com/questions/50377264/using-sqlite-jdbc-driver-in-a-gradle-java-project).

pteidlibj-2.0.jar: [How to include local .jar as dependency.](https://stackoverflow.com/questions/20700053/how-to-add-local-jar-file-dependency-to-build-gradle-file)

[Include local library](https://developer.android.com/studio/build/dependencies) (security)

### Setup notes

Generated Eclipse project using `gradle eclipse` command, while having `apply plugin: 'eclipse'` inside the `build.gradle` file. Tutorial: <http://www.thejavageek.com/2015/05/22/create-eclipse-project-with-gradle/>

Got `.gitignore` file from <https://www.gitignore.io/api/eclipse> and <https://www.gitignore.io/api/java%2Cgradle%2Cintellij>.

### Build

```sh
gradle build
```

### Run

```sh
gradle run
```



## Maven

### Build & Run

```sh
cd security
mvn install
cd notary
mvn install:install-file -Dfile=../pteidlibj-2.0.jar -DgroupId=pt.ulisboa.tecnico -DartifactId=pteidlibj -Dversion=2.0 -Dpackaging=jar
mvn compile # Build
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:absolute/path/to/lib/linux/
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

