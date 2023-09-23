package mekanism.common.integration.computer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import mekanism.common.integration.computer.MethodRestriction;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface ComputerMethod {

    /**
     * Name to use for the method instead of the actual internal java name.
     */
    String nameOverride() default "";

    /**
     * Restriction for this method of whether the method should be applied to a handler or not.
     */
    MethodRestriction restriction() default MethodRestriction.NONE;

    /**
     * Array of modids that are required for this method to be applied to a handler.
     */
    String[] requiredMods() default {};

    /**
     * Whether this method is thread-safe or needs to be queued to run on the main thread.
     */
    boolean threadSafe() default false;

    /**
     * Whether this method will fail when security is not public. Documentation property.
     */
    boolean requiresPublicSecurity() default false;

    /**
     * Method description for documentation.
     */
    String methodDescription() default "";

    /**
     * For {@link mekanism.common.integration.computer.Convertable} return types, specifies the possible values for documentation
     */
    Class[] possibleReturns() default {};
}
