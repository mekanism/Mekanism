package mekanism.common.tile.transmitter;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.item.CursedTransporterItemHandler;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityLogisticalTransporterBase extends TileEntityTransmitter {

    protected TileEntityLogisticalTransporterBase(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        addCapabilityResolver(new TransporterCapabilityResolver());
    }

    @Override
    protected abstract LogisticalTransporterBase createTransmitter(IBlockProvider blockProvider);

    @Override
    public LogisticalTransporterBase getTransmitter() {
        return (LogisticalTransporterBase) super.getTransmitter();
    }

    public static void tickClient(Level level, BlockPos pos, BlockState state, TileEntityLogisticalTransporterBase transmitter) {
        transmitter.getTransmitter().onUpdateClient();
    }

    @Override
    public void onUpdateServer() {
        super.onUpdateServer();
        getTransmitter().onUpdateServer();
    }

    @Override
    public void blockRemoved() {
        super.blockRemoved();
        if (!isRemote()) {
            LogisticalTransporterBase transporter = getTransmitter();
            if (!transporter.isUpgrading()) {
                //If the transporter is not currently being upgraded, drop the contents
                for (TransporterStack stack : transporter.getTransit()) {
                    TransporterUtils.drop(transporter, stack);
                }
            }
        }
    }

    @Override
    public void sideChanged(@NotNull Direction side, @NotNull ConnectionType old, @NotNull ConnectionType type) {
        super.sideChanged(side, old, type);
        //Note: We don't expose a cap for when the connection type is none or push and this method only gets called if type != old,
        // so we can check to ensure that if we are one of the two that the other isn't the other one we don't have a cap for
        if (type == ConnectionType.NONE && old != ConnectionType.PUSH ||
            type == ConnectionType.PUSH && old != ConnectionType.NONE) {
            invalidateCapability(ForgeCapabilities.ITEM_HANDLER, side);
            //Notify the neighbor on that side our state changed and we no longer have a capability
            WorldUtils.notifyNeighborOfChange(level, side, worldPosition);
        } else if (old == ConnectionType.NONE && type != ConnectionType.PUSH ||
                   old == ConnectionType.PUSH && type != ConnectionType.NONE) {
            //Notify the neighbor on that side our state changed, and we now do have a capability
            WorldUtils.notifyNeighborOfChange(level, side, worldPosition);
        }
    }

    @NothingNullByDefault
    private class TransporterCapabilityResolver implements ICapabilityResolver {

        private static final List<Capability<?>> SUPPORTED_CAPABILITY = Collections.singletonList(ForgeCapabilities.ITEM_HANDLER);

        private final Map<Direction, CursedTransporterItemHandler> cursedHandlers = new EnumMap<>(Direction.class);
        private final Map<Direction, LazyOptional<IItemHandler>> handlers = new EnumMap<>(Direction.class);

        @Override
        public List<Capability<?>> getSupportedCapabilities() {
            return SUPPORTED_CAPABILITY;
        }

        /**
         * Lazily get and cache a handler instance for the given side, and make it be read only if something else is trying to interact with us using the null side
         */
        @Override
        public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
            if (side == null) {
                //We provide no readonly item handler view
                return LazyOptional.empty();
            }
            LazyOptional<IItemHandler> cachedCapability = handlers.get(side);
            if (cachedCapability == null || !cachedCapability.isPresent()) {
                LogisticalTransporterBase transporter = getTransmitter();
                //Note: We check here whether it exposes the cap rather than in the cap itself as we invalidate the cached cap whenever this changes
                if (transporter.exposesInsertCap(side)) {
                    handlers.put(side, cachedCapability = LazyOptional.of(() ->
                          cursedHandlers.computeIfAbsent(side, s -> new CursedTransporterItemHandler(transporter, worldPosition.relative(s),
                          () -> level == null ? -1 : level.getGameTime()))));
                } else {
                    return LazyOptional.empty();
                }
            }
            return cachedCapability.cast();
        }

        @Override
        public void invalidate(Capability<?> capability, @Nullable Direction side) {
            if (side != null) {
                invalidate(handlers.get(side));
            }
        }

        @Override
        public void invalidateAll() {
            handlers.values().forEach(this::invalidate);
        }

        protected void invalidate(@Nullable LazyOptional<?> cachedCapability) {
            if (cachedCapability != null && cachedCapability.isPresent()) {
                cachedCapability.invalidate();
            }
        }
    }
}