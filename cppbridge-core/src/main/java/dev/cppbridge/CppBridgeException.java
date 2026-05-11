package dev.cppbridge;

/**
 * Runtime exception thrown by CppBridgeJ when a native binding cannot be
 * resolved, invoked, inspected, or marshalled safely.
 */
public class CppBridgeException extends RuntimeException {
    /**
     * Creates an exception with a detail message.
     *
     * @param message explanation of the failure
     */
    public CppBridgeException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a detail message and original cause.
     *
     * @param message explanation of the failure
     * @param cause original exception
     */
    public CppBridgeException(String message, Throwable cause) {
        super(message, cause);
    }
}
