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

        //TODO: Document this is basically a "canRun" method
        @Override
        public boolean getAsBoolean() {
            //TODO: Do we actually want this here or just in our invalidation?? And here make sure the tile is not null
            // Because if the GC actually cleans up the cache's when no longer in use... then we don't have to bother adding
            // extra ones back if it has been removed. And we can get rid of the part in run that has to remove the cached acceptor
            // (which causes us to have to recreate it anyway which is actually probably quite bad)...
            TileEntityTransmitter transmitterTile = tile.get();
            //Check to make sure the transmitter is still valid and that the position we are going to check is actually still loaded
            return transmitterTile != null && !transmitterTile.isRemoved() && transmitterTile.hasLevel() && transmitterTile.isLoaded() &&
                   WorldUtils.isBlockLoaded(transmitterTile.getLevel(), transmitterTile.getBlockPos().relative(side));
        }

        @Override
        public void run() {
            //If it is, then refresh the connection
            TileEntityTransmitter transmitterTile = tile.get();
            if (transmitterTile != null) {//Validate it didn't somehow become null between checking if it is valid and running the invalidation listener
                //TODO: Evaluate this, we need to clear the cached acceptors as they are no longer necessarily valid
                // I don't think this is right as it doesn't properly visually update when neighboring blocks are removed??
                //TODO: FIX THIS, as removing when not invalid means that while yes we prevent the cache from erroring calling getCapability
                // on it after it is invalid... it means even when the capability cache would still be valid that we remove it (and the listener stays around)
                //TODO: Test if not having this actually causes problems when using markDirtyAcceptor instead of refreshConnections as if the error I had
                // was before moving to markDirtyAcceptors which delays the check it could be related to BlockCapabilityCache errors when calling getCapability
                // from the invalidation listener
                transmitterTile.getTransmitter().getAcceptorCache().cachedAcceptors.remove(side);
                //TODO: Evaluate if this is correct or should it be transmitter.markDirtyAcceptor(side)
                // previously the refresh listener did refreshConnections, but isAcceptorAndListen used markDirtyAcceptor
                //transmitterTile.getTransmitter().refreshConnections(side);
                //Note: This is needed at the very least when it goes from nothing to having a side
                // Though maybe we need to call both
                transmitterTile.getTransmitter().markDirtyAcceptor(side);
            }
        }
    }
}