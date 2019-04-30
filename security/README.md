# HDS Notary - Security module

This module has the security classes needed to be used by both the `notary` and the `user`.

## Gradle

pteidlibj.jar: [How to include local .jar as dependency.](https://stackoverflow.com/questions/20700053/how-to-add-local-jar-file-dependency-to-build-gradle-file)

[Build as Java library.](https://guides.gradle.org/building-java-libraries/)

Generated Eclipse project using `gradle eclipse` command, while having `apply plugin: 'eclipse'` inside the `build.gradle` file. Tutorial: <http://www.thejavageek.com/2015/05/22/create-eclipse-project-with-gradle/>

Got `.gitignore` file from <https://www.gitignore.io/api/eclipse> and <https://www.gitignore.io/api/java%2Cgradle%2Cintellij>.

### Build

```sh
gradle build
```

### Run (Generate keys)

```sh
gradle run
cp *.txt ../user/src/main/resources
cp *_public_key.txt ../notary/src/main/resources
```

