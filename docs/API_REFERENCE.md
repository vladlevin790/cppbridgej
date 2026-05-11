# API Reference

## `CppBridge`

```java
T CppBridge.load(Class<T> api)
T CppBridge.load(Class<T> api, Path libraryPath)
BindingReport CppBridge.inspect(Class<?> api)
```

`load` creates a dynamic proxy for a Java interface annotated with `@CppModule`.

`inspect` returns runtime binding diagnostics without invoking native functions.

## `@CppModule`

```java
@CppModule(libraryName = "fastmath")
public interface FastMath {
}
```

Main attributes:

- `libraryName`: platform-neutral library name;
- `libraryPath`: optional explicit path;
- `mode`: currently `NATIVE`.

## `@CppFunction`

```java
@CppFunction("average_double")
double average(double[] values);
```

Maps a Java interface method to an exported native symbol.

## `@CppArray`

```java
@CppArray(ArrayDirection.IN)
```

Controls heap-array copy direction.

Values:

- `IN`
- `OUT`
- `IN_OUT`

## Supported scalar types

```text
byte
int
long
float
double
void
```

## Supported heap arrays

```text
byte[]
int[]
long[]
float[]
double[]
```

Heap arrays are mapped to pointer plus `int length`.

## Managed native arrays

```text
NativeByteArray
NativeIntArray
NativeLongArray
NativeFloatArray
NativeDoubleArray
```

Common methods:

```java
static NativeDoubleArray allocate(int length)
static NativeDoubleArray copyOf(double[] values)
int length()
MemorySegment segment()
double[] toArray()
void close()
```

## Diagnostics

```java
BindingReport report = CppBridge.inspect(FastMath.class);
boolean healthy = report.isHealthy();
String text = report.toText();
```

Entry status values:

```text
OK
MISSING_LIBRARY
MISSING_SYMBOL
UNSUPPORTED_SIGNATURE
INSPECTION_FAILED
```
