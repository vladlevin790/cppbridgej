# Build-time Validation

The Maven plugin can validate exported native symbols after compilation.

```xml
<configuration>
    <libraryName>fastmath</libraryName>
    <expectedSymbols>
        <expectedSymbol>sum_int</expectedSymbol>
        <expectedSymbol>average_double</expectedSymbol>
    </expectedSymbols>
    <failOnMissingSymbols>true</failOnMissingSymbols>
</configuration>
```

## Generated files

```text
target/cppbridge/native-build-report.txt
target/cppbridge/exported-symbols.txt
target/cppbridge/exported-symbols-raw.txt
target/cppbridge/missing-symbols.txt
```

## Report contents

The native build report includes:

- platform;
- library name;
- output library path;
- source directory;
- compiler command;
- compiler exit code;
- compiled C++ source files;
- expected symbols;
- symbol inspection command;
- exported symbol count;
- missing symbol count.

## Symbol tools

Platform inspection commands:

```text
macOS/Linux: nm -g <library>
Windows:     dumpbin /EXPORTS <library>
```

If symbol validation fails, check that exported functions use `extern "C"` and that names in `expectedSymbols` match the C ABI names exactly.
