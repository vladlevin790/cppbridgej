package dev.cppbridge.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a Java interface method to an exported native symbol.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CppFunction {
    /**
     * Native symbol name. When empty, the Java method name is used as the
     * symbol name.
     *
     * @return exported native function name
     */
    String value() default "";
}
