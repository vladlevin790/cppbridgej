package dev.cppbridge;

/**
 * Selects the backend used by a {@link dev.cppbridge.annotations.CppModule}.
 *
 * <p>The current implementation supports {@link #NATIVE}. The other values are
 * reserved for future backends and for API compatibility with the planned
 * WebAssembly fallback.</p>
 */
public enum BuildMode {
    /**
     * Compiles C++ sources to a platform native shared library and calls it
     * through the Java Foreign Function &amp; Memory API.
     */
    NATIVE,

    /**
     * Reserved for a future WebAssembly backend.
     */
    WASM,

    /**
     * Reserved for automatic backend selection in a later version.
     */
    AUTO
}
