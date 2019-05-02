# HDS Notary - User

## Gradle

Generic tutorial: <https://spring.io/guides/gs/gradle/>

pteidlibj.jar: [How to include local .jar as dependency.](https://stackoverflow.com/questions/20700053/how-to-add-local-jar-file-dependency-to-build-gradle-file)

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
source setEnvVar.sh
gradle run
```
