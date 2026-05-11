# CI and Release

## CI

The project includes GitHub Actions configuration for Ubuntu and macOS with JDK 22.

CI should run:

```bash
mvn clean verify
```

The benchmark suite is not part of the regular CI path because JMH results are noisy and increase build time.

## Local release checks

```bash
mvn clean verify
./scripts/run-example.sh
./scripts/show-build-reports.sh
```

Optional benchmark check:

```bash
./scripts/run-image-benchmarks.sh
```

## Source package

```bash
./scripts/package-source.sh
```

Expected output:

```text
target/cppbridgej-source.zip
```

The source package should not include `target/`, `.DS_Store`, or local IDE files.
