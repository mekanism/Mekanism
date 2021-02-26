package mekanism.common.integration.computer;

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
     * Name to use for the synthetic setter.
     */
    String setter() default "";
}