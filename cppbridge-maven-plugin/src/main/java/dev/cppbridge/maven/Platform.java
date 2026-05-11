package dev.cppbridge.maven;

import java.util.Locale;

/**
 * Platform-specific settings used by the Maven plugin.
 */
enum Platform {
    MACOS,
    LINUX,
    WINDOWS;

    /**
     * Detects the current operating system.
     *
     * @return detected platform
     */
    static Platform detect() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("mac") || os.contains("darwin")) {
            return MACOS;
        }
        if (os.contains("win")) {
            return WINDOWS;
        }
        return LINUX;
    }

    /**
     * Builds the shared-library filename for this platform.
     *
     * @param baseName logical library name
     * @return platform-specific filename
     */
    String libraryFileName(String baseName) {
        String cleanName = baseName
                .replaceAll("[^a-zA-Z0-9_-]", "_")
                .replaceAll("-", "_");

        return switch (this) {
            case MACOS -> "lib" + cleanName + ".dylib";
            case LINUX -> "lib" + cleanName + ".so";
            case WINDOWS -> cleanName + ".dll";
        };
    }

    /**
     * Returns the default compiler executable for this platform.
     *
     * @return compiler command name
     */
    String defaultCompiler() {
        return switch (this) {
            case MACOS -> "clang++";
            case LINUX -> "g++";
            case WINDOWS -> "cl";
        };
    }
}
