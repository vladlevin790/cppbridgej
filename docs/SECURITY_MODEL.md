# Security Model

CppBridgeJ executes native code in the same process as the JVM through Java FFM.

Native code can:

- read or write process memory if the C++ code is wrong;
- crash the JVM;
- leak native memory;
- call operating-system APIs.

CppBridgeJ is not a sandbox.

Use it for trusted native code built with the application. For untrusted plugins, use a sandboxed runtime model. A future WASM backend may address that use case.

Recommended practices:

- keep exported ABI functions small;
- validate lengths in C++;
- avoid exceptions across the ABI boundary;
- use managed native arrays instead of raw pointer handling in Java;
- configure build-time symbol validation;
- cover exported functions with integration tests.
