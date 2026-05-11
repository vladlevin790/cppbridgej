package dev.cppbridge.benchmark;

import dev.cppbridge.CppBridge;
import dev.cppbridge.memory.NativeByteArray;
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
 * JMH benchmarks for byte-buffer image-style kernels.
 */
public class ImageBenchmarks {
    /** Roughly: small image buffer and large image buffer. */
    @Param({"300000", "3000000"})
    public int size;

    private BenchmarkMath cpp;
    private byte[] javaPixels;
    private byte[] javaFusedPixels;
    private byte[] heapBridgePixels;
    private byte[] heapBridgeFusedPixels;
    private NativeByteArray nativePixels;
    private NativeByteArray nativeFusedPixels;

    @Setup(Level.Trial)
    public void setupTrial() {
        cpp = CppBridge.load(BenchmarkMath.class);
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        javaPixels = new byte[size];
        for (int i = 0; i < javaPixels.length; i++) {
            javaPixels[i] = (byte) ((i * 31 + 17) & 0xFF);
        }
        javaFusedPixels = javaPixels.clone();
        heapBridgePixels = javaPixels.clone();
        heapBridgeFusedPixels = javaPixels.clone();
        nativePixels = NativeByteArray.copyOf(javaPixels);
        nativeFusedPixels = NativeByteArray.copyOf(javaPixels);
    }

    @TearDown(Level.Invocation)
    public void tearDownInvocation() {
        nativePixels.close();
        nativeFusedPixels.close();
    }

    @Benchmark
    public void javaImagePipeline(Blackhole blackhole) {
        brightnessJava(javaPixels, 25);
        invertJava(javaPixels);
        thresholdJava(javaPixels, 128);
        blackhole.consume(javaPixels[javaPixels.length - 1]);
    }

    @Benchmark
    public void javaFusedImagePipeline(Blackhole blackhole) {
        imagePipelineJava(javaFusedPixels, 25, 128);
        blackhole.consume(javaFusedPixels[javaFusedPixels.length - 1]);
    }

    /**
     * C++ image pipeline through byte[]: easy API, but every call copies the buffer.
     */
    @Benchmark
    public void cppHeapByteArrayImagePipeline(Blackhole blackhole) {
        cpp.brightness(heapBridgePixels, 25);
        cpp.invert(heapBridgePixels);
        cpp.threshold(heapBridgePixels, 128);
        blackhole.consume(heapBridgePixels[heapBridgePixels.length - 1]);
    }

    /**
     * C++ fused image kernel through byte[]. The buffer is copied once in and once out.
     */
    @Benchmark
    public void cppHeapByteArrayFusedImagePipeline(Blackhole blackhole) {
        cpp.imagePipeline(heapBridgeFusedPixels, 25, 128);
        blackhole.consume(heapBridgeFusedPixels[heapBridgeFusedPixels.length - 1]);
    }

    /**
     * C++ image pipeline over a persistent native byte buffer.
     */
    @Benchmark
    public void cppNativeByteArrayImagePipeline(Blackhole blackhole) {
        cpp.brightnessNative(nativePixels, 25);
        cpp.invertNative(nativePixels);
        cpp.thresholdNative(nativePixels, 128);
        blackhole.consume(nativePixels.get(nativePixels.length() - 1));
    }

    /**
     * C++ fused image kernel over a persistent native byte buffer.
     */
    @Benchmark
    public void cppNativeByteArrayFusedImagePipeline(Blackhole blackhole) {
        cpp.imagePipelineNative(nativeFusedPixels, 25, 128);
        blackhole.consume(nativeFusedPixels.get(nativeFusedPixels.length() - 1));
    }

    private static void brightnessJava(byte[] pixels, int delta) {
        for (int i = 0; i < pixels.length; i++) {
            int value = Byte.toUnsignedInt(pixels[i]) + delta;
            if (value > 255) {
                value = 255;
            } else if (value < 0) {
                value = 0;
            }
            pixels[i] = (byte) value;
        }
    }

    private static void invertJava(byte[] pixels) {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = (byte) (255 - Byte.toUnsignedInt(pixels[i]));
        }
    }

    private static void thresholdJava(byte[] pixels, int threshold) {
        if (threshold < 0) {
            threshold = 0;
        } else if (threshold > 255) {
            threshold = 255;
        }

        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = (byte) (Byte.toUnsignedInt(pixels[i]) >= threshold ? 255 : 0);
        }
    }

    private static void imagePipelineJava(byte[] pixels, int delta, int threshold) {
        if (threshold < 0) {
            threshold = 0;
        } else if (threshold > 255) {
            threshold = 255;
        }

        for (int i = 0; i < pixels.length; i++) {
            int bright = Byte.toUnsignedInt(pixels[i]) + delta;
            if (bright > 255) {
                bright = 255;
            } else if (bright < 0) {
                bright = 0;
            }

            int inverted = 255 - bright;
            pixels[i] = (byte) (inverted >= threshold ? 255 : 0);
        }
    }
}
