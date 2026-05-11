package dev.cppbridge.annotations;

import dev.cppbridge.BuildMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Java interface as a CppBridgeJ native API.
 *
 * <p>The annotated interface is loaded through {@code CppBridge.load(...)}.
 * Methods are mapped to native symbols by {@link CppFunction} or by their Java
 * method names when no {@link CppFunction} value is provided.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CppModule {
    /**
     * Explicit path to the native shared library.
     *
     * <p>Examples: {@code target/native/libfastmath.dylib},
     * {@code target/native/libfastmath.so}, {@code target/native/fastmath.dll}.</p>
     *
     * @return explicit native library path, or an empty string to resolve by
     *         {@link #libraryName()}
     */
    String libraryPath() default "";

    /**
     * Logical native library name without platform prefix or suffix.
     *
     * <p>For example, {@code fastmath} resolves to {@code libfastmath.dylib} on
     * macOS, {@code libfastmath.so} on Linux, and {@code fastmath.dll} on
     * Windows.</p>
     *
     * @return logical native library name
     */
    String libraryName() default "";

    /**
     * Directory where the native library is expected to be located.
     *
     * @return native output directory
     */
    String outputDirectory() default "target/native";

    /**
     * Backend mode used for this module.
     *
     * @return requested backend mode
     */
    BuildMode mode() default BuildMode.NATIVE;
}
