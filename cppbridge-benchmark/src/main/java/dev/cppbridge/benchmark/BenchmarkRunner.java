package dev.cppbridge.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.CommandLineOptionException;
import org.openjdk.jmh.runner.options.CommandLineOptions;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Runnable JMH entry point.
 *
 * <p>Unlike the earlier prototype, this runner delegates command-line arguments to JMH itself.
 * That means these commands now work:</p>
 *
 * <pre>
 * java -jar target/benchmarks.jar
 * java -jar target/benchmarks.jar ImageBenchmarks
 * java -jar target/benchmarks.jar PipelineBenchmarks -f 1 -wi 3 -i 5
 * </pre>
 */
/**
 * Command-line entry point for running the benchmark jar.
 */
public final class BenchmarkRunner {
    private BenchmarkRunner() {
    }

    public static void main(String[] args) throws RunnerException, CommandLineOptionException {
        Options options = new OptionsBuilder()
                .parent(new CommandLineOptions(args))
                .detectJvmArgs()
                .jvmArgsAppend("--enable-native-access=ALL-UNNAMED")
                .build();

        new Runner(options).run();
    }
}
