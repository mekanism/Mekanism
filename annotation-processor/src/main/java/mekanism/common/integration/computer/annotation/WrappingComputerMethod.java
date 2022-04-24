package mekanism.common.integration.computer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @apiNote It is important this method is in the same package as the actual annotation so that it can be resolved and used by the processor without having to add a ton
 * of dependencies to the processor to get it to be able to compile the entirety of Mekanism as a dependency.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface WrappingComputerMethod {
}