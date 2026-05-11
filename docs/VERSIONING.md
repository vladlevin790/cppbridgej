# Versioning

Current version:

```text
1.0.0-rc2
```

Release candidates may still change the API before `1.0.0`.

Expected stable API surface:

- `CppBridge.load(...)`
- `CppBridge.inspect(...)`
- `@CppModule`
- `@CppFunction`
- `@CppArray`
- `ArrayDirection`
- managed native arrays
- Maven plugin `compile-cpp`
- build-time expected symbol validation

Potential post-1.0 additions:

- Gradle plugin;
- WASM backend;
- generated binding metadata;
- richer type layouts;
- Spring Boot integration.
