package mekanism.common.tile.base;

import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.capabilities.resolver.manager.ICapabilityHandlerManager;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CapabilityTileEntity extends TileEntityUpdateable {

    private final CapabilityCache capabilityCache = new CapabilityCache();

    public CapabilityTileEntity(TileEntityTypeRegistryObject<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected final void addCapabilityResolvers(List<ICapabilityHandlerManager<?>> capabilityHandlerManagers) {
        for (ICapabilityHandlerManager<?> capabilityHandlerManager : capabilityHandlerManagers) {
            //Add all managers that we support in our tile, as capability resolvers
            if (capabilityHandlerManager.canHandle()) {
                capabilityCache.addCapabilityResolver(capabilityHandlerManager);
            }
        }
    }

    @SafeVarargs
    protected final void addDisabledCapabilities(BlockCapability<?, @Nullable Direction>... capabilities) {
        capabilityCache.addDisabledCapabilities(capabilities);
    }

    protected final void addDisabledCapabilities(Collection<BlockCapability<?, @Nullable Direction>> capabilities) {
        capabilityCache.addDisabledCapabilities(capabilities);
    }

    protected final void addSemiDisabledCapability(BlockCapability<?, @Nullable Direction> capability, BooleanSupplier checker) {
        capabilityCache.addSemiDisabledCapability(capability, checker);
    }

    protected final void addConfigComponent(TileComponentConfig config) {
        capabilityCache.addConfigComponent(config);
    }

    @Nullable//TODO: If we don't actually have a use for this remove it
    private ICapabilityResolver<@Nullable Direction> getResolver(BlockCapability<?, @Nullable Direction> capability) {
        return capabilityCache.getResolver(capability);
    }

    private ICapabilityResolver<@Nullable Direction> getResolver(BlockCapability<?, @Nullable Direction> capability, Supplier<ICapabilityResolver<@Nullable Direction>> resolver) {
        return capabilityCache.getResolver(capability, resolver);
    }

    //TODO: Document that this and getResolver should only be called from within capability providers?
    @Nullable
    public <CAP> CAP getCapability(BlockCapability<CAP, @Nullable Direction> capability, Supplier<ICapabilityResolver<@Nullable Direction>> resolver, @Nullable Direction context) {
        if (capabilityCache.isCapabilityDisabled(capability, context)) {
            //TODO: Test that this works??
            return null;
        }
        return getResolver(capability, resolver)
              .resolve(capability, context);
    }

    //TODO: Document that this and getResolver should only be called from within capability providers?
    @Nullable
    public <CAP> CAP getCapability(BlockCapability<CAP, @Nullable Direction> capability, @Nullable Direction context) {
        if (capabilityCache.isCapabilityDisabled(capability, context)) {
            //TODO: Test that this works??
            return null;
        }
        ICapabilityResolver<@Nullable Direction> resolver = getResolver(capability);
        return resolver == null ? null : resolver.resolve(capability, context);
    }

    @Override
    public void setLevel(@NotNull Level world) {
        super.setLevel(world);
        if (level instanceof ServerLevel serverLevel) {
            //TODO - 1.20.2: Do we need to validate that no one called setLevel a second time and changed it
            // and then if that is possible do we need to check if the level changed in our still valid check
            //TODO: override clearRemoved and add this back
            serverLevel.registerCapabilityListener(worldPosition, () -> {
                //When the capabilities on our tile get invalidated, make sure to also invalidate all our backing cached values
                capabilityCache.invalidateAll();
                return !isRemoved();
            });
        }
    }

    public void invalidateCachedCapabilities() {
        capabilityCache.invalidateAll();
        invalidateCapabilities();
        //TODO: re-evaluate all the capability invalidation stuff. I think cache wise we should somehow be able to cache our capability implementation
        // and then need to use us invalidating our caps to know when to clear them
        // TODO - 1.20.2: We also probably want to override invalidateCapabilities to capture cases like when the tile is removed
    }

    public void invalidateCapability(@NotNull BlockCapability<?, @Nullable Direction> capability, @Nullable Direction side) {
        capabilityCache.invalidate(capability, side);
        invalidateCapabilities();
        //This by proxy of the listener then invalidates all our capabilities so we don't really need to invalidate by specific ones
        //TODO: FIX ^
    }

    public void invalidateCapability(@NotNull BlockCapability<?, @Nullable Direction> capability, Direction... sides) {
        capabilityCache.invalidateSides(capability, sides);
        invalidateCapabilities();
        //This by proxy of the listener then invalidates all our capabilities so we don't really need to invalidate by specific ones
        //TODO: FIX ^
    }

    public void invalidateCapabilities(@NotNull Collection<BlockCapability<?, @Nullable Direction>> capabilities, @Nullable Direction side) {
        for (BlockCapability<?, @Nullable Direction> capability : capabilities) {
            invalidateCapability(capability, side);
        }
        //This by proxy of the listener then invalidates all our capabilities so we don't really need to invalidate by specific ones
        //TODO: FIX ^
    }

    public void invalidateCapabilities(@NotNull Collection<BlockCapability<?, @Nullable Direction>> capabilities, Direction... sides) {
        for (BlockCapability<?, @Nullable Direction> capability : capabilities) {
            invalidateCapability(capability, sides);
        }
        //This by proxy of the listener then invalidates all our capabilities so we don't really need to invalidate by specific ones
        //TODO: FIX ^
    }
}