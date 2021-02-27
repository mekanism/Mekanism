package mekanism.common.integration.computer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SyntheticComputerMethod {

    /**
     * Name to use for the synthetic getter.
     */
    String getter() default "";

    /**
     * Whether or not the synthetic getter is thread-safe or needs to be queued to run on the main thread.
     */
    boolean getterThreadSafe() default false;

    /**
     * Name to use for the synthetic setter.
     */
    String setter() default "";

    /**
     * Whether or not the synthetic setter is thread-safe or needs to be queued to run on the main thread.
     */
    boolean setterThreadSafe() default false;
}