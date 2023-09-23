package mekanism.common.integration.computer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import mekanism.common.integration.computer.MethodRestriction;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;

/**
 * Wraps fields as getters and methods with return types into one or more "synthetic" methods, based on the wrapper class.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface WrappingComputerMethod {

    /**
     * Wrapper class that defines how to create methods to wrap the return type of the annotated element.
     */
    Class<? extends SpecialComputerMethodWrapper> wrapper();

    /**
     * Names of the methods, must be the same number as the number of methods defined in the {@link #wrapper()} and must be in the same order as the {@link #wrapper()}
     * defines its methods
     */
    String[] methodNames();

    /**
     * Restriction for the generated wrapped methods generated of whether they should be applied to a handler or not.
     */
    MethodRestriction restriction() default MethodRestriction.NONE;

    /**
     * Array of modids that are required for if the method wrappers should be generated and applied to a handler.
     */
    String[] requiredMods() default {};

    /**
     * Whether the generated wrapped methods are thread-safe or needs to be queued to run on the main thread.
     */
    boolean threadSafe() default false;

    /**
     * Describe the wrapped element, to be used in the %s placeholder of target methods' docs.
     */
    String docPlaceholder();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface WrappingComputerMethodIndex {

        int value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface WrappingComputerMethodHelp {

        /**
         * Describes the target method, should have a single %s placeholder to denote the subject.
         */
        String value();
    }
}
