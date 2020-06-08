package mekanism.common.lib.transmitter;

import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AcceptorCache<ACCEPTOR> {

    private final Map<Direction, NonNullConsumer<LazyOptional<ACCEPTOR>>> cachedListeners = new EnumMap<>(Direction.class);
    private final TileEntity[] cachedTiles = new TileEntity[6];
    private final TileEntityTransmitter<ACCEPTOR, ?, ?> transmitter;
    //TODO: Move this to being private?
    public byte currentAcceptorConnections = 0x00;

    public AcceptorCache(TileEntityTransmitter<ACCEPTOR, ?, ?> transmitter) {
        this.transmitter = transmitter;
    }

    public void clear() {
        cachedListeners.clear();
    }

    public void invalidateCachedAcceptor(Direction side) {
        int index = side.ordinal();
        if (cachedTiles[index] != null) {
            cachedTiles[index] = null;
            transmitter.markDirtyAcceptor(side);
            //Note: as our listeners don't care about the actual implementation of the tile on a given side
            // we don't bother invalidating and getting rid of the old listener when we remove the cached
            // acceptor as we don't want to accidentally flood the acceptor with listeners if something
            // breaks on our end.
        }
    }

    private void updateCachedAcceptorAndListen(Direction side, TileEntity acceptorTile, LazyOptional<ACCEPTOR> acceptor) {
        int index = side.ordinal();
        if (cachedTiles[index] != acceptorTile) {
            cachedTiles[index] = acceptorTile;
            transmitter.markDirtyAcceptor(side);
            //If the capability is present, add a listener so that once it gets invalidated we recheck that side
            // assuming that the world and position is still loaded and our tile has not been removed
            acceptor.addListener(getRefreshListener(side));
        }
    }

    public LazyOptional<ACCEPTOR> getCachedAcceptor(Capability<ACCEPTOR> capability, Direction side) {
        return CapabilityUtils.getCapability(getCachedAcceptorTile(side), capability, side.getOpposite());
    }

    @Nullable
    public TileEntity getCachedAcceptorTile(Direction side) {
        //TODO: Do we need to also invalidate if the connection map doesn't contain it
        return TileEntityTransmitter.connectionMapContainsSide(currentAcceptorConnections, side) ? cachedTiles[side.ordinal()] : null;
    }

    @Deprecated//TODO - V10: Re-evaluate this
    public boolean hasStrictEnergyHandlerAndListen(TileEntity tile, Direction side) {
        boolean hasAcceptor = EnergyCompatUtils.hasStrictEnergyHandlerAndListen(tile, side.getOpposite(), getRefreshListener(side));
        int index = side.ordinal();
        if (cachedTiles[index] != tile) {
            cachedTiles[index] = tile;
            transmitter.markDirtyAcceptor(side);
        }
        return hasAcceptor;
    }

    /**
     * @apiNote Only call this from the server side
     */
    public boolean isAcceptorAndListen(TileEntity tile, Direction side, Capability<ACCEPTOR> capability) {
        LazyOptional<ACCEPTOR> acceptor = CapabilityUtils.getCapability(tile, capability, side.getOpposite());
        if (acceptor.isPresent()) {
            //Update the cached acceptor and if it changed, add a listener to it to listen for invalidation
            updateCachedAcceptorAndListen(side, tile, acceptor);
            return true;
        }
        return false;
    }

    /**
     * Gets the listener that will refresh connections on a given side.
     */
    private NonNullConsumer<LazyOptional<ACCEPTOR>> getRefreshListener(Direction side) {
        return cachedListeners.computeIfAbsent(side, this::getUncachedRefreshListener);
    }

    private NonNullConsumer<LazyOptional<ACCEPTOR>> getUncachedRefreshListener(Direction side) {
        return ignored -> {
            //Check to make sure the transmitter is still valid and that the position we are going to check is actually still loaded
            if (!transmitter.isRemoved() && transmitter.hasWorld() && transmitter.getWorld().isBlockPresent(transmitter.getPos().offset(side))) {
                //If it is, then refresh the connection
                transmitter.refreshConnections(side);
            }
        };
    }
}