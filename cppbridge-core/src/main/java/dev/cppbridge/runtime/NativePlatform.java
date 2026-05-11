package dev.cppbridge.runtime;

import java.util.Locale;

/**
 * Operating systems supported by the native backend.
 */
public enum NativePlatform {
    MACOS,
    LINUX,
    WINDOWS;

    /**
     * Detects the current operating system from {@code os.name}.
     *
     * @return detected platform
     */
    public static NativePlatform detect() {
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
     * Converts a logical library name to a platform-specific shared-library
     * filename.
     *
     * @param baseName logical library name without prefix or extension
     * @return platform-specific library filename
     */
    public String libraryFileName(String baseName) {
        String cleanName = baseName
                .replaceAll("[^a-zA-Z0-9_-]", "_")
                .replaceAll("-", "_");

        return switch (this) {
            case MACOS -> "lib" + cleanName + ".dylib";
            case LINUX -> "lib" + cleanName + ".so";
            case WINDOWS -> cleanName + ".dll";
        };
    }
}
