package mekanism.common.lib.transmitter.acceptor;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.lib.transmitter.acceptor.EnergyAcceptorCache.EnergyAcceptorInfo;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyAcceptorCache extends AbstractAcceptorCache<IStrictEnergyHandler, EnergyAcceptorInfo> {

    public EnergyAcceptorCache(Transmitter<IStrictEnergyHandler, ?, ?> transmitter, TileEntityTransmitter transmitterTile) {
        super(transmitter, transmitterTile);
    }

    /**
     * @apiNote Only call this from the server side
     */
    public boolean hasStrictEnergyHandlerAndListen(ServerLevel level, BlockPos pos, Direction side) {
        //TODO: Make sure we are removing it from cachedAcceptors when it becomes invalidated or the next call to this will error
        // Is this even correct??? The cache only actually gets removed if isValid is false
        // This definitely needs more thought and handling related to when a position potentially changes which base energy cap it is providing
        EnergyAcceptorInfo cachedAcceptor = cachedAcceptors.get(side);
        if (cachedAcceptor != null) {
            return cachedAcceptor.acceptor() != null;
        }
        Direction opposite = side.getOpposite();
        EnergyAcceptorInfo acceptorInfo = new EnergyAcceptorInfo();
        cachedAcceptors.put(side, acceptorInfo);
        for (IEnergyCompat energyCompat : EnergyCompatUtils.getCompats()) {
            if (energyCompat.capabilityExists()) {
                acceptorInfo.addCapability(energyCompat, level, pos, opposite, getRefreshListener(side));
            }
        }
        return acceptorInfo.acceptor() != null;
    }

    public static class EnergyAcceptorInfo implements AcceptorInfo<IStrictEnergyHandler> {

        private record CacheInfo(IEnergyCompat energyCompat, BlockCapabilityCache<?, @Nullable Direction> cache) {
        }

        private final List<CacheInfo> capabilities = new ArrayList<>();

        EnergyAcceptorInfo() {
        }

        void addCapability(IEnergyCompat energyCompat, ServerLevel level, BlockPos pos, Direction opposite, RefreshListener refreshListener) {
            capabilities.add(new CacheInfo(energyCompat, energyCompat.getCapability().createCache(level, pos, opposite, refreshListener, refreshListener)));
        }

        @Nullable
        @Override
        public IStrictEnergyHandler acceptor() {
            for (CacheInfo cacheInfo : capabilities) {
                IEnergyCompat energyCompat = cacheInfo.energyCompat();
                //Validate that the energy compat is still usable
                //TODO: Re-evaluate this
                if (energyCompat.isUsable()) {
                    Object capability = cacheInfo.cache().getCapability();
                    if (capability != null) {
                        //TODO - 1.20.2: CACHE THIS RESULT, the block capability cache is mostly re-usable
                        // so we need to make our invalidation listener also nuke a cached result that we would be storing here
                        // though we might want to be adjusting which one we return after invalidation
                        return energyCompat.wrapAsStrictEnergyHandler(capability);
                    }
                }
            }
            return null;
        }
    }
}