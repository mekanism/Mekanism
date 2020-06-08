package mekanism.common.lib.transmitter;

import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;

public class AcceptorCache<ACCEPTOR> {

    //TODO: Move these to being private
    private final Map<Direction, NonNullConsumer<LazyOptional<ACCEPTOR>>> cachedListeners = new EnumMap<>(Direction.class);
    private final TileEntityTransmitter<ACCEPTOR, ?, ?> transmitter;
    public byte currentAcceptorConnections = 0x00;
    //TODO: Do this differently so that we cache the LazyOptional<ACCEPTOR>
    @Deprecated
    private final TileEntity[] cachedAcceptors = new TileEntity[6];

    public AcceptorCache(TileEntityTransmitter<ACCEPTOR, ?, ?> transmitter) {
        this.transmitter = transmitter;
    }

    public void clear() {
        cachedListeners.clear();
    }

    //TODO: Re-evaluate/rewrite to not use tiles like this
    public void updateCachedAcceptor(Direction side, @Nullable TileEntity acceptor) {
        int index = side.ordinal();
        if (cachedAcceptors[index] != acceptor) {
            cachedAcceptors[index] = acceptor;
            transmitter.markDirtyAcceptor(side);
        }
    }

    //TODO: Change to actually returning an ACCEPTOR instead of a TE. Or at least a LazyOptional for the acceptor
    public TileEntity getCachedAcceptor(Direction side) {
        return TileEntityTransmitter.connectionMapContainsSide(currentAcceptorConnections, side) ? cachedAcceptors[side.ordinal()] : null;
    }

    @Deprecated//TODO - V10: Re-evaluate this
    public boolean hasStrictEnergyHandlerAndListen(TileEntity tile, Direction side) {
        return EnergyCompatUtils.hasStrictEnergyHandlerAndListen(tile, side.getOpposite(), getRefreshListener(side));
    }

    //TODO - V10: Rewrite this to not be as "directly" needed/be less of a "patch".
    // Ideally we will end up instead having it so that all targets are fully cached rather than
    // just registering a listener and "forgetting" about it
    public boolean isAcceptorAndListen(TileEntity tile, Direction side, Capability<ACCEPTOR> capability) {
        LazyOptional<ACCEPTOR> acceptor = CapabilityUtils.getCapability(tile, capability, side.getOpposite());
        if (acceptor.isPresent()) {
            //If the capability is present
            if (!transmitter.isRemote()) {
                //And we are on the server, add a listener so that once it gets invalidated we recheck that side
                // assuming that the world and position is still loaded and our tile has not been removed
                acceptor.addListener(getRefreshListener(side));
            }
            return true;
        }
        return false;
    }

    /**
     * Gets the listener that will refresh connections on a given side.
     */
    private NonNullConsumer<LazyOptional<ACCEPTOR>> getRefreshListener(@Nonnull Direction side) {
        return cachedListeners.computeIfAbsent(side, this::getUncachedRefreshListener);
    }

    private NonNullConsumer<LazyOptional<ACCEPTOR>> getUncachedRefreshListener(@Nonnull Direction side) {
        return ignored -> {
            //Check to make sure the transmitter is still valid and that the position we are going to check is actually still loaded
            if (!transmitter.isRemoved() && transmitter.hasWorld() && transmitter.getWorld().isBlockPresent(transmitter.getPos().offset(side))) {
                //If it is, then refresh the connection
                transmitter.refreshConnections(side);
            }
        };
    }
}