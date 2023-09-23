package mekanism.common.tile.interfaces;

import java.util.function.IntSupplier;

/**
 * Implement this if your machine/generator has some form of active state.
 */
public interface ITileActive {

    IntSupplier NO_DELAY = () -> 0;

    default boolean isActivatable() {
        return true;
    }

    /**
     * Gets the active state as a boolean.
     *
     * @return active state
     */
    boolean getActive();

    /**
     * Sets the active state to a new value.
     *
     * @param active - new active state
     */
    void setActive(boolean active);
}