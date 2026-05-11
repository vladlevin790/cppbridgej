# Contributing

## Development requirements

- JDK 22+
- Maven 3.9+
- `clang++`, `g++`, or MSVC `cl`

## Local validation

```bash
mvn clean verify
./scripts/run-example.sh
./scripts/show-build-reports.sh
```

Run benchmark groups only when working on runtime performance:

```bash
./scripts/run-array-benchmarks.sh
./scripts/run-pipeline-benchmarks.sh
./scripts/run-image-benchmarks.sh
```

## Native ABI rules

Export functions with a C ABI:

```cpp
#ifdef _WIN32
#define CPPBRIDGE_EXPORT extern "C" __declspec(dllexport)
#else
#define CPPBRIDGE_EXPORT extern "C"
#endif
```

Do not expose C++ classes, templates, exceptions, or STL types through the public native boundary. Use primitive scalars and primitive arrays.

## Pull requests

A change that adds or changes runtime behavior should include at least one of:

- unit test;
- integration test;
- benchmark update;
- documentation update.

Performance notes should include hardware, JVM version, compiler version, benchmark command, and JMH output.
