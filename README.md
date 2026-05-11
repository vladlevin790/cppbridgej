# CppBridgeJ

CppBridgeJ is a small Java framework for calling C++ functions through the Java Foreign Function & Memory API.

It provides:

- annotations for mapping Java interfaces to exported C functions;
- a Maven plugin that compiles `src/main/cpp/*.cpp` into a platform shared library;
- heap-array and off-heap array mapping for primitive data;
- runtime binding diagnostics;
- build-time symbol validation;
- JMH benchmarks for comparing Java and native implementations.

The project targets coarse-grained native kernels: image buffers, numeric transforms, audio buffers, matrix operations, and simulation steps. It is not intended for replacing small Java methods with native calls.

## Requirements

- JDK 22 or newer
- Maven 3.9 or newer
- C++ compiler:
  - macOS: `clang++`
  - Linux: `g++`
  - Windows: MSVC `cl` from a Developer Command Prompt

Check the active JDK:

```bash
java -version
mvn -v
```

On macOS, select JDK 22 if Maven uses another JDK:

```bash
/usr/libexec/java_home -V
export JAVA_HOME=$(/usr/libexec/java_home -v 22)
mvn -v
```

## Quick start

From the repository root:

```bash
mvn clean install
./scripts/show-build-reports.sh
./scripts/run-example.sh
```

Run all tests:

```bash
./scripts/run-tests.sh
```

Equivalent Maven command:

```bash
mvn clean verify
```

Run benchmarks:

```bash
./scripts/run-array-benchmarks.sh
./scripts/run-pipeline-benchmarks.sh
./scripts/run-image-benchmarks.sh
```

Equivalent manual flow:

```bash
mvn -pl cppbridge-benchmark -am clean package
cd cppbridge-benchmark
java --enable-native-access=ALL-UNNAMED -jar target/benchmarks.jar ArrayBenchmarks
```

Shell scripts in this repository are optional helpers. The project can be built and tested with Maven commands only.

Generate JavaDoc:

```bash
./scripts/generate-javadocs.sh
```

Equivalent Maven command:

```bash
mvn -pl cppbridge-core,cppbridge-maven-plugin javadoc:javadoc
```

## Minimal example

C++ source in `src/main/cpp/fastmath.cpp`:

```cpp
#ifdef _WIN32
#define CPPBRIDGE_EXPORT extern "C" __declspec(dllexport)
#else
#define CPPBRIDGE_EXPORT extern "C"
#endif

CPPBRIDGE_EXPORT int sum_int(int a, int b) {
    return a + b;
}

CPPBRIDGE_EXPORT double average_double(double* values, int length) {
    if (length <= 0) {
        return 0.0;
    }

    double total = 0.0;
    for (int i = 0; i < length; i++) {
        total += values[i];
    }
    return total / length;
}
```

Java interface:

```java
@CppModule(libraryName = "fastmath")
public interface FastMath {
    @CppFunction("sum_int")
    int sum(int a, int b);

    @CppFunction("average_double")
    double average(@CppArray(ArrayDirection.IN) double[] values);
}
```

Java usage:

```java
FastMath math = CppBridge.load(FastMath.class);

int sum = math.sum(10, 20);
double average = math.average(new double[] {10.0, 20.0, 30.0});
```

## Maven plugin

```xml
<plugin>
    <groupId>dev.cppbridge</groupId>
    <artifactId>cppbridge-maven-plugin</artifactId>
    <version>${project.version}</version>
    <configuration>
        <libraryName>fastmath</libraryName>
        <optimizationLevel>O3</optimizationLevel>
        <cppStandard>c++20</cppStandard>
        <expectedSymbols>
            <expectedSymbol>sum_int</expectedSymbol>
            <expectedSymbol>average_double</expectedSymbol>
        </expectedSymbols>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>compile-cpp</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

The plugin writes native build diagnostics to:

```text
target/cppbridge/native-build-report.txt
target/cppbridge/exported-symbols.txt
target/cppbridge/missing-symbols.txt
```

If `expectedSymbols` contains a symbol that is not exported by the shared library, the build fails by default.

## Array mapping

Primitive Java arrays are passed as pointer plus length:

```text
double[] -> double*, int length
byte[]   -> uint8_t*, int length
```

A Java method:

```java
@CppFunction("average_double")
double average(@CppArray(ArrayDirection.IN) double[] values);
```

expects this C++ function:

```cpp
CPPBRIDGE_EXPORT double average_double(double* values, int length);
```

`@CppArray` controls copy direction:

- `IN`: copy Java data to native memory before the call;
- `OUT`: copy native memory back to Java after the call;
- `IN_OUT`: copy both ways.

Default direction is `IN_OUT`.

## NativeArray API

For repeated calls over the same large buffer, use managed off-heap arrays:

```java
try (NativeDoubleArray values = NativeDoubleArray.copyOf(heapValues)) {
    math.heavyTransform(values);
    math.multiplyEachNative(values, 2.0);
    double[] result = values.toArray();
}
```

Supported wrappers:

- `NativeByteArray`
- `NativeIntArray`
- `NativeLongArray`
- `NativeFloatArray`
- `NativeDoubleArray`

These wrappers avoid copying the same array into native memory on every call.

## Binding report

Runtime inspection is available before executing native calls:

```java
BindingReport report = CppBridge.inspect(FastMath.class);
System.out.println(report.toText());
```

Example output:

```text
CppBridgeJ binding report
API: dev.cppbridge.example.FastMath
Mode: NATIVE
Library exists: true
Healthy: true

- double average(double[])
  -> double average_double(double*, int length)
  symbol: average_double
  status: OK
```

## Benchmark notes

The repository includes JMH benchmarks under `cppbridge-benchmark`. Current local measurements are recorded in `docs/BENCHMARK_RESULTS_MACBOOK_JDK22.md`.

Representative results from macOS with JDK 22.0.2:

```text
heavyTransform, 1_000_000 double values
Java loop:             9.327 ms/op
C++ NativeArray FFM:   5.059 ms/op
```

```text
image fused pipeline, 3_000_000 bytes
Java fused pipeline:        1.485 ms/op
C++ NativeByteArray FFM:    0.450 ms/op
```

These numbers describe one machine and one benchmark configuration. Re-run JMH for the target environment before using the data for engineering decisions.

## Scope and limitations

Implemented:

- native shared-library backend;
- Java FFM invocation;
- Java dynamic proxy binding;
- primitive scalar types: `byte`, `int`, `long`, `float`, `double`, `void`;
- primitive arrays and managed native arrays;
- Maven-based C++ compilation;
- exported-symbol validation.

Not implemented yet:

- structs and custom memory layouts;
- strings;
- callbacks;
- native exceptions;
- Gradle plugin;
- WASM backend.

## Documentation

Generated JavaDoc is written to:

```text
cppbridge-core/target/reports/apidocs/index.html
cppbridge-maven-plugin/target/reports/apidocs/index.html
```

Project documentation:

- `docs/QUICKSTART.md`
- `docs/USER_GUIDE.md`
- `docs/API_REFERENCE.md`
- `docs/ARCHITECTURE.md`
- `docs/BUILD_TIME_VALIDATION.md`
- `docs/BINDING_REPORT.md`
- `docs/BENCHMARK_RESULTS_MACBOOK_JDK22.md`
- `docs/KNOWN_LIMITATIONS.md`
- `docs/SECURITY_MODEL.md`
- `docs/ROADMAP.md`

## Troubleshooting

### Maven tries to download `dev.cppbridge` artifacts

Run from the repository root first:

```bash
mvn clean install
./scripts/run-example.sh
```

This installs the local reactor modules into the local Maven repository before running module-specific commands.

### FFM native-access warning

Use:

```bash
--enable-native-access=ALL-UNNAMED
```

The scripts already set this flag where it is needed.

## JavaDoc

Generate JavaDoc:

```bash
./scripts/generate-javadocs.sh
```

Equivalent Maven commands:

```bash
mvn -pl cppbridge-core -DskipTests org.apache.maven.plugins:maven-javadoc-plugin:3.10.1:javadoc
mvn -pl cppbridge-maven-plugin -DskipTests org.apache.maven.plugins:maven-javadoc-plugin:3.10.1:javadoc
```

The script verifies that these files exist:

```text
cppbridge-core/target/reports/apidocs/index.html
cppbridge-maven-plugin/target/reports/apidocs/index.html
```
