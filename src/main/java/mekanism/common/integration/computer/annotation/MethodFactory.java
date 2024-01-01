package mekanism.common.integration.computer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker interface for a generated ComputerMethodFactory Used by the 2nd annotation generator to gather the method factories
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface MethodFactory {

    /**
     * The class that this factory handles. (i.e. its type parameter)
     */
    Class<?> target();
}
