package mekanism.api.container;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Helper to define a generalized way to get the contents of a container.
 *
 * @since 10.6.6
 */
@FunctionalInterface
public interface InContainerGetterLong {

    /**
     * @param container Container index of the container to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return The object stored in the given container.
     */
    long getStored(int container, @Nullable Direction side);
}