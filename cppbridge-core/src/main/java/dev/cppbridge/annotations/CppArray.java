package dev.cppbridge.annotations;

import dev.cppbridge.ArrayDirection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures how a Java primitive array parameter is copied between Java heap
 * memory and native memory.
 *
 * <p>The annotation is used only for Java heap arrays such as {@code double[]},
 * {@code byte[]}, and {@code int[]}. Managed native arrays from
 * {@code dev.cppbridge.memory} are already stored off-heap and are passed
 * directly to native code.</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CppArray {
    /**
     * Defines whether the array is copied into native memory, copied back to
     * Java memory, or both.
     *
     * @return array marshalling direction
     */
    ArrayDirection value() default ArrayDirection.IN_OUT;
}
