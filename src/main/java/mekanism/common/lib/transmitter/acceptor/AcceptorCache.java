package mekanism.common.lib.transmitter.acceptor;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache.CacheBasedInfo;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class AcceptorCache<ACCEPTOR> extends AbstractAcceptorCache<ACCEPTOR, CacheBasedInfo<ACCEPTOR>> {

    private final BlockCapability<ACCEPTOR, @Nullable Direction> capability;

    public AcceptorCache(TileEntityTransmitter transmitterTile, BlockCapability<ACCEPTOR, @Nullable Direction> capability) {
        super(transmitterTile);
        this.capability = capability;
    }

    @Override
    protected CacheBasedInfo<ACCEPTOR> initializeCache(ServerLevel level, BlockPos pos, Direction opposite, RefreshListener refreshListener) {
        return new CacheBasedInfo<>(BlockCapabilityCache.create(capability, level, pos, opposite, refreshListener, refreshListener));
    }

    public record CacheBasedInfo<ACCEPTOR>(BlockCapabilityCache<ACCEPTOR, @Nullable Direction> cache) implements AcceptorInfo<ACCEPTOR> {

        @Nullable
        @Override
        public ACCEPTOR acceptor() {
            return cache.getCapability();
        }
    }
}