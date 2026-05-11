# Project Scope

CppBridgeJ provides a Java-facing API for native C++ kernels.

It focuses on:

- C++ compilation inside Maven builds;
- type mapping for primitive values and arrays;
- Java FFM invocation;
- off-heap buffers for repeated native calls;
- diagnostics for build-time and runtime binding issues;
- benchmarks for evaluating workloads.

Primary use cases:

- image and byte-buffer processing;
- numerical transforms;
- audio or signal buffers;
- matrix and simulation kernels;
- project-local native extensions.

Out of scope for the current release:

- replacing arbitrary Java code;
- exposing full C++ APIs;
- sandboxing untrusted native code;
- automatic performance decisions.
