package mekanism.common.lib.transmitter.acceptor;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
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

    public EnergyAcceptorCache(TileEntityTransmitter transmitterTile) {
        super(transmitterTile);
    }

    @Override
    protected EnergyAcceptorInfo initializeCache(ServerLevel level, BlockPos pos, Direction opposite, RefreshListener refreshListener) {
        EnergyAcceptorInfo acceptorInfo = new EnergyAcceptorInfo();
        for (IEnergyCompat energyCompat : EnergyCompatUtils.getCompats()) {
            if (energyCompat.capabilityExists()) {
                acceptorInfo.addCapability(energyCompat, level, pos, opposite, refreshListener);
            }
        }
        return acceptorInfo;
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
                //Validate that the energy compat is actually usable
                if (energyCompat.isUsable()) {
                    Object capability = cacheInfo.cache().getCapability();
                    if (capability != null) {
                        //TODO: If creating these wrappers ends up showing as a performance hotspot/causing issues for the GC
                        // we should look into seeing if we can somehow cache the wrapped object
                        IStrictEnergyHandler wrapped = energyCompat.wrapAsStrictEnergyHandler(capability);
                        if (wrapped != null) {
                            return wrapped;
                        }
                    }
                }
            }
            return null;
        }
    }
}