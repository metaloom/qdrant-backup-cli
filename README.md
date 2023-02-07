# Qdrant Backup CLI

The backup CLI tool can be used to download all points / collections and store them locally in a [NDJSON](http://ndjson.org/) format.

## State

Still in development

## Open Tasks

- [ ] Document exit code
- [ ] Add env variable support
- [ ] Test junit 5 / eclipse / native test execution
- [ ] Download snapshots
- [ ] Add sonarcloud checks
- [ ] Remove bogus native-image files
- [x] Use count API to enable progress computation and add ETA
- [x] Add maven enforcer plugin
- [x] Document usage
- [x] Configure logging
- [x] Add additional tests
- [x] Add github actions building
- [x] Get full collection info
- [x] Setup jreleaser native build releases to github
- [x] Check error handling
- [x] Test locking, snapshot handling
- [x] Maybe: Mac, Windows Build?


## Usage Examples

```bash
./qdrant-backup-cli
Usage: qdrant-backup-cli [-v] [-h=<hostname>] [-p=<port>] [COMMAND]
CLI tool for the qdrant vector database
  -h, --hostname=<hostname>
                      Hostname to connect to
                        Default: localhost
  -p, --port=<port>   HTTP port to connect to.
                        Default: 6333
  -v
Commands:
  snapshot, s     Snapshot commands. (e.g. create and restore snapshots)
  collection, co  Collection commands
  cluster, cl     Cluster commands. (e.g. peer management, info)
  point, p        Point commands
  admin, a        Administrative commands. (e.g. lock, unlock)
```

```bash
# Backup all points to file
./qdrant-backup-cli -h localhost -p 6333 point backup -c collection-name backup.json

# Or to stdout
./qdrant-backup-cli -h localhost -p 6333 point backup -c collection-name -

# Backup collections to file
./qdrant-backup-cli -h localhost -p 6333 collection backup backup.json

# Count points
./qdrant-backup-cli -h localhost -p 6333 point count -c collection-name

# Lock / Unlock
./qdrant-backup-cli -h localhost -p 6333 admin lock
./qdrant-backup-cli -h localhost -p 6333 admin unlock
./qdrant-backup-cli -h localhost -p 6333 admin status

# Create / List Restore Snapshots
./qdrant-backup-cli -h localhost -p 6333 snapshot create collection-name

# Use * to create snapshots for all collections
./qdrant-backup-cli -h localhost -p 6333 snapshot create *
./qdrant-backup-cli -h localhost -p 6333 snapshot list collection-name
./qdrant-backup-cli -h localhost -p 6333 snapshot restore collection-name "file:///qdrant/snapshots/test-collection/test-collection-5936205438334902491-2023-02-07-11-34-34.snapshot"

# Cluster Info / Peer Removal
./qdrant-backup-cli -h localhost -p 6333 cluster info
```


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

Reflection is managed via `io.metaloom.qdrant.cli.graalvm.RuntimeReflectionRegistrationFeature` and enabled in `src/main/resources/META-INF/native-image/io.metaloom.qdrant/qdrant-backup-cli/native-image.properties`.

```bash
# Run the native-image-agent using the jar (Useful to find missed reflection invocations)
java -agentlib:native-image-agent=config-merge-dir=src/main/resources/META-INF/native-image/io.metaloom.qdrant/qdrant-backup-cli -jar target/qdrant-backup-cli-0.0.1-SNAPSHOT.jar  p count -c test
```