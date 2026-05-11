# User Guide

## Model

```text
Java interface
  -> annotations
  -> Maven plugin compiles C++
  -> shared library
  -> FFM MethodHandle
  -> Java proxy
```

## Module mapping

```java
@CppModule(libraryName = "fastmath")
public interface FastMath {
}
```

Platform output paths:

```text
macOS   target/native/libfastmath.dylib
Linux   target/native/libfastmath.so
Windows target/native/fastmath.dll
```

## Function mapping

```java
@CppFunction("sum_int")
int sum(int a, int b);
```

Native side:

```cpp
CPPBRIDGE_EXPORT int sum_int(int a, int b) {
    return a + b;
}
```

## Scalars

Supported scalar Java types:

```text
byte, int, long, float, double, void
```

## Heap arrays

Primitive arrays are mapped as pointer plus length.

Java:

```java
@CppFunction("average_double")
double average(@CppArray(ArrayDirection.IN) double[] values);
```

C++:

```cpp
CPPBRIDGE_EXPORT double average_double(double* values, int length);
```

Array direction:

- `IN`: Java to native;
- `OUT`: native to Java;
- `IN_OUT`: both directions.

Default is `IN_OUT`.

## Managed native arrays

Use managed native arrays when data is reused across multiple native calls:

```java
try (NativeDoubleArray values = NativeDoubleArray.copyOf(heapValues)) {
    math.heavyTransform(values);
    math.multiplyEachNative(values, 2.0);
    double[] result = values.toArray();
}
```

Supported wrappers:

```text
NativeByteArray
NativeIntArray
NativeLongArray
NativeFloatArray
NativeDoubleArray
```

## Build-time symbol validation

```xml
<expectedSymbols>
    <expectedSymbol>sum_int</expectedSymbol>
    <expectedSymbol>average_double</expectedSymbol>
</expectedSymbols>
```

If a configured symbol is missing, the Maven build fails. Reports are written to `target/cppbridge/`.

## Runtime inspection

```java
BindingReport report = CppBridge.inspect(FastMath.class);
System.out.println(report.toText());
```

Use this when checking how Java methods map to native symbols.

## Usage guidelines

Suitable workloads:

- large image or byte buffers;
- numerical transforms;
- repeated operations over one native buffer;
- matrix, audio, simulation, or codec kernels.

Avoid:

- tiny native methods called inside Java loops;
- repeated heap-to-native copies for small arrays;
- exposing complex C++ types directly through the ABI.
