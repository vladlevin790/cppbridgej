package dev.cppbridge;

/**
 * Describes how CppBridgeJ should marshal Java primitive arrays.
 */
public enum ArrayDirection {
    /** Java -> native only. Faster for read-only native functions. */
    IN,

    /** native -> Java only. Useful when C++ fills an output buffer. */
    OUT,

    /** Java -> native -> Java. Default because it is safest for mutable kernels. */
    IN_OUT
}
