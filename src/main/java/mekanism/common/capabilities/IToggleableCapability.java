package mekanism.common.capabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 * A capability provider that allows for easier toggling of capabilities in subclasses where everything else may be the
 * same but the capability may not always be enabled.
 */
public interface IToggleableCapability extends ICapabilityProvider {

    /**
     * Checks if a given capability is disabled for this provider on the given side. If false is returned it makes makes
     * no guarantees that the capability is provided.
     *
     * @param capability The capability to check
     * @param side The Side to check from: CAN BE NULL. Null is defined to represent 'internal' or 'self'
     * @return True if this given capability is disabled for the given side. If true, then {@link
     * #hasCapability(Capability, EnumFacing)} should return false.
     */
    default boolean isCapabilityDisabled(@Nonnull Capability<?> capability, @Nullable EnumFacing side) {
        return false;
    }
}