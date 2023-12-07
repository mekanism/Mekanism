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
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class AbstractAcceptorCache<ACCEPTOR, INFO extends AcceptorInfo<ACCEPTOR>> {

    private final Map<Direction, RefreshListener> cachedListeners = new EnumMap<>(Direction.class);
    protected final Map<Direction, INFO> cachedAcceptors = new EnumMap<>(Direction.class);
    protected final Transmitter<ACCEPTOR, ?, ?> transmitter;
    protected final TileEntityTransmitter transmitterTile;
    public byte currentAcceptorConnections = 0x00;

    protected AbstractAcceptorCache(Transmitter<ACCEPTOR, ?, ?> transmitter, TileEntityTransmitter transmitterTile) {
        this.transmitter = transmitter;
        this.transmitterTile = transmitterTile;
    }

    //TODO - 1.20.2: How much do we actually need to clear this vs can we just let GC handle it
    // especially given then if it is removed and then gets added back in some ways we may as well be able to re-use things
    public void clear() {
        cachedListeners.clear();
        cachedAcceptors.clear();
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