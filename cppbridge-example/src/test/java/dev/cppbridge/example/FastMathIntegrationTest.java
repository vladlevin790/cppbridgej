package dev.cppbridge.example;

import dev.cppbridge.CppBridge;
import dev.cppbridge.diagnostics.BindingReport;
import dev.cppbridge.diagnostics.BindingStatus;
import dev.cppbridge.memory.NativeByteArray;
import dev.cppbridge.memory.NativeDoubleArray;
import dev.cppbridge.memory.NativeLongArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FastMathIntegrationTest {
    @Test
    void bindingReportIsHealthyAfterMavenPluginBuildsNativeLibrary() {
        BindingReport report = CppBridge.inspect(FastMath.class);

        assertTrue(report.libraryExists(), report.toText());
        assertTrue(report.isHealthy(), report.toText());
        assertTrue(report.entries().stream().allMatch(entry -> entry.status() == BindingStatus.OK), report.toText());
    }

    @Test
    void callsScalarArrayAndNativeArrayKernels() {
        FastMath math = CppBridge.load(FastMath.class);

        assertEquals(30, math.sum(10, 20));

        double[] values = {10.0, 20.0, 30.0, 40.0};
        assertEquals(25.0, math.average(values), 0.000001);

        math.multiplyEach(values, 2.5);
        assertArrayEquals(new double[] {25.0, 50.0, 75.0, 100.0}, values, 0.000001);

        try (NativeDoubleArray nativeValues = NativeDoubleArray.copyOf(new double[] {1.0, 2.0, 3.0, 4.0})) {
            math.multiplyEachNative(nativeValues, 10.0);
            assertArrayEquals(new double[] {10.0, 20.0, 30.0, 40.0}, nativeValues.toArray(), 0.000001);
        }

        assertEquals(15L, math.sumLongArray(new long[] {1, 2, 3, 4, 5}));

        try (NativeLongArray nativeLongValues = NativeLongArray.copyOf(new long[] {1, 2, 3, 4, 5})) {
            assertEquals(15L, math.sumLongArrayNative(nativeLongValues));
        }

        byte[] pixels = {(byte) 10, (byte) 100, (byte) 250};
        math.brighten(pixels, 20);
        assertEquals(30, Byte.toUnsignedInt(pixels[0]));
        assertEquals(120, Byte.toUnsignedInt(pixels[1]));
        assertEquals(255, Byte.toUnsignedInt(pixels[2]));

        try (NativeByteArray nativePixels = NativeByteArray.copyOf(new byte[] {(byte) 10, (byte) 100, (byte) 250})) {
            math.brightenNative(nativePixels, 20);
            assertEquals(30, nativePixels.getUnsigned(0));
            assertEquals(120, nativePixels.getUnsigned(1));
            assertEquals(255, nativePixels.getUnsigned(2));
        }
    }
}
