package mekanism.api.transmitters;

import java.util.Collection;
import mekanism.api.Coord4D;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public interface IGridTransmitter<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, BUFFER>, BUFFER> extends ITransmitter {

    boolean hasTransmitterNetwork();

    /**
     * Gets the network currently in use by this transmitter segment.
     *
     * @return network this transmitter is using
     */
    NETWORK getTransmitterNetwork();

    /**
     * Sets this transmitter segment's network to a new value.
     *
     * @param network - network to set to
     */
    void setTransmitterNetwork(NETWORK network);

    void setRequestsUpdate();

    int getTransmitterNetworkSize();

    int getTransmitterNetworkAcceptorSize();

    String getTransmitterNetworkNeeded();

    String getTransmitterNetworkFlow();

    String getTransmitterNetworkBuffer();

    double getTransmitterNetworkCapacity();

    int getCapacity();

    World world();

    Coord4D coord();

    Coord4D getAdjacentConnectableTransmitterCoord(EnumFacing side);

    ACCEPTOR getAcceptor(EnumFacing side);

    boolean isValid();

    boolean isOrphan();

    void setOrphan(boolean orphaned);

    NETWORK createEmptyNetwork();

    NETWORK mergeNetworks(Collection<NETWORK> toMerge);

    NETWORK getExternalNetwork(Coord4D from);

    void takeShare();

    void updateShare();

    BUFFER getBuffer();

    boolean isCompatibleWith(IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> other);
}
