package mekanism.common.transmitters;

import java.util.Collection;
import mekanism.api.Coord4D;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class TransmitterImpl<A, N extends DynamicNetwork<A, N>> extends Transmitter<A, N> {

    public TileEntityTransmitter<A, N> containingTile;

    public TransmitterImpl(TileEntityTransmitter<A, N> multiPart) {
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
        return new Coord4D(getTileEntity().getPos(), getTileEntity().getWorld());
    }

    @Override
    public Coord4D getAdjacentConnectableTransmitterCoord(EnumFacing side) {
        Coord4D sideCoord = coord().offset(side);

        TileEntity potentialTransmitterTile = sideCoord.getTileEntity(world());

        if (!containingTile.canConnectMutual(side)) {
            return null;
        }

        if (CapabilityUtils
              .hasCapability(potentialTransmitterTile, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite())) {
            IGridTransmitter transmitter = CapabilityUtils
                  .getCapability(potentialTransmitterTile, Capabilities.GRID_TRANSMITTER_CAPABILITY,
                        side.getOpposite());

            if (TransmissionType.checkTransmissionType(transmitter, getTransmissionType()) && containingTile
                  .isValidTransmitter(potentialTransmitterTile)) {
                return sideCoord;
            }
        }

        return null;
    }

    @Override
    public boolean isCompatibleWith(IGridTransmitter<A, N> other) {
        if (other instanceof TransmitterImpl) {
            return containingTile.isValidTransmitter(((TransmitterImpl) other).containingTile);
        }
        return true;//allow non-Transmitter impls to connect?
    }

    @Override
    public A getAcceptor(EnumFacing side) {
        return getTileEntity().getCachedAcceptor(side);
    }

    @Override
    public boolean isValid() {
        TileEntityTransmitter cont = getTileEntity();

        if (cont == null) {
            return false;
        }

        return !cont.isInvalid() && coord().exists(world()) && coord().getTileEntity(world()) == cont
              && cont.getTransmitter() == this;
    }

    @Override
    public N createEmptyNetwork() {
        return getTileEntity().createNewNetwork();
    }

    @Override
    public N getExternalNetwork(Coord4D from) {
        TileEntity tile = from.getTileEntity(world());

        if (CapabilityUtils.hasCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, null)) {
            IGridTransmitter transmitter = CapabilityUtils
                  .getCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, null);

            if (TransmissionType.checkTransmissionType(transmitter, getTransmissionType())) {
                return ((IGridTransmitter<A, N>) transmitter).getTransmitterNetwork();
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

    @Override
    public Object getBuffer() {
        return getTileEntity().getBuffer();
    }

    @Override
    public N mergeNetworks(Collection<N> toMerge) {
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

    public TileEntityTransmitter<A, N> getTileEntity() {
        return containingTile;
    }

    public void setTileEntity(TileEntityTransmitter<A, N> containingPart) {
        this.containingTile = containingPart;
    }
}
