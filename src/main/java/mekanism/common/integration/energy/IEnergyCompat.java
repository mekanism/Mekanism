package mekanism.common.integration.energy;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.MultiTypeCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IEnergyCompat {

    /**
     * Whether this energy compat is actually enabled.
     *
     * @return if this energy compat is enabled.
     */
    boolean isUsable();

    /**
     * {@return true if the mods required for the capability this compat acts on are actually loaded}
     */
    default boolean capabilityExists() {
        return true;
    }

    /**
     * Gets the capability this compat integrates with.
     *
     * @return The capability this compat is integrating with.
     */
    MultiTypeCapability<?> getCapability();

    /**
     * Wraps a capability provider that provides an {@link IStrictEnergyHandler} as a one that provides the capability this energy compat is for.
     *
     * @param provider The capability provider to wrap
     *
     * @return A capability provider that provides this energy compat's capability.
     */
    <OBJECT, CONTEXT> ICapabilityProvider<OBJECT, CONTEXT, ?> getProviderAs(ICapabilityProvider<OBJECT, CONTEXT, IStrictEnergyHandler> provider);

    /**
     * Wraps a strict energy handler in a wrapper that matches this compat's capability.
     *
     * @param handler Handler to wrap.
     */
    Object wrapStrictEnergyHandler(IStrictEnergyHandler handler);

    /**
     * Wraps this compat's capability in a strict energy handler.
     *
     * @param handler Handler to wrap. Can be assumed to be the same type as this energy compat.
     */
    @Nullable
    IStrictEnergyHandler wrapAsStrictEnergyHandler(Object handler);

    /**
     * Wraps the capability implemented in the provider into a lazy optional {@link IStrictEnergyHandler}, or returns {@code LazyOptional.empty()} if the capability is
     * not implemented.
     *
     * @param level   Level to query.
     * @param pos     Position in level.
     * @param state   The block state, if known, or null if unknown.
     * @param tile    The block entity, if known, or null if unknown.
     * @param context Side
     *
     * @return The capability implemented in the provider into an {@link IStrictEnergyHandler}, or {@code null} if the capability is not implemented.
     */
    @Nullable
    default IStrictEnergyHandler getAsStrictEnergyHandler(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity tile, @Nullable Direction context) {
        Object capability = getCapability().getCapabilityIfLoaded(level, pos, state, tile, context);
        return capability == null ? null : wrapAsStrictEnergyHandler(capability);
    }

    /**
     * Gets an exposed capability of this compat's type from a stack and wraps it into a strict energy handler.
     *
     * @param stack ItemStack to check for the capability
     */
    @Nullable
    default IStrictEnergyHandler getStrictEnergyHandler(ItemStack stack) {
        Object capability = getCapability().getCapability(stack);
        return capability == null ? null : wrapAsStrictEnergyHandler(capability);
    }

    /**
     * Gets an exposed capability of this compat's type from an entity and wraps it into a strict energy handler.
     *
     * @param entity Entity to check for the capability
     */
    @Nullable
    default IStrictEnergyHandler getStrictEnergyHandler(Entity entity) {
        Object capability = getCapability().getCapability(entity);
        return capability == null ? null : wrapAsStrictEnergyHandler(capability);
    }
}