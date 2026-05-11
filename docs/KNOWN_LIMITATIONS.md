# Known Limitations

- Only the native FFM backend is implemented.
- WASM is not implemented.
- Complex C++ types are not supported at the ABI boundary.
- Strings are not supported yet.
- Struct layouts are not supported yet.
- Callbacks are not supported yet.
- Native exceptions must not cross the ABI boundary.
- Java heap arrays are copied unless managed native arrays are used.
- Small native calls may be slower than Java due to call overhead.
- Windows support depends on MSVC tooling and has less coverage than macOS/Linux.
- JDK 22+ is required.

Supported boundary types are intentionally limited to primitive scalars, primitive arrays, and managed native arrays.
