package dev.cppbridge.diagnostics;

/**
 * Validation status for one Java-to-native method binding.
 */
public enum BindingStatus {
    /**
     * The native library exists, the symbol is exported, and the Java signature
     * is supported by CppBridgeJ.
     */
    OK,

    /**
     * The Java signature is supported, but the native symbol was not found in
     * the selected shared library.
     */
    MISSING_SYMBOL,

    /**
     * The Java method uses a return type or parameter type that cannot be
     * mapped to the current native backend.
     */
    UNSUPPORTED_SIGNATURE,

    /**
     * The native shared library file does not exist at the expected path.
     */
    LIBRARY_NOT_FOUND,

    /**
     * The library could not be inspected, for example because it could not be
     * opened by the JVM.
     */
    INSPECTION_FAILED
}
