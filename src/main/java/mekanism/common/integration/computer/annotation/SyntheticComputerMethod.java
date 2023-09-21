package mekanism.common.integration.computer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import mekanism.common.integration.computer.MethodRestriction;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface SyntheticComputerMethod {

    /**
     * Restriction for synthetic methods generated of whether the method should be applied to a handler or not.
     */
    MethodRestriction restriction() default MethodRestriction.NONE;

    /**
     * Array of modids that are required for synthetic methods to be generated and applied to a handler.
     */
    String[] requiredMods() default {};

    /**
     * Name to use for the synthetic getter.
     */
    String getter() default "";

    /**
     * Whether the synthetic getter is thread-safe or needs to be queued to run on the main thread.
     */
    boolean threadSafeGetter() default false;

    /**
     * Name to use for the synthetic setter.
     */
    String setter() default "";

    /**
     * Whether the synthetic setter is thread-safe or needs to be queued to run on the main thread.
     */
    boolean threadSafeSetter() default false;

    /**
     * Method description for getter documentation.
     */
    String getterDescription() default "";

    /**
     * Method description for setter documentation.
     */
    String setterDescription() default "";
}
