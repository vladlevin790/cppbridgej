# Testing

## Unit tests

Core unit tests cover:

- binding report generation;
- unsupported signature reporting;
- managed native array allocation and copying.

## Integration tests

`cppbridge-example` compiles C++ through the Maven plugin and calls the resulting shared library through the public Java API.

Test path:

```text
Maven plugin -> C++ compiler -> shared library -> CppBridge.load -> FFM call -> assertion
```

## Benchmarks

JMH benchmarks are stored in `cppbridge-benchmark`.

They compare:

- Java loops;
- C++ calls with heap-array marshalling;
- C++ calls with managed native arrays;
- separate native calls vs fused native kernels.

Benchmarks are not correctness tests.
