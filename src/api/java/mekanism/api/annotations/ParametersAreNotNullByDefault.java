package mekanism.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to declare that all parameters in a class are {@link @NotNull}
 */
@NotNull
@Nonnull//Note: Must use the javax nonnull for intellij to recognize it properly in warnings
@TypeQualifierDefault(ElementType.PARAMETER)
@Retention(RetentionPolicy.CLASS)
public @interface ParametersAreNotNullByDefault {
}