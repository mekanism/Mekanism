package mekanism.common.tile.base;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.capabilities.resolver.manager.ICapabilityHandlerManager;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CapabilityTileEntity extends TileEntityUpdateable {

    //Note: The below providers assume that the capability if supported has been added by either addCapabilityResolver or addCapabilityResolvers
    public static final ICapabilityProvider<CapabilityTileEntity, @Nullable Direction, IChemicalHandler> CHEMICAL_HANDLER_PROVIDER = basicCapabilityProvider(Capabilities.CHEMICAL.block());
    public static final ICapabilityProvider<CapabilityTileEntity, @Nullable Direction, IHeatHandler> HEAT_HANDLER_PROVIDER = basicCapabilityProvider(Capabilities.HEAT);
    public static final ICapabilityProvider<CapabilityTileEntity, @Nullable Direction, IItemHandler> ITEM_HANDLER_PROVIDER = basicCapabilityProvider(Capabilities.ITEM.block());
    public static final ICapabilityProvider<CapabilityTileEntity, @Nullable Direction, IFluidHandler> FLUID_HANDLER_PROVIDER = basicCapabilityProvider(Capabilities.FLUID.block());

    public static <CAP> ICapabilityProvider<CapabilityTileEntity, @Nullable Direction, CAP> basicCapabilityProvider(BlockCapability<CAP, @Nullable Direction> capability) {
        return (tile, context) -> {
            if (tile.capabilityCache.isCapabilityDisabled(capability, context)) {
                return null;
            }
            ICapabilityResolver<@Nullable Direction> resolver = tile.capabilityCache.getResolver(capability);
            return resolver == null ? null : resolver.resolve(capability, context);
        };
    }

    public static <TILE extends CapabilityTileEntity, CAP> ICapabilityProvider<TILE, @Nullable Direction, CAP> capabilityProvider(
          BlockCapability<CAP, @Nullable Direction> capability, BiFunction<TILE, BlockCapability<CAP, @Nullable Direction>, ICapabilityResolver<@Nullable Direction>> resolverGetter) {
        return (tile, context) -> {
            CapabilityCache capabilityCache = ((CapabilityTileEntity) tile).capabilityCache;
            if (capabilityCache.isCapabilityDisabled(capability, context)) {
                return null;
            }
            return capabilityCache.getResolver(capability, () -> resolverGetter.apply(tile, capability))
                  .resolve(capability, context);
        };
    }

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

    protected final void addCapabilityResolver(ICapabilityResolver<@Nullable Direction> resolver) {
        capabilityCache.addCapabilityResolver(resolver);
    }

    protected final void addConfigComponent(TileComponentConfig config) {
        capabilityCache.addConfigComponent(config);
    }

    /**
     * Invalidates our backing internal representations for certain capabilities in addition to actually notifying the level of capability invalidation.
     */
    public void invalidateCapabilitiesFull() {
        //Clear our internal cached capability instances and then invalidate the capabilities to the world
        // that way when queried from the invalidation listener we will ensure we can provide the up to date instance
        capabilityCache.invalidateAll();
        invalidateCapabilities();
        if (level != null) {
            BlockState state = getBlockState();
            AttributeHasBounding attribute = Attribute.get(state, AttributeHasBounding.class);
            if (attribute != null) {
                //Mark all bounding positions as having invalid caps
                attribute.handle(level, worldPosition, state, null, (world, pos, ignoredData) -> {
                    world.invalidateCapabilities(pos);
                    return true;
                });
            }
        }
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

    public final void invalidateCapabilityAll(@NotNull BlockCapability<?, @Nullable Direction> capability) {
        capabilityCache.invalidateAll(capability);
        invalidateCapabilities();
    }

    public final void invalidateCapabilities(@NotNull Collection<BlockCapability<?, @Nullable Direction>> capabilities, @Nullable Direction side) {
        for (BlockCapability<?, @Nullable Direction> capability : capabilities) {
            capabilityCache.invalidate(capability, side);
        }
        invalidateCapabilities();
    }

    public final void invalidateCapabilitiesAll(@NotNull Collection<BlockCapability<?, @Nullable Direction>> capabilities) {
        for (BlockCapability<?, @Nullable Direction> capability : capabilities) {
            capabilityCache.invalidateAll(capability);
        }
        invalidateCapabilities();
    }
}