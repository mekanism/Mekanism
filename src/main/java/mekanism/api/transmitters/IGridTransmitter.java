package mekanism.api.transmitters;

import java.util.Collection;
import mekanism.api.Coord4D;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public interface IGridTransmitter<A, N extends DynamicNetwork<A, N>> extends ITransmitter {

    boolean hasTransmitterNetwork();

    /**
     * Gets the network currently in use by this transmitter segment.
     *
     * @return network this transmitter is using
     */
    N getTransmitterNetwork();

    /**
     * Sets this transmitter segment's network to a new value.
     *
     * @param network - network to set to
     */
    void setTransmitterNetwork(N network);

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

    A getAcceptor(EnumFacing side);

    boolean isValid();

    boolean isOrphan();

    void setOrphan(boolean orphaned);

    N createEmptyNetwork();

    N mergeNetworks(Collection<N> toMerge);

    N getExternalNetwork(Coord4D from);

    void takeShare();

    void updateShare();

    Object getBuffer();

    boolean isCompatibleWith(IGridTransmitter<A,N> other);
}
