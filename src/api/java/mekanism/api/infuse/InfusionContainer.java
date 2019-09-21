package mekanism.api.infuse;

import javax.annotation.Nonnull;

/**
 * Common interface for things that store an infusion type + amount
 */
//TODO: Remove this??
public interface InfusionContainer {

    /**
     * The type of infusion ingredient stored
     * @return the type or null if empty
     */
    //TODO: Make this nonnull and have an "empty" infuse type
    @Nonnull
    InfuseType getType();

    /**
     * The amount of infusion input container.
     * @return the amount, must be 0 if type is null
     */
    int getAmount();

    boolean isEmpty();
}