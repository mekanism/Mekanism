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
import mekanism.common.util.EmitUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
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

    public void clear() {
        cachedListeners.clear();
        cachedAcceptors.clear();
    }

    public void invalidateCachedAcceptor(Direction side) {
        if (cachedAcceptors.containsKey(side)) {
            //Fully remove the cached acceptor, both the tile, and the value
            cachedAcceptors.remove(side);
            transmitter.markDirtyAcceptor(side);
            //Note: as our listeners don't care about the actual implementation of the tile on a given side
            // we don't bother invalidating and getting rid of the old listener when we remove the cached
            // acceptor as we don't want to accidentally flood the acceptor with listeners if something
            // breaks on our end.
        }
    }

    /**
     * @implNote Grabs the acceptors from cache, ensuring that the connection map contains the side
     */
    @Nullable
    public ACCEPTOR getCachedAcceptor(Direction side) {
        return Transmitter.connectionMapContainsSide(currentAcceptorConnections, side) ? getConnectedAcceptor(side) : null;
    }

    /**
     * Similar to {@link EmitUtils#forEachSide(net.minecraft.world.level.Level, net.minecraft.core.BlockPos, Iterable, mekanism.common.util.EmitUtils.SideAction)} except
     * queries our cached acceptors.
     *
     * @implNote Grabs the acceptors from cache
     */
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

    /**
     * @implNote Grabs the acceptors from cache
     */
    @Nullable
    public ACCEPTOR getConnectedAcceptor(Direction side) {
        INFO acceptorInfo = cachedAcceptors.get(side);
        return acceptorInfo == null ? null : acceptorInfo.acceptor();
    }

    @Nullable
    public BlockEntity getConnectedAcceptorTile(Direction side) {
        return WorldUtils.getTileEntity(transmitterTile.getLevel(), transmitterTile.getBlockPos().relative(side));
        //TODO: Figure out and potentially reimplement. If we don't then inline the above to the one caller
        /*AcceptorInfo<ACCEPTOR> acceptorInfo = cachedAcceptors.get(side);
        if (acceptorInfo != null) {
            BlockEntity tile = acceptorInfo.getTile();
            if (!tile.isRemoved()) {
                return tile;
            }
        }
        return null;*/
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
            }
        }
    }
}