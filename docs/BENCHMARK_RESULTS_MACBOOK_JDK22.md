# Benchmark Results: MacBook, JDK 22.0.2

Environment from local runs:

```text
JDK: 22.0.2 HotSpot
Benchmark: JMH 1.37
Mode: Average time, ms/op
Warmup: 3 x 1 s
Measurement: 5 x 1 s
Threads: 1
Native access: --enable-native-access=ALL-UNNAMED
```

These results are machine-specific. Use them as a reference point, not as a general performance guarantee.

## Array benchmarks

Size: `1_000_000` elements.

| Benchmark | Score, ms/op |
|---|---:|
| `javaAverageForLoop` | 0.961 |
| `cppAverageFfmHeapArray` | 1.404 |
| `cppAverageFfmNativeArray` | 0.987 |
| `javaMultiplyEachForLoop` | 0.176 |
| `cppMultiplyEachFfmHeapArray` | 0.765 |
| `cppMultiplyEachFfmNativeArray` | 0.246 |
| `javaHeavyTransform` | 9.327 |
| `cppHeavyTransformFfmHeapArray` | 5.584 |
| `cppHeavyTransformFfmNativeArray` | 5.059 |

## Numeric pipeline

Size: `1_000_000` double values.

| Benchmark | Score, ms/op |
|---|---:|
| `javaThreeStepPipeline` | 18.446 |
| `javaFusedThreeStepPipeline` | 22.011 |
| `cppNativeArrayThreeStepPipeline` | 10.254 |
| `cppNativeArrayFusedPipeline` | 15.096 |

For this workload, the three-step native pipeline is faster than the fused numeric kernel.

## Image pipeline

Size: `3_000_000` bytes.

| Benchmark | Score, ms/op |
|---|---:|
| `javaImagePipeline` | 1.800 |
| `javaFusedImagePipeline` | 1.566 |
| `cppHeapByteArrayImagePipeline` | 1.035 |
| `cppHeapByteArrayFusedImagePipeline` | 0.648 |
| `cppNativeByteArrayImagePipeline` | 0.524 |
| `cppNativeByteArrayFusedImagePipeline` | 0.450 |

## Summary

- Heap-array calls are simple to use, but copy cost is visible for large buffers.
- Managed native arrays reduce repeated copy overhead.
- Small/simple loops may be faster in Java.
- Larger numeric and byte-buffer workloads can benefit from native kernels.
- Fusing operations should be benchmarked per workload.
