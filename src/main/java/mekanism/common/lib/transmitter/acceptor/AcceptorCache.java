package mekanism.common.lib.transmitter.acceptor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jspecify.annotations.Nullable;

public class AcceptorCache<ACCEPTOR> {

    private final Map<Direction, RefreshListener> cachedListeners = new EnumMap<>(Direction.class);
    private final Map<Direction, CacheBasedInfo<ACCEPTOR>> cachedAcceptors = new EnumMap<>(Direction.class);
    private final BlockCapability<ACCEPTOR, @Nullable Direction> capability;
    private final TileEntityTransmitter transmitterTile;
    public byte currentAcceptorConnections = 0x00;

    public AcceptorCache(TileEntityTransmitter transmitterTile, BlockCapability<ACCEPTOR, @Nullable Direction> capability) {
        this.transmitterTile = transmitterTile;
        this.capability = capability;
    }

    public void initializeCache(ServerLevel level) {
        //Note: This doesn't exactly support if the level changes, but if there ever are actually bugs related to that
        // we can handle figuring that out then as it will require invalidating the old caches
        if (cachedAcceptors.isEmpty()) {
            for (Direction side : EnumUtils.DIRECTIONS) {
                BlockPos pos = transmitterTile.getBlockPos().relative(side);
                cachedAcceptors.put(side, initializeCache(level, pos, side.getOpposite(), getRefreshListener(side)));
            }
        }
    }

    private CacheBasedInfo<ACCEPTOR> initializeCache(ServerLevel level, BlockPos pos, Direction opposite, RefreshListener refreshListener) {
        BlockCapabilityCache<ACCEPTOR, @Nullable Direction> cache = BlockCapabilityCache.create(capability, level, pos, opposite, refreshListener, refreshListener);
        return new CacheBasedInfo<>(cache);
    }

    /// @implNote Grabs the acceptors from cache, ensuring that the connection map contains the side
    @Nullable
    public ACCEPTOR getCachedAcceptor(Direction side) {
        return Transmitter.connectionMapContainsSide(currentAcceptorConnections, side) ? getConnectedAcceptor(side) : null;
    }

    /// Gets all our cached acceptors for the given sides.
    ///
    /// @param sides The sides of to look up, assumes that all the given sides are currently connected to acceptors and not other transmitters and is not set to none.
    ///
    /// @implNote Grabs the acceptors from cache
    public List<ACCEPTOR> getConnectedAcceptors(Set<Direction> sides) {
        List<ACCEPTOR> acceptors = new ArrayList<>(sides.size());
        for (Direction side : sides) {
            ACCEPTOR connectedAcceptor = getConnectedAcceptor(side);
            if (connectedAcceptor != null) {
                acceptors.add(connectedAcceptor);
            }
        }
        return acceptors;
    }

    /// @apiNote Only call this from the server side
    /// @implNote Grabs the acceptors from cache
    @Nullable
    public ACCEPTOR getConnectedAcceptor(Direction side) {
        CacheBasedInfo<ACCEPTOR> acceptorInfo = cachedAcceptors.get(side);
        return acceptorInfo == null ? null : acceptorInfo.acceptor();
    }

    /// Gets the listener that will refresh connections on a given side.
    private RefreshListener getRefreshListener(Direction side) {
        RefreshListener listener = cachedListeners.get(side);
        //noinspection Java8MapApi - Capturing lambda
        if (listener == null) {
            listener = new RefreshListener(transmitterTile, side);
            cachedListeners.put(side, listener);
        }
        return listener;
    }

    public record CacheBasedInfo<ACCEPTOR>(BlockCapabilityCache<ACCEPTOR, @Nullable Direction> cache) {

        @Nullable
        public ACCEPTOR acceptor() {
            return cache.getCapability();
        }
    }

    public static class RefreshListener implements Runnable, BooleanSupplier {

        //Note: We only keep a weak reference to the tile from inside the listener so that if it gets unloaded it can be released from memory
        // instead of being referenced by the listener still in the tile in a neighboring chunk
        private final WeakReference<TileEntityTransmitter> tile;
        private final Direction side;

        private RefreshListener(TileEntityTransmitter tile, Direction side) {
            this.tile = new WeakReference<>(tile);
            this.side = side;
        }

        /// Used to check if this listener is still valid
        ///
        /// @return `true` if still valid.
        @Override
        public boolean getAsBoolean() {
            //Note: We could get away with just returning true here and letting GC fully handle removing this listener,
            // but we allow it to clean it up earlier if our transmitter tile is GC'd
            return tile.get() != null;
        }

        /// Called if this listener is still valid to run the cache invalidation logic.
        @Override
        public void run() {
            TileEntityTransmitter transmitterTile = tile.get();
            //If we are still a valid listener, validate the tile didn't somehow become null between checking if it is valid and running the invalidation listener,
            // then mark the acceptor as dirty
            if (transmitterTile != null) {
                Transmitter<?, ?, ?> transmitter = transmitterTile.getTransmitter();
                //Check that the transmitter is actually valid before marking parts of it as dirty
                // this happens here instead of as part of the overall validation check so that if
                // the transmitter later becomes valid again the BlockCapabilityCache's still usable.
                // The most common case for this would be when the chunk the transmitter is in becomes inaccessible BUT is still loaded,
                // and then something next to the transmitter fires a capability invalidation.
                if (transmitter.isValid()) {
                    //Note: Call markDirtyAcceptor so we don't evaluate the changed block until the server tick
                    // so that blocks hopefully had a chance to figure out their caps
                    // and if the chunk is now unloaded we will get null as the capability, and then when it loads again
                    // we will be updated and get the proper capability again
                    transmitter.markDirtyAcceptor(side);
                }
            }
        }
    }
}