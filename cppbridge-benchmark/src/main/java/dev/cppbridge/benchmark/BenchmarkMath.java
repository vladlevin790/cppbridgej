package dev.cppbridge.benchmark;

import dev.cppbridge.ArrayDirection;
import dev.cppbridge.annotations.CppArray;
import dev.cppbridge.annotations.CppFunction;
import dev.cppbridge.annotations.CppModule;
import dev.cppbridge.memory.NativeByteArray;
import dev.cppbridge.memory.NativeDoubleArray;

@CppModule(libraryName = "benchmath")
/**
 * Native benchmark API used by JMH benchmark classes.
 */
public interface BenchmarkMath {
    @CppFunction("average_double")
    double average(@CppArray(ArrayDirection.IN) double[] values);

    @CppFunction("multiply_each_double")
    void multiplyEach(double[] values, double factor);

    @CppFunction("heavy_transform_double")
    void heavyTransform(double[] values);

    @CppFunction("average_double")
    double averageNative(NativeDoubleArray values);

    @CppFunction("multiply_each_double")
    void multiplyEachNative(NativeDoubleArray values, double factor);

    @CppFunction("heavy_transform_double")
    void heavyTransformNative(NativeDoubleArray values);

    @CppFunction("three_step_pipeline_double")
    void threeStepPipeline(double[] values, double factor);

    @CppFunction("three_step_pipeline_double")
    void threeStepPipelineNative(NativeDoubleArray values, double factor);

    @CppFunction("brightness_u8")
    void brightness(byte[] pixels, int delta);

    @CppFunction("invert_u8")
    void invert(byte[] pixels);

    @CppFunction("threshold_u8")
    void threshold(byte[] pixels, int threshold);

    @CppFunction("brightness_u8")
    void brightnessNative(NativeByteArray pixels, int delta);

    @CppFunction("invert_u8")
    void invertNative(NativeByteArray pixels);

    @CppFunction("threshold_u8")
    void thresholdNative(NativeByteArray pixels, int threshold);

    @CppFunction("image_pipeline_u8")
    void imagePipeline(byte[] pixels, int delta, int threshold);

    @CppFunction("image_pipeline_u8")
    void imagePipelineNative(NativeByteArray pixels, int delta, int threshold);
}
