package mekanism.api.container;

import mekanism.api.Action;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Helper to define generalized container interactions for use in our batch container utils. Mainly used for Fluids
 *
 * @since 10.5.13
 */
@FunctionalInterface
public interface IntContainerInteraction<TYPE> {

    /**
     * @param container Container index to interact with
     * @param amount    Amount being adjusted by the interaction.
     * @param side      The side we are interacting with the handler from (null for internal).
     * @param action    The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return Result of the interaction (for example the result of an insert or extraction)
     */
    TYPE interact(int container, int amount, @Nullable Direction side, Action action);
}