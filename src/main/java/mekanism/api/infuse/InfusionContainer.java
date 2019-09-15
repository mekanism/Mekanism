package mekanism.api.infuse;

import javax.annotation.Nullable;

/**
 * Common interface for things that store an infusion type + amount
 */
public interface InfusionContainer {

    /**
     * The type of infusion ingredient stored
     * @return the type or null if empty
     */
    @Nullable
    InfuseType getType();

    /**
     * The amount of infusion input container.
     * @return the amount, must be 0 if type is null
     */
    int getAmount();

    //TODO: 1.14 maybe move where this is declared/how it works slightly
    default boolean isEmpty() {
        return getType() == null || getAmount() == 0;
    }
}
