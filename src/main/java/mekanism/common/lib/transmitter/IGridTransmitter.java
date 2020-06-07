package mekanism.common.lib.transmitter;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.math.FloatingLong;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;

public interface IGridTransmitter<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, BUFFER>, BUFFER> {

    /**
     * Get the transmitter's transmission type
     *
     * @return TransmissionType this transmitter uses
     */
    TransmissionType getTransmissionType();

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

    /**
     * Only call on the server
     */
    void requestsUpdate();

    int getTransmitterNetworkSize();

    int getTransmitterNetworkAcceptorSize();

    ITextComponent getTransmitterNetworkNeeded();

    ITextComponent getTransmitterNetworkFlow();

    ITextComponent getTransmitterNetworkBuffer();

    long getTransmitterNetworkCapacity();

    @Nonnull
    FloatingLong getCapacityAsFloatingLong();

    long getCapacity();

    //TODO: Remove the need for this?
    Coord4D coord();

    Coord4D getAdjacentConnectableTransmitterCoord(Direction side);

    ACCEPTOR getAcceptor(Direction side);

    boolean isValid();

    boolean isOrphan();

    void setOrphan(boolean orphaned);

    NETWORK createEmptyNetwork();

    NETWORK createEmptyNetworkWithID(UUID networkID);

    NETWORK createNetworkByMerging(Collection<NETWORK> toMerge);

    NETWORK getExternalNetwork(Coord4D from);

    void takeShare();

    /**
     * @return Gets and releases the transmitter's buffer.
     *
     * @apiNote Should only be {@code null}, if the buffer type supports null. So things like fluid's should use the empty variant.
     */
    BUFFER releaseShare();

    /**
     * @return Gets the transmitter's buffer.
     *
     * @apiNote Should only be {@code null}, if the buffer type supports null. So things like fluid's should use the empty variant.
     */
    BUFFER getShare();

    /**
     * If the transmitter does not have a buffer this will try to fallback on the network's buffer.
     *
     * @return The transmitter's buffer, or if null the network's buffer.
     */
    @Nullable
    default BUFFER getBufferWithFallback() {
        BUFFER buffer = getShare();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer == null && hasTransmitterNetwork()) {
            return getTransmitterNetwork().getBuffer();
        }
        return buffer;
    }

    default boolean isCompatibleWith(TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> other) {
        return true;
    }
}