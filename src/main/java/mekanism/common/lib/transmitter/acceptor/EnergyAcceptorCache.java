package mekanism.common.lib.transmitter.acceptor;

import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.integration.energy.IEnergyCompat.CacheConverter;
import mekanism.common.integration.energy.StrictEnergyCompat;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache.CacheBasedInfo;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyAcceptorCache extends AbstractAcceptorCache<IStrictEnergyHandler, AcceptorInfo<IStrictEnergyHandler>> {

    public EnergyAcceptorCache(Transmitter<IStrictEnergyHandler, ?, ?> transmitter, TileEntityTransmitter transmitterTile) {
        super(transmitter, transmitterTile);
    }

    /**
     * @apiNote Only call this from the server side
     */
    public boolean hasStrictEnergyHandlerAndListen(ServerLevel level, BlockPos pos, Direction side) {
        //TODO: Make sure we are removing it from cachedAcceptors when it becomes invalidated or the next call to this will error
        // Is this even correct??? The cache only actually gets removed if isValid is false
        AcceptorInfo<IStrictEnergyHandler> cachedAcceptor = cachedAcceptors.get(side);
        if (cachedAcceptor != null) {
            return cachedAcceptor.acceptor() != null;
        }
        //TODO - 1.20.2: Validate usages of opposite and make sure we have the ones that should be it
        Direction opposite = side.getOpposite();
        for (IEnergyCompat energyCompat : EnergyCompatUtils.getCompats()) {
            if (energyCompat.isUsable()) {
                //TODO: Is there some way we can do this without having to effectively look it up twice
                // as we don't want to use the BlockCapabilityCache which will add an invalidation listener, if there is no type present
                if (level.getCapability(energyCompat.getCapability().block(), pos, opposite) != null) {
                    RefreshListener refreshListener = getRefreshListener(side);
                    if (energyCompat instanceof StrictEnergyCompat) {
                        //We don't need to perform any wrapping so can just use a direct implementation
                        //TODO: Do we want a helper to create the cache based info?
                        cachedAcceptors.put(side, new CacheBasedInfo<>(BlockCapabilityCache.create(
                              Capabilities.STRICT_ENERGY.block(),
                              level,
                              pos,
                              opposite,
                              refreshListener,
                              refreshListener
                        )));
                    } else {
                        //TODO: Add some sort of comment
                        cachedAcceptors.put(side, new WrappingAcceptorInfo<>(energyCompat.getCacheAndConverter(level, pos, opposite, refreshListener, refreshListener)));
                    }
                    return true;
                }
            }
        }
        //TODO - 1.20.2: Add an invalidation listener to the level so that we can recheck it as we didn't add one above (and we don't need to listen to state changes anymore)
        // Can we add it directly on the level here or do we need a block capability cache so we can have an element get returned and stored in cachedAcceptors?
        return false;
    }

    public static class WrappingAcceptorInfo<RAW> implements AcceptorInfo<IStrictEnergyHandler> {

        private final BlockCapabilityCache<RAW, @Nullable Direction> rawCache;
        private final Function<RAW, IStrictEnergyHandler> convertToStrict;

        public WrappingAcceptorInfo(CacheConverter<RAW> converter) {
            this(converter.rawCache(), converter.convertToStrict());
        }

        public WrappingAcceptorInfo(BlockCapabilityCache<RAW, @Nullable Direction> rawCache, Function<RAW, IStrictEnergyHandler> convertToStrict) {
            this.rawCache = rawCache;
            this.convertToStrict = convertToStrict;
        }

        @Nullable
        @Override
        public IStrictEnergyHandler acceptor() {
            RAW capability = rawCache.getCapability();
            if (capability == null) {
                return null;
            }
            //TODO - 1.20.2: CACHE THIS RESULT, the block capability cache is mostly re-usable
            // so we need to make our invalidation listener also nuke a cached result that we would be storing here
            // though we might want to be adjusting which one we return after invalidation
            return convertToStrict.apply(capability);
        }
    }
}