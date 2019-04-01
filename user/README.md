# HDS Notary - User

### Build & Run

```sh
cd user
mvn install:install-file -Dfile=../pteidlibj-2.0.jar -DgroupId=pt.ulisboa.tecnico -DartifactId=pteidlibj -Dversion=2.0 -Dpackaging=jar
mvn compile # Build
mvn exec:java # Run
```

### Generate Eclipse project

```sh
mvn eclipse:eclipse
```