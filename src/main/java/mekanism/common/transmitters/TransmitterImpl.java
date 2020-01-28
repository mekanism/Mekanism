package mekanism.common.transmitters;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;

public class TransmitterImpl<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, BUFFER>, BUFFER> extends Transmitter<ACCEPTOR, NETWORK, BUFFER> {

    public TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> containingTile;

    public TransmitterImpl(TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> multiPart) {
        setTileEntity(multiPart);
    }

    @Override
    public int getCapacity() {
        return getTileEntity().getCapacity();
    }

    @Override
    public World world() {
        return getTileEntity().getWorld();
    }

    @Override
    public Coord4D coord() {
        return new Coord4D(getTileEntity().getPos(), world());
    }

    @Override
    public Coord4D getAdjacentConnectableTransmitterCoord(Direction side) {
        Coord4D sideCoord = coord().offset(side);
        TileEntity potentialTransmitterTile = MekanismUtils.getTileEntity(world(), sideCoord.getPos());
        if (!containingTile.canConnectMutual(side)) {
            return null;
        }
        Optional<IGridTransmitter<?, ?, ?>> gridTransmitter = MekanismUtils.toOptional(CapabilityUtils.getCapability(potentialTransmitterTile,
              Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite()));
        if (gridTransmitter.isPresent() && TransmissionType.checkTransmissionType(gridTransmitter.get(), getTransmissionType()) &&
            containingTile.isValidTransmitter(potentialTransmitterTile)) {
            return sideCoord;
        }
        return null;
    }

    @Override
    public boolean isCompatibleWith(IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> other) {
        if (other instanceof TransmitterImpl) {
            return containingTile.isValidTransmitter(((TransmitterImpl<?, ?, ?>) other).containingTile);
        }
        return true;//allow non-Transmitter impls to connect?
    }

    @Override
    public void connectionFailed() {
        containingTile.delayedRefresh = true;
    }

    @Override
    public ACCEPTOR getAcceptor(Direction side) {
        return getTileEntity().getCachedAcceptor(side);
    }

    @Override
    public boolean isValid() {
        TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> cont = getTileEntity();
        if (cont == null) {
            return false;
        }
        return !cont.isRemoved() && MekanismUtils.getTileEntity(world(), cont.getPos()) == cont && cont.getTransmitter() == this;
    }

    @Override
    public NETWORK createEmptyNetwork() {
        return getTileEntity().createNewNetwork();
    }

    @Override
    public NETWORK getExternalNetwork(Coord4D from) {
        Optional<IGridTransmitter<?, ?, ?>> gridTransmitter = MekanismUtils.toOptional(CapabilityUtils.getCapability(MekanismUtils.getTileEntity(world(), from.getPos()),
              Capabilities.GRID_TRANSMITTER_CAPABILITY, null));
        if (gridTransmitter.isPresent()) {
            IGridTransmitter<?, ?, ?> transmitter = gridTransmitter.get();
            if (TransmissionType.checkTransmissionType(transmitter, getTransmissionType())) {
                return ((IGridTransmitter<ACCEPTOR, NETWORK, BUFFER>) transmitter).getTransmitterNetwork();
            }
        }
        return null;
    }

    @Override
    public void takeShare() {
        containingTile.takeShare();
    }

    @Override
    public void updateShare() {
        containingTile.updateShare();
    }

    @Nullable
    @Override
    public BUFFER getBuffer() {
        return getTileEntity().getBuffer();
    }

    @Nullable
    @Override
    public BUFFER getBufferWithFallback() {
        return getTileEntity().getBufferWithFallback();
    }

    @Override
    public NETWORK mergeNetworks(Collection<NETWORK> toMerge) {
        return getTileEntity().createNetworkByMerging(toMerge);
    }

    @Override
    public TransmissionType getTransmissionType() {
        return getTileEntity().getTransmissionType();
    }

    @Override
    public void setRequestsUpdate() {
        containingTile.sendDesc = true;
    }

    public TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> getTileEntity() {
        return containingTile;
    }

    public void setTileEntity(TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> containingPart) {
        this.containingTile = containingPart;
    }
}