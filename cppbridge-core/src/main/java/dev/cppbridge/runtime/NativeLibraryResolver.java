package dev.cppbridge.runtime;

import dev.cppbridge.CppBridgeException;
import dev.cppbridge.annotations.CppModule;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Resolves native library paths from {@link CppModule} metadata and the current
 * operating system.
 */
public final class NativeLibraryResolver {
    private NativeLibraryResolver() {
    }

    /**
     * Resolves and verifies the expected native library path.
     *
     * @param apiType annotated API interface
     * @param module module annotation
     * @return resolved library path
     */
    public static String resolve(Class<?> apiType, CppModule module) {
        Path path = expectedPath(apiType, module);

        if (!Files.exists(path)) {
            String libraryName = effectiveLibraryName(apiType, module);
            throw new CppBridgeException(
                    "Native library was not found: " + path.toAbsolutePath().normalize() + "\n" +
                            "Expected libraryName='" + libraryName + "'. " +
                            "Run `mvn package` in the module that owns src/main/cpp first, " +
                            "or pass an explicit path: CppBridge.load(Api.class, \"target/native/...\")."
            );
        }

        return path.toString();
    }

    /**
     * Computes the expected native library path without checking whether it
     * exists.
     *
     * @param apiType annotated API interface
     * @param module module annotation
     * @return expected library path
     */
    public static Path expectedPath(Class<?> apiType, CppModule module) {
        if (module.libraryPath() != null && !module.libraryPath().isBlank()) {
            return Path.of(module.libraryPath()).normalize();
        }

        String fileName = NativePlatform.detect().libraryFileName(effectiveLibraryName(apiType, module));
        return Path.of(module.outputDirectory(), fileName).normalize();
    }

    /**
     * Returns the configured logical library name or falls back to the API type
     * simple name.
     *
     * @param apiType annotated API interface
     * @param module module annotation
     * @return logical native library name
     */
    public static String effectiveLibraryName(Class<?> apiType, CppModule module) {
        String libraryName = module.libraryName();
        if (libraryName == null || libraryName.isBlank()) {
            libraryName = apiType.getSimpleName();
        }
        return libraryName;
    }
}
