package mekanism.common.lib.transmitter.acceptor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class AbstractAcceptorCache<ACCEPTOR, INFO extends AcceptorInfo<ACCEPTOR>> {

    private final Map<Direction, RefreshListener> cachedListeners = new EnumMap<>(Direction.class);
    private final Map<Direction, INFO> cachedAcceptors = new EnumMap<>(Direction.class);
    private final TileEntityTransmitter transmitterTile;
    public byte currentAcceptorConnections = 0x00;

    protected AbstractAcceptorCache(TileEntityTransmitter transmitterTile) {
        this.transmitterTile = transmitterTile;
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

    protected abstract INFO initializeCache(ServerLevel level, BlockPos pos, Direction opposite, RefreshListener refreshListener);

    /**
     * @apiNote Only call this from the server side
     */
    public boolean isAcceptor(Direction side) {
        AcceptorInfo<ACCEPTOR> cache = cachedAcceptors.get(side);
        //Note: Cache should never be null unless we haven't been initialized yet, but it is simple enough to handle that here with a null check
        return cache != null && cache.acceptor() != null;
    }

    /**
     * @implNote Grabs the acceptors from cache, ensuring that the connection map contains the side
     */
    @Nullable
    public ACCEPTOR getCachedAcceptor(Direction side) {
        return Transmitter.connectionMapContainsSide(currentAcceptorConnections, side) ? getConnectedAcceptor(side) : null;
    }

    /**
     * Gets all our cached acceptors for the given sides.
     *
     * @implNote Grabs the acceptors from cache
     */
    public List<ACCEPTOR> getConnectedAcceptors(Set<Direction> sides) {
        //TODO: Figure out if this is including transmitters in it due to not checking acceptor connections
        List<ACCEPTOR> acceptors = new ArrayList<>(sides.size());
        for (Direction side : sides) {
            //TODO - 1.20.2: Validate that the fact this doesn't validate the thing is actually connected is fine??
            // as this potentially should actually use getCachedAcceptor
            ACCEPTOR connectedAcceptor = getConnectedAcceptor(side);
            if (connectedAcceptor != null) {
                acceptors.add(connectedAcceptor);
            }
        }
        return acceptors;
    }

    /**
     * @implNote Grabs the acceptors from cache
     */
    @Nullable
    public ACCEPTOR getConnectedAcceptor(Direction side) {
        INFO acceptorInfo = cachedAcceptors.get(side);
        return acceptorInfo == null ? null : acceptorInfo.acceptor();
    }

    /**
     * Gets the listener that will refresh connections on a given side.
     */
    protected RefreshListener getRefreshListener(@NotNull Direction side) {
        return cachedListeners.computeIfAbsent(side, s -> new RefreshListener(transmitterTile, s));
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

        /**
         * Used to check if this listener is still valid
         *
         * @return {@code true} if still valid.
         */
        @Override
        public boolean getAsBoolean() {
            TileEntityTransmitter transmitterTile = tile.get();
            //Check to make sure the transmitter is still valid
            //TODO: Is loaded check should potentially use onUnload so it runs before the onRemoved?
            //TODO: I think this isLoaded check may actually break when chunks become loaded but inaccessible...
            // so we may need to move the isLoaded check to run or maybe even just remove it all together??
            // Can we maybe just have it always be true and let the GC handle nuking the listeners
            return transmitterTile != null && !transmitterTile.isRemoved() && transmitterTile.hasLevel() && transmitterTile.isLoaded();
        }

        /**
         * Called if this listener is still valid to run the cache invalidation logic.
         */
        @Override
        public void run() {
            TileEntityTransmitter transmitterTile = tile.get();
            //If we are still a valid listener, validate the tile didn't somehow become null between checking if it is valid and running the invalidation listener,
            // then mark the acceptor as dirty
            if (transmitterTile != null) {
                //Note: Call markDirtyAcceptor so we don't evaluate the changed block until the server tick
                // so that blocks hopefully had a chance to figure out their caps
                // and if the chunk is now unloaded we will get null as the capability, and then when it loads again
                // we will be updated and get the proper capability again
                transmitterTile.getTransmitter().markDirtyAcceptor(side);
                //TODO: This used to do refreshConnections (at least under certain conditions) which updates more things than just markDirty acceptor
                // so maybe some of the reasons that the idle path seems to stay broken is because we don't update enough stuff?
            }
        }
    }
}