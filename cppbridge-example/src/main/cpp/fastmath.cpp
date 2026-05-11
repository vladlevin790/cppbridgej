#include <cstdint>

#ifdef _WIN32
#define CPPBRIDGE_EXPORT extern "C" __declspec(dllexport)
#else
#define CPPBRIDGE_EXPORT extern "C"
#endif

CPPBRIDGE_EXPORT int sum_int(int a, int b) {
    return a + b;
}

CPPBRIDGE_EXPORT double average_double(double* values, int length) {
    if (length <= 0) {
        return 0.0;
    }

    double total = 0.0;
    for (int i = 0; i < length; i++) {
        total += values[i];
    }

    return total / length;
}

CPPBRIDGE_EXPORT void multiply_each_double(double* values, int length, double factor) {
    for (int i = 0; i < length; i++) {
        values[i] *= factor;
    }
}

CPPBRIDGE_EXPORT long long sum_long_array(long long* values, int length) {
    long long total = 0;
    for (int i = 0; i < length; i++) {
        total += values[i];
    }
    return total;
}

CPPBRIDGE_EXPORT void brighten_bytes(std::int8_t* values, int length, int amount) {
    for (int i = 0; i < length; i++) {
        int current = static_cast<unsigned char>(values[i]);
        int next = current + amount;
        if (next > 255) {
            next = 255;
        }
        values[i] = static_cast<std::int8_t>(next);
    }
}
