package dev.cppbridge.diagnostics;

import java.util.List;
import java.util.Objects;

/**
 * Diagnostic report for all native bindings declared by one API interface.
 *
 * @param apiType fully qualified Java API interface name
 * @param mode backend mode from the {@code @CppModule} annotation
 * @param libraryPath native library path used for inspection
 * @param libraryExists whether the native library exists at {@code libraryPath}
 * @param entries method-level binding diagnostics
 */
public record BindingReport(
        String apiType,
        String mode,
        String libraryPath,
        boolean libraryExists,
        List<BindingReportEntry> entries
) {
    /**
     * Creates an immutable binding report.
     */
    public BindingReport {
        Objects.requireNonNull(apiType, "apiType");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(libraryPath, "libraryPath");
        entries = List.copyOf(Objects.requireNonNull(entries, "entries"));
    }

    /**
     * Returns {@code true} when the library exists and every method binding is
     * valid.
     *
     * @return whether all bindings are valid
     */
    public boolean isHealthy() {
        return libraryExists && entries.stream().allMatch(entry -> entry.status() == BindingStatus.OK);
    }

    /**
     * Renders this report as readable plain text.
     *
     * @return diagnostic report text
     */
    public String toText() {
        StringBuilder builder = new StringBuilder();
        builder.append("CppBridgeJ binding report\n");
        builder.append("API: ").append(apiType).append('\n');
        builder.append("Mode: ").append(mode).append('\n');
        builder.append("Library: ").append(libraryPath).append('\n');
        builder.append("Library exists: ").append(libraryExists).append('\n');
        builder.append("Healthy: ").append(isHealthy()).append('\n');
        builder.append('\n');

        for (BindingReportEntry entry : entries) {
            builder.append("- ").append(entry.javaMethod()).append('\n');
            builder.append("  -> ").append(entry.nativeSignature()).append('\n');
            builder.append("  symbol: ").append(entry.nativeSymbol()).append('\n');
            builder.append("  status: ").append(entry.status());
            if (!entry.message().isBlank()) {
                builder.append(" — ").append(entry.message());
            }
            builder.append("\n\n");
        }

        return builder.toString();
    }

    /**
     * Returns the same text as {@link #toText()}.
     *
     * @return diagnostic report text
     */
    @Override
    public String toString() {
        return toText();
    }
}
