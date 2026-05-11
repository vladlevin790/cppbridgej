package dev.cppbridge.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Maven goal that compiles C++ sources from {@code src/main/cpp} into a native
 * shared library.
 *
 * <p>The goal also inspects exported symbols and can fail the build when a
 * configured expected symbol is missing. Reports are written to
 * {@code target/cppbridge} by default.</p>
 */
@Mojo(name = "compile-cpp", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public final class CompileCppMojo extends AbstractMojo {
    /** Directory containing C++ source files. */
    @Parameter(defaultValue = "${project.basedir}/src/main/cpp")
    private File sourceDirectory;

    /** Directory where the compiled shared library is written. */
    @Parameter(defaultValue = "${project.build.directory}/native")
    private File outputDirectory;

    /** Directory where build reports and symbol reports are written. */
    @Parameter(defaultValue = "${project.build.directory}/cppbridge")
    private File reportDirectory;

    /** Logical native library name without platform prefix or extension. */
    @Parameter(defaultValue = "${project.artifactId}")
    private String libraryName;

    /** Optional compiler executable. If empty, the platform default is used. */
    @Parameter(defaultValue = "")
    private String compiler;

    /** Compiler optimization level, for example {@code O2} or {@code O3}. */
    @Parameter(defaultValue = "O3")
    private String optimizationLevel;

    /** C++ language standard passed to the compiler. */
    @Parameter(defaultValue = "c++20")
    private String cppStandard;

    /** Extra compiler arguments, for example: -march=native, -ffast-math, /arch:AVX2. */
    @Parameter
    private List<String> extraCompilerArgs;

    /** Symbols that must be exported by the resulting native library. */
    @Parameter
    private List<String> expectedSymbols;

    /** Fail the Maven build if any configured expectedSymbols are missing. */
    @Parameter(defaultValue = "true")
    private boolean failOnMissingSymbols;

    /** Generate target/cppbridge/native-build-report.txt and exported-symbols.txt. */
    @Parameter(defaultValue = "true")
    private boolean generateBuildReport;

    /** Skip C++ compilation for this module. */
    @Parameter(defaultValue = "false")
    private boolean skip;

    /**
     * Executes C++ compilation, symbol inspection, and optional symbol
     * validation.
     *
     * @throws MojoExecutionException if compilation or required symbol
     *                                validation fails
     */
    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("C++ compilation skipped by configuration.");
            return;
        }
        Path sourceDir = sourceDirectory.toPath();
        if (!Files.exists(sourceDir)) {
            getLog().info("C++ source directory does not exist, skipping: " + sourceDir);
            return;
        }

        List<Path> cppFiles = findCppFiles(sourceDir);
        if (cppFiles.isEmpty()) {
            getLog().info("No C++ files found in: " + sourceDir);
            return;
        }

        try {
            Files.createDirectories(outputDirectory.toPath());
            Files.createDirectories(reportDirectory.toPath());
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create CppBridgeJ output directories", e);
        }

        Platform platform = Platform.detect();
        Path outputLibrary = outputDirectory.toPath().resolve(platform.libraryFileName(libraryName));
        List<String> command = buildCommand(platform, cppFiles, outputLibrary);

        getLog().info("Compiling C++ library: " + outputLibrary);
        getLog().info(String.join(" ", command));

        CommandResult compileResult = runCommand(command);
        if (!compileResult.output().isBlank()) {
            getLog().info(compileResult.output());
        }
        if (compileResult.exitCode() != 0) {
            throw new MojoExecutionException("C++ compiler failed with exit code "
                    + compileResult.exitCode() + "\n" + compileResult.output());
        }

        NativeSymbolReport symbolReport = inspectExportedSymbols(platform, outputLibrary);
        List<String> missingSymbols = findMissingSymbols(symbolReport.normalizedSymbols());

        if (generateBuildReport) {
            writeReports(platform, cppFiles, outputLibrary, command, compileResult, symbolReport, missingSymbols);
        }

        if (!missingSymbols.isEmpty()) {
            String message = "Native library is missing expected exported symbols: " + String.join(", ", missingSymbols)
                    + "\nSee: " + reportDirectory.toPath().resolve("native-build-report.txt").toAbsolutePath();
            if (failOnMissingSymbols) {
                throw new MojoExecutionException(message);
            }
            getLog().warn(message);
        } else if (expectedSymbols != null && !expectedSymbols.isEmpty()) {
            getLog().info("CppBridgeJ expected-symbol validation passed: " + expectedSymbols.size() + " symbol(s).");
        }
    }

    private List<Path> findCppFiles(Path sourceDir) throws MojoExecutionException {
        try (Stream<Path> stream = Files.walk(sourceDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.endsWith(".cpp") || name.endsWith(".cc") || name.endsWith(".cxx");
                    })
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot scan C++ source directory: " + sourceDir, e);
        }
    }

    private List<String> buildCommand(Platform platform, List<Path> cppFiles, Path outputLibrary) {
        if (platform == Platform.WINDOWS) {
            return buildMsvcCommand(cppFiles, outputLibrary);
        }
        return buildUnixCommand(platform, cppFiles, outputLibrary);
    }

    private List<String> buildUnixCommand(Platform platform, List<Path> cppFiles, Path outputLibrary) {
        String selectedCompiler = compiler == null || compiler.isBlank()
                ? platform.defaultCompiler()
                : compiler;

        List<String> command = new ArrayList<>();
        command.add(selectedCompiler);
        command.add("-" + optimizationLevel);
        command.add("-std=" + cppStandard);
        command.add("-shared");
        command.add("-fPIC");
        addExtraCompilerArgs(command);
        for (Path cppFile : cppFiles) {
            command.add(cppFile.toAbsolutePath().toString());
        }
        command.add("-o");
        command.add(outputLibrary.toAbsolutePath().toString());
        return command;
    }

    private void addExtraCompilerArgs(List<String> command) {
        if (extraCompilerArgs == null || extraCompilerArgs.isEmpty()) {
            return;
        }
        for (String arg : extraCompilerArgs) {
            if (arg != null && !arg.isBlank()) {
                command.add(arg);
            }
        }
    }

    private List<String> buildMsvcCommand(List<Path> cppFiles, Path outputLibrary) {
        String selectedCompiler = compiler == null || compiler.isBlank() ? "cl" : compiler;

        List<String> command = new ArrayList<>();
        command.add(selectedCompiler);
        command.add("/O2");
        command.add("/LD");
        command.add("/EHsc");
        addExtraCompilerArgs(command);
        for (Path cppFile : cppFiles) {
            command.add(cppFile.toAbsolutePath().toString());
        }
        command.add("/Fe:" + outputLibrary.toAbsolutePath());
        return command;
    }

    private CommandResult runCommand(List<String> command) throws MojoExecutionException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            return new CommandResult(exitCode, output);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot start command: " + String.join(" ", command), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MojoExecutionException("Command interrupted: " + String.join(" ", command), e);
        }
    }

    private NativeSymbolReport inspectExportedSymbols(Platform platform, Path outputLibrary) {
        List<String> command = platform == Platform.WINDOWS
                ? List.of("dumpbin", "/EXPORTS", outputLibrary.toAbsolutePath().toString())
                : List.of("nm", "-g", outputLibrary.toAbsolutePath().toString());

        try {
            CommandResult result = runCommand(command);
            if (result.exitCode() != 0) {
                return new NativeSymbolReport(command, result.output(), Set.of(), Set.of(),
                        "Symbol inspection command failed with exit code " + result.exitCode());
            }
            Set<String> rawSymbols = parseSymbols(platform, result.output());
            Set<String> normalizedSymbols = new LinkedHashSet<>();
            for (String symbol : rawSymbols) {
                normalizedSymbols.add(normalizeSymbol(symbol));
            }
            return new NativeSymbolReport(command, result.output(), rawSymbols, normalizedSymbols, "");
        } catch (MojoExecutionException exception) {
            return new NativeSymbolReport(command, "", Set.of(), Set.of(),
                    "Cannot inspect exported symbols. Is nm/dumpbin available? " + exception.getMessage());
        }
    }

    private Set<String> parseSymbols(Platform platform, String output) {
        Set<String> symbols = new LinkedHashSet<>();
        for (String line : output.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            if (platform == Platform.WINDOWS) {
                parseDumpbinLine(trimmed, symbols);
            } else {
                parseNmLine(trimmed, symbols);
            }
        }
        return symbols;
    }

    private void parseNmLine(String line, Set<String> symbols) {
        String[] parts = line.split("\\s+");
        if (parts.length == 0) {
            return;
        }
        String candidate = parts[parts.length - 1];
        if (candidate.contains("/") || candidate.contains(".")) {
            return;
        }
        symbols.add(candidate);
    }

    private void parseDumpbinLine(String line, Set<String> symbols) {
        String[] parts = line.split("\\s+");
        if (parts.length >= 4 && parts[0].chars().allMatch(Character::isDigit)) {
            symbols.add(parts[3]);
        }
    }

    private String normalizeSymbol(String symbol) {
        String normalized = symbol == null ? "" : symbol.trim();
        while (normalized.startsWith("_")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private List<String> findMissingSymbols(Set<String> normalizedSymbols) {
        if (expectedSymbols == null || expectedSymbols.isEmpty()) {
            return List.of();
        }
        List<String> missing = new ArrayList<>();
        for (String expectedSymbol : expectedSymbols) {
            if (expectedSymbol == null || expectedSymbol.isBlank()) {
                continue;
            }
            String normalizedExpected = normalizeSymbol(expectedSymbol);
            if (!normalizedSymbols.contains(normalizedExpected)) {
                missing.add(expectedSymbol);
            }
        }
        return missing;
    }

    private void writeReports(
            Platform platform,
            List<Path> cppFiles,
            Path outputLibrary,
            List<String> command,
            CommandResult compileResult,
            NativeSymbolReport symbolReport,
            List<String> missingSymbols
    ) throws MojoExecutionException {
        Path reportDir = reportDirectory.toPath();
        Path buildReport = reportDir.resolve("native-build-report.txt");
        Path exportedSymbolsFile = reportDir.resolve("exported-symbols.txt");
        Path rawSymbolsFile = reportDir.resolve("exported-symbols-raw.txt");
        Path missingSymbolsFile = reportDir.resolve("missing-symbols.txt");

        StringBuilder report = new StringBuilder();
        report.append("CppBridgeJ native build report\n");
        report.append("Generated at: ").append(Instant.now()).append('\n');
        report.append("Platform: ").append(platform).append('\n');
        report.append("Library name: ").append(libraryName).append('\n');
        report.append("Output library: ").append(outputLibrary.toAbsolutePath()).append('\n');
        report.append("Source directory: ").append(sourceDirectory.toPath().toAbsolutePath()).append('\n');
        report.append("Compiler command: ").append(String.join(" ", command)).append('\n');
        report.append("Compiler exit code: ").append(compileResult.exitCode()).append('\n');
        report.append('\n');

        report.append("C++ sources:\n");
        for (Path cppFile : cppFiles) {
            report.append("- ").append(cppFile.toAbsolutePath()).append('\n');
        }
        report.append('\n');

        report.append("Expected symbols:\n");
        if (expectedSymbols == null || expectedSymbols.isEmpty()) {
            report.append("- <none configured>\n");
        } else {
            for (String expectedSymbol : expectedSymbols) {
                report.append("- ").append(expectedSymbol).append('\n');
            }
        }
        report.append('\n');

        report.append("Symbol inspection command: ").append(String.join(" ", symbolReport.command())).append('\n');
        if (!symbolReport.message().isBlank()) {
            report.append("Symbol inspection note: ").append(symbolReport.message()).append('\n');
        }
        report.append("Exported symbols detected: ").append(symbolReport.normalizedSymbols().size()).append('\n');
        report.append("Missing expected symbols: ").append(missingSymbols.size()).append('\n');
        for (String missingSymbol : missingSymbols) {
            report.append("- MISSING: ").append(missingSymbol).append('\n');
        }

        try {
            Files.writeString(buildReport, report.toString(), StandardCharsets.UTF_8);
            Files.writeString(exportedSymbolsFile, String.join(System.lineSeparator(), symbolReport.normalizedSymbols()), StandardCharsets.UTF_8);
            Files.writeString(rawSymbolsFile, symbolReport.rawOutput(), StandardCharsets.UTF_8);
            Files.writeString(missingSymbolsFile, String.join(System.lineSeparator(), missingSymbols), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot write CppBridgeJ build reports to " + reportDir, e);
        }

        getLog().info("CppBridgeJ native build report: " + buildReport.toAbsolutePath());
    }

    private record CommandResult(int exitCode, String output) {
    }

    private record NativeSymbolReport(
            List<String> command,
            String rawOutput,
            Set<String> rawSymbols,
            Set<String> normalizedSymbols,
            String message
    ) {
    }
}
