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
    //TODO: Do we want this to use RelativeSide rather than Direction?

    /**
     * Checks if a given capability is disabled for this provider on the given side. If false is returned it makes makes no guarantees that the capability is provided.
     *
     * @param capability The capability to check
     * @param side       The Side to check from: CAN BE NULL. Null is defined to represent 'internal' or 'self'
     *
     * @return True if this given capability is disabled for the given side. If true, then {@link #getCapability(Capability, Direction)} should return {@link
     * LazyOptional#empty()}.
     */
    default boolean isCapabilityDisabled(@Nonnull Capability<?> capability, @Nullable Direction side) {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Do not override this method if you are implementing {@link IToggleableCapability}, instead override {@link #getCapabilityIfEnabled(Capability,
     * Direction)}, calling this method is fine.
     */
    @Nonnull
    @Override
    default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        return isCapabilityDisabled(capability, side) ? LazyOptional.empty() : getCapabilityIfEnabled(capability, side);
    }

    /**
     * Copy of {@link ICapabilityProvider#getCapability(Capability, Direction)} but checks for if the capability is disabled before being called. Docs copied for
     * convenience
     *
     * Retrieves the Optional handler for the capability requested on the specific side. The return value <strong>CAN</strong> be the same for multiple faces. Modders are
     * encouraged to cache this value, using the listener capabilities of the Optional to be notified if the requested capability get lost.
     *
     * @param capability The capability to check
     * @param side       The Side to check from, <strong>CAN BE NULL</strong>. Null is defined to represent 'internal' or 'self'
     *
     * @return The requested an optional holding the requested capability.
     */
    @Nonnull
    <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side);
}