# Performance Notes

CppBridgeJ should be evaluated with JMH or another controlled benchmark for the target workload.

## Main cost sources

- FFM call overhead;
- heap-to-native copy;
- native-to-heap copy;
- memory allocation;
- compiler optimization differences;
- CPU vectorization differences.

## Heap arrays

Heap arrays are convenient and work well for simple APIs. For large arrays, copy cost may dominate.

## Managed native arrays

Managed native arrays keep data off-heap across calls. They are better suited to pipelines where the same buffer is processed by several native kernels.

## Fused kernels

Combining several operations into one native function can reduce call overhead and memory passes. It can also make compiler optimization harder. Benchmark both variants when performance matters.

## Benchmark hygiene

Record:

- CPU and operating system;
- JVM version;
- compiler and flags;
- JMH command;
- warmup and measurement settings;
- battery/power mode if relevant.
