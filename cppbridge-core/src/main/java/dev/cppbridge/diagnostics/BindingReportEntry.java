package dev.cppbridge.diagnostics;

import java.util.Objects;

/**
 * Diagnostic result for a single Java method and its native symbol mapping.
 *
 * @param javaMethod Java method signature rendered for diagnostics
 * @param nativeSymbol exported native symbol expected by CppBridgeJ
 * @param nativeSignature native signature inferred from the Java method
 * @param status validation status
 * @param message optional additional diagnostic message
 */
public record BindingReportEntry(
        String javaMethod,
        String nativeSymbol,
        String nativeSignature,
        BindingStatus status,
        String message
) {
    /**
     * Creates an immutable binding report entry.
     */
    public BindingReportEntry {
        Objects.requireNonNull(javaMethod, "javaMethod");
        Objects.requireNonNull(nativeSymbol, "nativeSymbol");
        Objects.requireNonNull(nativeSignature, "nativeSignature");
        Objects.requireNonNull(status, "status");
        message = message == null ? "" : message;
    }
}
