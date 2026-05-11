# Release Checklist

## Local checks

```bash
mvn clean verify
./scripts/run-example.sh
./scripts/show-build-reports.sh
```

## Benchmark check

Run at least one benchmark group before editing benchmark documentation:

```bash
./scripts/run-image-benchmarks.sh
./scripts/run-pipeline-benchmarks.sh
```

Record JVM, compiler, OS, and command line.

## Documentation

- README commands match the current scripts.
- `docs/QUICKSTART.md` works from a clean checkout.
- Benchmark numbers are labelled with environment information.
- `docs/ROADMAP.md` separates implemented and planned features.
- `CHANGELOG.md` has an entry for the release.

## Repository

- CI passes on Ubuntu and macOS.
- License file is present.
- `.gitignore` excludes generated output.
- Source archive excludes `target/` and local system files.

## Packaging

```bash
./scripts/package-source.sh
```

Output:

```text
target/cppbridgej-source.zip
```
