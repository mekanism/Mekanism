package mekanism.common.lib.transmitter.acceptor;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.network.transmitter.Transmitter;
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

    public AcceptorCache(Transmitter<ACCEPTOR, ?, ?> transmitter, TileEntityTransmitter transmitterTile) {
        super(transmitter, transmitterTile);
    }

    /**
     * @apiNote Only call this from the server side
     */
    public boolean isAcceptorAndListen(ServerLevel level, BlockPos pos, Direction side, BlockCapability<ACCEPTOR, @Nullable Direction> capability) {
        //TODO: Do we need to be calling this regardless of if it is a valid acceptor? Or maybe at least add a refresh listener when it isn't
        //TODO: Validate this and recheck the comment
        //Update the cached acceptor and if it changed, add a listener to it to listen for invalidation
        AcceptorInfo<ACCEPTOR> cache = cachedAcceptors.computeIfAbsent(side, s -> {
            RefreshListener refreshListener = getRefreshListener(s);
            return new CacheBasedInfo<>(BlockCapabilityCache.create(capability, level, pos, s.getOpposite(), refreshListener, refreshListener));
        });
        //TODO: Make sure we are removing it from cachedAcceptors when it becomes invalidated or the next call to this will error
        // Is this even correct??? The cache only actually gets removed if isValid is false
        return cache.acceptor() != null;
    }

    public record CacheBasedInfo<ACCEPTOR>(BlockCapabilityCache<ACCEPTOR, @Nullable Direction> cache) implements AcceptorInfo<ACCEPTOR> {

        @Nullable
        @Override
        public ACCEPTOR acceptor() {
            return cache.getCapability();
        }
    }
}