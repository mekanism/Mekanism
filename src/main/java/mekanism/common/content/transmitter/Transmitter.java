package mekanism.common.content.transmitter;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.math.FloatingLong;
import mekanism.common.MekanismLang;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.IGridTransmitter;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

@Deprecated//TODO - V10: Merge directly into TileEntityTransmitter
public class Transmitter<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, BUFFER>, BUFFER> implements IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> {

    public NETWORK theNetwork = null;

    public boolean orphaned = true;

    public TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> containingTile;

    public Transmitter(TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter) {
        setTileEntity(transmitter);
    }

    @Nonnull
    @Override
    public FloatingLong getCapacityAsFloatingLong() {
        return getTileEntity().getCapacityAsFloatingLong();
    }

    @Override
    public long getCapacity() {
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
    public NETWORK getTransmitterNetwork() {
        return theNetwork;
    }

    @Override
    public void setTransmitterNetwork(NETWORK network) {
        if (theNetwork == network) {
            return;
        }
        if (world().isRemote && theNetwork != null) {
            theNetwork.removeTransmitter(this);
        }
        theNetwork = network;
        orphaned = theNetwork == null;
        if (world().isRemote) {
            if (theNetwork != null) {
                theNetwork.addTransmitter(this);
            }
        } else {
            setRequestsUpdate();
        }
    }

    @Override
    public boolean hasTransmitterNetwork() {
        return !isOrphan() && getTransmitterNetwork() != null;
    }

    @Override
    public int getTransmitterNetworkSize() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().transmittersSize() : 0;
    }

    @Override
    public int getTransmitterNetworkAcceptorSize() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().getAcceptorSize() : 0;
    }

    @Override
    public ITextComponent getTransmitterNetworkNeeded() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getNeededInfo();
        }
        return MekanismLang.NO_NETWORK.translate();
    }

    @Override
    public ITextComponent getTransmitterNetworkFlow() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getFlowInfo();
        }
        return MekanismLang.NO_NETWORK.translate();
    }

    @Override
    public ITextComponent getTransmitterNetworkBuffer() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getStoredInfo();
        }
        return MekanismLang.NO_NETWORK.translate();
    }

    @Override
    public long getTransmitterNetworkCapacity() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().getCapacity() : getCapacity();
    }

    @Override
    public boolean isOrphan() {
        return orphaned;
    }

    @Override
    public void setOrphan(boolean nowOrphaned) {
        orphaned = nowOrphaned;
    }

    @Override
    public Coord4D getAdjacentConnectableTransmitterCoord(Direction side) {
        Coord4D sideCoord = coord().offset(side);
        TileEntity potentialTransmitterTile = MekanismUtils.getTileEntity(world(), sideCoord.getPos());
        if (!containingTile.canConnectMutual(side, potentialTransmitterTile)) {
            return null;
        }
        if (potentialTransmitterTile instanceof IGridTransmitter && getTransmissionType().checkTransmissionType((IGridTransmitter<?, ?, ?>) potentialTransmitterTile) &&
            containingTile.isValidTransmitter(potentialTransmitterTile)) {
            return sideCoord;
        }
        return null;
    }

    @Override
    public boolean isCompatibleWith(IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> other) {
        if (other instanceof Transmitter) {
            return containingTile.isValidTransmitter(((Transmitter<?, ?, ?>) other).containingTile);
        }
        return true;//allow non-Transmitter impls to connect?
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
    public NETWORK createEmptyNetworkWithID(UUID networkID) {
        return getTileEntity().createNewNetworkWithID(networkID);
    }

    @Override
    public NETWORK getExternalNetwork(Coord4D from) {
        TileEntity tile = MekanismUtils.getTileEntity(world(), from.getPos());
        if (tile instanceof IGridTransmitter) {
            IGridTransmitter<?, ?, ?> transmitter = (IGridTransmitter<?, ?, ?>) tile;
            if (getTransmissionType().checkTransmissionType(transmitter)) {
                return ((IGridTransmitter<ACCEPTOR, NETWORK, BUFFER>) transmitter).getTransmitterNetwork();
            }
        }
        return null;
    }

    @Override
    public void takeShare() {
        containingTile.takeShare();
    }

    @Nullable
    @Override
    public BUFFER releaseShare() {
        return getTileEntity().releaseShare();
    }

    @Override
    public BUFFER getShare() {
        return getTileEntity().getShare();
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
        getTileEntity().requestsUpdate();
    }

    public TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> getTileEntity() {
        return containingTile;
    }

    public void setTileEntity(TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> containingPart) {
        this.containingTile = containingPart;
    }
}