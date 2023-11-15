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

    @Nullable
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

    //TODO: Rename this method to something better
    public void invalidateCachedCapabilities() {
        //Clear our internal cached capability instances and then invalidate the capabilities to the world
        // that way when queried from the invalidation listener we will ensure we can provide the up to date instance
        capabilityCache.invalidateAll();
        invalidateCapabilities();
    }

    @Override
    public void setRemoved() {
        //Note: Clear the backing caps before letting super invalidate as then if anything somehow queries us in their invalidation listeners
        // they will get the proper non cached data
        capabilityCache.invalidateAll();
        super.setRemoved();
    }

    @Override
    public void clearRemoved() {
        //Note: Clear the backing caps before letting super invalidate as then if anything somehow queries us in their invalidation listeners
        // they will get the proper non cached data
        capabilityCache.invalidateAll();
        super.clearRemoved();
    }

    public final void invalidateCapability(@NotNull BlockCapability<?, @Nullable Direction> capability, @Nullable Direction side) {
        capabilityCache.invalidate(capability, side);
        invalidateCapabilities();
    }

    public final void invalidateCapability(@NotNull BlockCapability<?, @Nullable Direction> capability, Direction... sides) {
        capabilityCache.invalidateSides(capability, sides);
        invalidateCapabilities();
    }

    public final void invalidateCapabilities(@NotNull Collection<BlockCapability<?, @Nullable Direction>> capabilities, @Nullable Direction side) {
        for (BlockCapability<?, @Nullable Direction> capability : capabilities) {
            capabilityCache.invalidate(capability, side);
        }
        invalidateCapabilities();
    }

    public final void invalidateCapabilities(@NotNull Collection<BlockCapability<?, @Nullable Direction>> capabilities, Direction... sides) {
        for (BlockCapability<?, @Nullable Direction> capability : capabilities) {
            capabilityCache.invalidateSides(capability, sides);
        }
        invalidateCapabilities();
    }
}