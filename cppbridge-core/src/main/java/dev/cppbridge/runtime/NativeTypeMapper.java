package dev.cppbridge.runtime;

import dev.cppbridge.CppBridgeException;
import dev.cppbridge.memory.NativeByteArray;
import dev.cppbridge.memory.NativeDoubleArray;
import dev.cppbridge.memory.NativeFloatArray;
import dev.cppbridge.memory.NativeIntArray;
import dev.cppbridge.memory.NativeLongArray;

import java.lang.foreign.ValueLayout;

final class NativeTypeMapper {
    private NativeTypeMapper() {
    }

    static boolean isPrimitiveArray(Class<?> type) {
        return type == byte[].class
                || type == int[].class
                || type == long[].class
                || type == float[].class
                || type == double[].class;
    }

    static boolean isManagedNativeArray(Class<?> type) {
        return type == NativeByteArray.class
                || type == NativeIntArray.class
                || type == NativeLongArray.class
                || type == NativeFloatArray.class
                || type == NativeDoubleArray.class;
    }

    static boolean isArrayLike(Class<?> type) {
        return isPrimitiveArray(type) || isManagedNativeArray(type);
    }

    static ValueLayout valueLayoutForScalar(Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return ValueLayout.JAVA_INT;
        }
        if (type == long.class || type == Long.class) {
            return ValueLayout.JAVA_LONG;
        }
        if (type == float.class || type == Float.class) {
            return ValueLayout.JAVA_FLOAT;
        }
        if (type == double.class || type == Double.class) {
            return ValueLayout.JAVA_DOUBLE;
        }
        if (type == byte.class || type == Byte.class) {
            return ValueLayout.JAVA_BYTE;
        }

        throw new CppBridgeException("Unsupported scalar type: " + type.getName());
    }

    static ValueLayout valueLayoutForArray(Class<?> arrayType) {
        if (arrayType == byte[].class || arrayType == NativeByteArray.class) {
            return ValueLayout.JAVA_BYTE;
        }
        if (arrayType == int[].class || arrayType == NativeIntArray.class) {
            return ValueLayout.JAVA_INT;
        }
        if (arrayType == long[].class || arrayType == NativeLongArray.class) {
            return ValueLayout.JAVA_LONG;
        }
        if (arrayType == float[].class || arrayType == NativeFloatArray.class) {
            return ValueLayout.JAVA_FLOAT;
        }
        if (arrayType == double[].class || arrayType == NativeDoubleArray.class) {
            return ValueLayout.JAVA_DOUBLE;
        }

        throw new CppBridgeException("Unsupported array type: " + arrayType.getName());
    }

    static int arrayLength(Object array) {
        if (array instanceof byte[] v) {
            return v.length;
        }
        if (array instanceof NativeByteArray v) {
            return v.length();
        }
        if (array instanceof int[] v) {
            return v.length;
        }
        if (array instanceof NativeIntArray v) {
            return v.length();
        }
        if (array instanceof long[] v) {
            return v.length;
        }
        if (array instanceof NativeLongArray v) {
            return v.length();
        }
        if (array instanceof float[] v) {
            return v.length;
        }
        if (array instanceof NativeFloatArray v) {
            return v.length();
        }
        if (array instanceof double[] v) {
            return v.length;
        }
        if (array instanceof NativeDoubleArray v) {
            return v.length();
        }

        throw new CppBridgeException("Unsupported array value: " + array.getClass().getName());
    }
}
