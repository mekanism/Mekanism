package mekanism.common.integration.computer.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marker interface for a generated ComputerMethodFactory
 * Used by the 2nd annotation generator to gather the method factories
 */
@Retention(RetentionPolicy.CLASS)
public @interface MethodFactory {
    /**
     * The class that this factory handles. (i.e. its type parameter)
     */
    Class<?> target();
}
