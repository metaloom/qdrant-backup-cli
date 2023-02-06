# Qdrant Backup CLI

The backup CLI tool can be used to download all points / collections and store them locally in a [NDJSON](http://ndjson.org/) format.

## State

Still in development

## Open Tasks

* Add maven enforcer plugin
* Configure logging
* Add additional tests
* Add github actions building
* Add sonarcloud checks
* Setup jreleaser native build releases to github
* Remove bogus native-image files
* Use count API to enable progress computation and add ETA
* Check error handling
* Test locking, snapshot handling
* Maybe: Mac, Windows Build?

## Building

Requires JDK 17

```bash
mvn clean package -DskipTests
```

## Building (native-image)

Requires: `graalvm-ce-java17-22.3.1`

```bash
mvn clean package -DskipTests -Pnative
```

## Update reflect-config.json (optional)

Reflection is managed via `io.metaloom.qdrant.cli.graalvm.RuntimeReflectionRegistrationFeature` and enabled in `src/main/resources/META-INF/native-image/io.metaloom.qdrant/qdrant-cli/native-image.properties`.

```bash
# Run the native-image-agent using the jar (Useful to find missed reflection invocations)
java -agentlib:native-image-agent=config-merge-dir=src/main/resources/META-INF/native-image/io.metaloom.qdrant/qdrant-cli -jar target/qdrant-cli-0.0.1-SNAPSHOT.jar  backup points -c test -h localhost -p 6333 test.json
```