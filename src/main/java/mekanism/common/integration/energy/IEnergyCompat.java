package mekanism.common.integration.energy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

@ParametersAreNonnullByDefault
public interface IEnergyCompat {

    /**
     * Whether or not this energy compat is actually enabled.
     *
     * @return if this energy compat is enabled.
     */
    boolean isUsable();

    /**
     * Gets the capability this compat integrates with.
     *
     * @return The capability this compat is integrating with. Or {@code null} if the capability is not usable.
     */
    @Nullable
    Capability<?> getCapability();

    /**
     * Checks if a given capability matches the capability that this {@link IEnergyCompat} is for.
     *
     * @param capability Capability to check
     *
     * @return {@code true} if the capability matches, {@code false} if it doesn't.
     */
    default boolean isMatchingCapability(Capability<?> capability) {
        return capability == getCapability();
    }

    /**
     * Checks if the given provider has this capability.
     *
     * @param provider Capability provider
     * @param side     Side
     *
     * @return {@code true} if the provider has this {@link IEnergyCompat}'s capability, {@code false} otherwise
     *
     * @implNote The capabilities should be kept lazy so that they are not resolved if they are not needed yet.
     */
    boolean isCapabilityPresent(ICapabilityProvider provider, @Nullable Direction side);

    /**
     * Gets the {@link IStrictEnergyHandler} as a lazy optional for the capability this energy compat is for.
     *
     * @param handler The handler to wrap
     *
     * @return A lazy optional for this capability
     */
    @Nonnull
    LazyOptional<?> getHandlerAs(IStrictEnergyHandler handler);

    /**
     * Wraps the capability implemented in the provider into a lazy optional {@link IStrictEnergyHandler}, or returns {@code LazyOptional.empty()} if the capability is
     * not implemented.
     *
     * @param provider Capability provider
     * @param side     Side
     *
     * @return The capability implemented in the provider into an {@link IStrictEnergyHandler}, or {@code null} if the capability is not implemented.
     */
    @Nonnull
    LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(ICapabilityProvider provider, @Nullable Direction side);
}