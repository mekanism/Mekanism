package mekanism.common.content.network.transmitter;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

public abstract class BufferedTransmitter<ACCEPTOR, NETWORK extends DynamicBufferedNetwork<ACCEPTOR, NETWORK, BUFFER, TRANSMITTER>, BUFFER,
      TRANSMITTER extends BufferedTransmitter<ACCEPTOR, NETWORK, BUFFER, TRANSMITTER>> extends Transmitter<ACCEPTOR, NETWORK, TRANSMITTER> {

    public BufferedTransmitter(TileEntityTransmitter tile, TransmissionType... transmissionTypes) {
        super(tile, transmissionTypes);
    }

    public long getTransmitterNetworkCapacity() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().getCapacity() : getCapacity();
    }

    /**
     * @apiNote Only call from the server side
     */
    protected abstract void pullFromAcceptors();

    public abstract long getCapacity();

    /**
     * If the transmitter does not have a buffer this will try to fallback on the network's buffer.
     *
     * @return The transmitter's buffer, or if null the network's buffer.
     */
    @Nonnull
    public abstract BUFFER getBufferWithFallback();

    /**
     * @return True if the buffer with fallback is null (or empty)
     */
    public abstract boolean noBufferOrFallback();

    protected boolean canHaveIncompatibleNetworks() {
        return false;
    }

    @Override
    public boolean isValidTransmitter(Transmitter<?, ?, ?> transmitter) {
        if (canHaveIncompatibleNetworks() && transmitter instanceof BufferedTransmitter) {
            BufferedTransmitter<?, ?, ?, ?> other = (BufferedTransmitter<?, ?, ?, ?>) transmitter;
            if (other.canHaveIncompatibleNetworks()) {
                //If it is a transmitter, only declare it as valid, if we don't have a combination
                // of a transmitter with a network and an orphaned transmitter, but only bother if
                // we can have incompatible networks
                if (hasTransmitterNetwork() && other.isOrphan() || other.hasTransmitterNetwork() && isOrphan()) {
                    return false;
                }
            }
        }
        return super.isValidTransmitter(transmitter);
    }

    @Override
    public void requestsUpdate() {
        if (canHaveIncompatibleNetworks()) {
            //If we can have incompatible networks, we need to update our connections
            // and potentially inform our neighbors we are connecting to, to also update their connections
            //Note: This is not needed if we cannot have incompatible networks as then we
            // are able to just directly connect to orphans
            byte possibleTransmitters = getPossibleTransmitterConnections();
            byte possibleAcceptors = getPossibleAcceptorConnections();
            byte allPossibleConnections = (byte) (possibleTransmitters | possibleAcceptors);
            byte allCurrentConnections = getAllCurrentConnections();
            //Update our connections in case they changed
            //Note: We cannot just do this in the if statement in case one changed from transmitter to acceptor
            currentTransmitterConnections = possibleTransmitters;
            getAcceptorCache().currentAcceptorConnections = possibleAcceptors;
            if (allPossibleConnections != allCurrentConnections) {
                //If they don't match get the difference
                byte changedTransmitters = (byte) (allPossibleConnections ^ allCurrentConnections);
                //Inform the neighboring tiles that they should refresh their connection on the side we changed
                // This happens because we are no longer an orphan and want to tell the neighboring tiles about it
                for (Direction side : EnumUtils.DIRECTIONS) {
                    if (connectionMapContainsSide(changedTransmitters, side)) {
                        TileEntityTransmitter tile = MekanismUtils.getTileEntity(TileEntityTransmitter.class, getTileWorld(), getTilePos().offset(side));
                        if (tile != null) {
                            tile.getTransmitter().refreshConnections(side.getOpposite());
                        }
                    }
                }
            }
        }
        super.requestsUpdate();
    }

    @Override
    protected void recheckConnections(byte newlyEnabledTransmitters) {
        if (hasTransmitterNetwork()) {
            if (canHaveIncompatibleNetworks()) {
                //We only need to check if we can have incompatible networks and if we actually have a network
                for (Direction side : EnumUtils.DIRECTIONS) {
                    if (connectionMapContainsSide(newlyEnabledTransmitters, side)) {
                        //Recheck the side that is now enabled, as we manually merge this
                        // cannot be simplified to a first match is good enough
                        recheckConnectionPrechecked(side);
                    }
                }
            }
        } else {
            super.recheckConnections(newlyEnabledTransmitters);
        }
    }

    @Override
    protected void recheckConnection(Direction side) {
        if (canHaveIncompatibleNetworks() && hasTransmitterNetwork()) {
            //We only need to check if we can have incompatible networks and if we actually have a network
            recheckConnectionPrechecked(side);
        }
    }

    private void recheckConnectionPrechecked(Direction side) {
        TileEntityTransmitter otherTile = MekanismUtils.getTileEntity(TileEntityTransmitter.class, getTileWorld(), getTilePos().offset(side));
        if (otherTile != null) {
            NETWORK network = getTransmitterNetwork();
            //The other one should always have the same incompatible networks state as us
            // But just in case it doesn't just check the boolean
            Transmitter<?, ?, ?> other = otherTile.getTransmitter();
            if (other instanceof BufferedTransmitter && ((BufferedTransmitter<?, ?, ?, ?>) other).canHaveIncompatibleNetworks() && other.hasTransmitterNetwork()) {
                NETWORK otherNetwork = (NETWORK) other.getTransmitterNetwork();
                if (network != otherNetwork && network.isCompatibleWith(otherNetwork)) {
                    //We have two networks that are now compatible and they are not the same source network
                    // The most common cause they would be same source network is that they would merge
                    // from the first pipe checking when it attempts to reconnect, and then the second
                    // pipe still is going to be checking the connection.

                    if (noBufferOrFallback()) {
                        //If we don't have any use them as primary network
                        NETWORK tempNetwork = network;
                        network = otherNetwork;
                        otherNetwork = tempNetwork;
                    }

                    // Manually merge the networks.
                    // This code is not in network registry as there is special handling needed to ensure
                    // it visually updates properly. There also were above checks that get us to a certain
                    // point where we can make some assumptions about the networks and if it is actually
                    // valid to merge them when otherwise people may try to merge things when they shouldn't
                    // be merged causing unexpected bugs.
                    List<TRANSMITTER> otherTransmitters = network.adoptTransmittersAndAcceptorsFrom(otherNetwork);

                    //Unregister the other network
                    otherNetwork.deregister();
                    //Commit the changes of the new network
                    network.commit();

                    //We did not have these as part of the update because they got directly added
                    // This means that we have to update the buffer and queue client updates ourselves
                    network.clampBuffer();
                    //Recheck the connections
                    other.refreshConnections(side.getOpposite());
                    //Force all the newly merged transmitters to send a sync update to the client
                    // to ensure that they now have the proper network id on the client
                    // Note: adoptTransmittersAndAcceptorsFrom will return all the new transmitters except for
                    // those that already have our network as their network (which should be none of them)
                    for (TRANSMITTER otherTransmitter : otherTransmitters) {
                        otherTransmitter.requestsUpdate();
                    }
                }
            }
        }
    }

    @Override
    protected void handleContentsUpdateTag(@Nonnull NETWORK network, @Nonnull CompoundNBT tag) {
        network.updateCapacity();
    }

    @Override
    protected void updateClientNetwork(@Nonnull NETWORK network) {
        super.updateClientNetwork(network);
        network.updateCapacity();
    }

    /**
     * @return Gets and releases the transmitter's buffer.
     *
     * @apiNote Should only be {@code null}, if the buffer type supports null. So things like fluid's should use the empty variant.
     */
    public abstract BUFFER releaseShare();

    /**
     * @return Gets the transmitter's buffer.
     */
    @Nonnull
    public abstract BUFFER getShare();
}