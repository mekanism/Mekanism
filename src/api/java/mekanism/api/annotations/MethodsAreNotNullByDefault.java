package mekanism.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to declare that all methods in a class are {@link @NotNull}
 */
@NotNull
@Nonnull//Note: Must use the javax nonnull for intellij to recognize it properly in warnings
@TypeQualifierDefault(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface MethodsAreNotNullByDefault {//TODO - 26.1: Replace this and the other ones with appropriate package infos and jspecify usage?
}