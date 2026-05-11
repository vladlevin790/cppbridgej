package dev.cppbridge.runtime;

import dev.cppbridge.ArrayDirection;
import dev.cppbridge.CppBridgeException;
import dev.cppbridge.memory.NativeByteArray;
import dev.cppbridge.memory.NativeDoubleArray;
import dev.cppbridge.memory.NativeFloatArray;
import dev.cppbridge.memory.NativeIntArray;
import dev.cppbridge.memory.NativeLongArray;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

final class NativeArrayMemory {
    private NativeArrayMemory() {
    }

    static boolean isManagedNativeArray(Class<?> type) {
        return type == NativeByteArray.class
                || type == NativeIntArray.class
                || type == NativeLongArray.class
                || type == NativeFloatArray.class
                || type == NativeDoubleArray.class;
    }

    static MemorySegment segmentOfManagedNativeArray(Object value) {
        if (value instanceof NativeByteArray nativeByteArray) {
            return nativeByteArray.segment();
        }
        if (value instanceof NativeIntArray nativeIntArray) {
            return nativeIntArray.segment();
        }
        if (value instanceof NativeLongArray nativeLongArray) {
            return nativeLongArray.segment();
        }
        if (value instanceof NativeFloatArray nativeFloatArray) {
            return nativeFloatArray.segment();
        }
        if (value instanceof NativeDoubleArray nativeDoubleArray) {
            return nativeDoubleArray.segment();
        }
        throw new CppBridgeException("Unsupported native array value: " + value.getClass().getName());
    }

    static int lengthOfManagedNativeArray(Object value) {
        if (value instanceof NativeByteArray nativeByteArray) {
            return nativeByteArray.length();
        }
        if (value instanceof NativeIntArray nativeIntArray) {
            return nativeIntArray.length();
        }
        if (value instanceof NativeLongArray nativeLongArray) {
            return nativeLongArray.length();
        }
        if (value instanceof NativeFloatArray nativeFloatArray) {
            return nativeFloatArray.length();
        }
        if (value instanceof NativeDoubleArray nativeDoubleArray) {
            return nativeDoubleArray.length();
        }
        throw new CppBridgeException("Unsupported native array value: " + value.getClass().getName());
    }

    static MemorySegment allocateAndCopy(Arena arena, Object array, ArrayDirection direction) {
        if (array instanceof byte[] values) {
            MemorySegment segment = arena.allocate(ValueLayout.JAVA_BYTE.byteSize() * values.length, ValueLayout.JAVA_BYTE.byteAlignment());
            if (direction != ArrayDirection.OUT) {
                segment.copyFrom(MemorySegment.ofArray(values));
            }
            return segment;
        }

        if (array instanceof int[] values) {
            MemorySegment segment = arena.allocate(ValueLayout.JAVA_INT.byteSize() * values.length, ValueLayout.JAVA_INT.byteAlignment());
            if (direction != ArrayDirection.OUT) {
                segment.copyFrom(MemorySegment.ofArray(values));
            }
            return segment;
        }

        if (array instanceof long[] values) {
            MemorySegment segment = arena.allocate(ValueLayout.JAVA_LONG.byteSize() * values.length, ValueLayout.JAVA_LONG.byteAlignment());
            if (direction != ArrayDirection.OUT) {
                segment.copyFrom(MemorySegment.ofArray(values));
            }
            return segment;
        }

        if (array instanceof float[] values) {
            MemorySegment segment = arena.allocate(ValueLayout.JAVA_FLOAT.byteSize() * values.length, ValueLayout.JAVA_FLOAT.byteAlignment());
            if (direction != ArrayDirection.OUT) {
                segment.copyFrom(MemorySegment.ofArray(values));
            }
            return segment;
        }

        if (array instanceof double[] values) {
            MemorySegment segment = arena.allocate(ValueLayout.JAVA_DOUBLE.byteSize() * values.length, ValueLayout.JAVA_DOUBLE.byteAlignment());
            if (direction != ArrayDirection.OUT) {
                segment.copyFrom(MemorySegment.ofArray(values));
            }
            return segment;
        }

        throw new IllegalArgumentException("Unsupported array type: " + array.getClass().getName());
    }

    static void copyBack(MemorySegment segment, Object array) {
        if (array instanceof byte[] values) {
            MemorySegment.ofArray(values).copyFrom(segment);
            return;
        }

        if (array instanceof int[] values) {
            MemorySegment.ofArray(values).copyFrom(segment);
            return;
        }

        if (array instanceof long[] values) {
            MemorySegment.ofArray(values).copyFrom(segment);
            return;
        }

        if (array instanceof float[] values) {
            MemorySegment.ofArray(values).copyFrom(segment);
            return;
        }

        if (array instanceof double[] values) {
            MemorySegment.ofArray(values).copyFrom(segment);
            return;
        }

        throw new IllegalArgumentException("Unsupported array type: " + array.getClass().getName());
    }
}
