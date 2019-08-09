package mekanism.common.capabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

/**
 * A capability provider that allows for easier toggling of capabilities in subclasses where everything else may be the same but the capability may not always be
 * enabled.
 */
public interface IToggleableCapability extends ICapabilityProvider {
    //TODO: Decide if this should be used in all mekanism tiles??
    // Benefits would be that extending can easily make it so things can be disabled in the future

    /**
     * Checks if a given capability is disabled for this provider on the given side. If false is returned it makes makes no guarantees that the capability is provided.
     *
     * @param capability The capability to check
     * @param side       The Side to check from: CAN BE NULL. Null is defined to represent 'internal' or 'self'
     *
     * @return True if this given capability is disabled for the given side. If true, then {@link #getCapability(Capability, Direction)} should return
     * {@link LazyOptional#empty()}.
     */
    default boolean isCapabilityDisabled(@Nonnull Capability<?> capability, @Nullable Direction side) {
        return false;
    }
}