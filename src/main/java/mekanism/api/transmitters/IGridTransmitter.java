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

    default boolean isCompatibleWith(IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> other){
        return true;
    }

    /**
     * Gets called on an orphan if at least one attempted network fails to connect
     * due to having connected to another network that is incompatible with the
     * next attempted ones.
     *
     * This is primarily used for if extra handling needs to be done, such as
     * refreshing the connections visually on a minor delay so that it has
     * time to have the buffer no longer be null and properly compare the
     * connection.
     */
    default void connectionFailed() {
    }
}
