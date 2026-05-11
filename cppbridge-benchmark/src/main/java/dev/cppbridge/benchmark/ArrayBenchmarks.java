package dev.cppbridge.benchmark;

import dev.cppbridge.CppBridge;
import dev.cppbridge.memory.NativeDoubleArray;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
/**
 * JMH benchmarks for primitive double-array calls through CppBridgeJ.
 */
public class ArrayBenchmarks {
    @Param({"1000", "100000", "1000000"})
    public int size;

    private BenchmarkMath cpp;
    private double[] values;
    private double[] mutableValues;
    private NativeDoubleArray nativeValues;
    private NativeDoubleArray nativeMutableValues;

    @Setup(Level.Trial)
    public void setupTrial() {
        cpp = CppBridge.load(BenchmarkMath.class);
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        values = new double[size];
        for (int i = 0; i < values.length; i++) {
            values[i] = i * 0.01;
        }
        mutableValues = values.clone();

        nativeValues = NativeDoubleArray.copyOf(values);
        nativeMutableValues = NativeDoubleArray.copyOf(mutableValues);
    }

    @TearDown(Level.Invocation)
    public void tearDownInvocation() {
        nativeValues.close();
        nativeMutableValues.close();
    }

    @Benchmark
    public double javaAverageForLoop() {
        double total = 0.0;
        for (double value : values) {
            total += value;
        }
        return total / values.length;
    }

    /**
     * Full bridge call from Java heap array: Java double[] -> native memory -> C++ -> Java result.
     */
    @Benchmark
    public double cppAverageFfmHeapArray() {
        return cpp.average(values);
    }

    /**
     * Hot native buffer call: data already lives in off-heap memory, so the benchmark mostly measures C++ work + FFM call.
     */
    @Benchmark
    public double cppAverageFfmNativeArray() {
        return cpp.averageNative(nativeValues);
    }

    @Benchmark
    public void javaMultiplyEachForLoop(Blackhole blackhole) {
        for (int i = 0; i < mutableValues.length; i++) {
            mutableValues[i] *= 2.5;
        }
        blackhole.consume(mutableValues[mutableValues.length - 1]);
    }

    /**
     * Full bridge call from Java heap array: Java double[] -> native memory -> C++ -> copy back to Java double[].
     */
    @Benchmark
    public void cppMultiplyEachFfmHeapArray(Blackhole blackhole) {
        cpp.multiplyEach(mutableValues, 2.5);
        blackhole.consume(mutableValues[mutableValues.length - 1]);
    }

    /**
     * Hot native buffer call: no per-call heap/native array copy.
     */
    @Benchmark
    public void cppMultiplyEachFfmNativeArray(Blackhole blackhole) {
        cpp.multiplyEachNative(nativeMutableValues, 2.5);
        blackhole.consume(nativeMutableValues.get(nativeMutableValues.length() - 1));
    }

    @Benchmark
    public void javaHeavyTransform(Blackhole blackhole) {
        for (int i = 0; i < mutableValues.length; i++) {
            double x = mutableValues[i];
            mutableValues[i] = Math.sin(x) * Math.cos(x) + Math.sqrt(x + 1.0);
        }
        blackhole.consume(mutableValues[mutableValues.length - 1]);
    }

    /**
     * Full bridge call from Java heap array: includes array marshalling cost.
     */
    @Benchmark
    public void cppHeavyTransformFfmHeapArray(Blackhole blackhole) {
        cpp.heavyTransform(mutableValues);
        blackhole.consume(mutableValues[mutableValues.length - 1]);
    }

    /**
     * Hot native buffer call: useful when several C++ kernels are chained over the same data.
     */
    @Benchmark
    public void cppHeavyTransformFfmNativeArray(Blackhole blackhole) {
        cpp.heavyTransformNative(nativeMutableValues);
        blackhole.consume(nativeMutableValues.get(nativeMutableValues.length() - 1));
    }
}
