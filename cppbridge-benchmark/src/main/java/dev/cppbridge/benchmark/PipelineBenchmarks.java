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
 * JMH benchmarks for multi-step numeric processing pipelines.
 */
public class PipelineBenchmarks {
    @Param({"100000", "1000000"})
    public int size;

    private BenchmarkMath cpp;
    private double[] javaValues;
    private double[] javaFusedValues;
    private double[] heapBridgeValues;
    private double[] heapBridgeFusedValues;
    private NativeDoubleArray nativeValues;
    private NativeDoubleArray nativeFusedValues;

    @Setup(Level.Trial)
    public void setupTrial() {
        cpp = CppBridge.load(BenchmarkMath.class);
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        javaValues = new double[size];
        for (int i = 0; i < javaValues.length; i++) {
            javaValues[i] = i * 0.01;
        }
        javaFusedValues = javaValues.clone();
        heapBridgeValues = javaValues.clone();
        heapBridgeFusedValues = javaValues.clone();
        nativeValues = NativeDoubleArray.copyOf(javaValues);
        nativeFusedValues = NativeDoubleArray.copyOf(javaValues);
    }

    @TearDown(Level.Invocation)
    public void tearDownInvocation() {
        nativeValues.close();
        nativeFusedValues.close();
    }

    @Benchmark
    public void javaThreeStepPipeline(Blackhole blackhole) {
        heavyTransformJava(javaValues);
        multiplyEachJava(javaValues, 1.25);
        heavyTransformJava(javaValues);
        blackhole.consume(javaValues[javaValues.length - 1]);
    }

    @Benchmark
    public void javaFusedThreeStepPipeline(Blackhole blackhole) {
        fusedPipelineJava(javaFusedValues, 1.25);
        blackhole.consume(javaFusedValues[javaFusedValues.length - 1]);
    }

    /**
     * C++ pipeline through heap arrays. Each native call performs Java heap/native marshalling.
     */
    @Benchmark
    public void cppHeapArrayThreeStepPipeline(Blackhole blackhole) {
        cpp.heavyTransform(heapBridgeValues);
        cpp.multiplyEach(heapBridgeValues, 1.25);
        cpp.heavyTransform(heapBridgeValues);
        blackhole.consume(heapBridgeValues[heapBridgeValues.length - 1]);
    }

    /**
     * C++ fused kernel through a heap array. The buffer is copied once in and once out.
     */
    @Benchmark
    public void cppHeapArrayFusedPipeline(Blackhole blackhole) {
        cpp.threeStepPipeline(heapBridgeFusedValues, 1.25);
        blackhole.consume(heapBridgeFusedValues[heapBridgeFusedValues.length - 1]);
    }

    /**
     * C++ pipeline over one persistent native buffer.
     */
    @Benchmark
    public void cppNativeArrayThreeStepPipeline(Blackhole blackhole) {
        cpp.heavyTransformNative(nativeValues);
        cpp.multiplyEachNative(nativeValues, 1.25);
        cpp.heavyTransformNative(nativeValues);
        blackhole.consume(nativeValues.get(nativeValues.length() - 1));
    }

    /**
     * C++ fused kernel over one persistent native buffer. This is the lowest-overhead FFM path.
     */
    @Benchmark
    public void cppNativeArrayFusedPipeline(Blackhole blackhole) {
        cpp.threeStepPipelineNative(nativeFusedValues, 1.25);
        blackhole.consume(nativeFusedValues.get(nativeFusedValues.length() - 1));
    }

    private static void heavyTransformJava(double[] values) {
        for (int i = 0; i < values.length; i++) {
            values[i] = heavyValueJava(values[i]);
        }
    }

    private static void multiplyEachJava(double[] values, double factor) {
        for (int i = 0; i < values.length; i++) {
            values[i] *= factor;
        }
    }

    private static void fusedPipelineJava(double[] values, double factor) {
        for (int i = 0; i < values.length; i++) {
            double x = heavyValueJava(values[i]);
            x *= factor;
            values[i] = heavyValueJava(x);
        }
    }

    private static double heavyValueJava(double x) {
        return Math.sin(x) * Math.cos(x) + Math.sqrt(x + 1.0);
    }
}
