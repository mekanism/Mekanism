package mekanism.api.container;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Helper to define a generalized way to get the contents of a container.
 *
 * @since 10.5.13
 */
@FunctionalInterface
public interface InContainerGetter<STORED> {

    /**
     * @param container Container index of the container to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return The object stored in the given container.
     */
    STORED getStored(int container, @Nullable Direction side);
}