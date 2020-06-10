package mekanism.common.tile.transmitter;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

public abstract class TileEntityBufferedTransmitter<ACCEPTOR, NETWORK extends DynamicBufferedNetwork<ACCEPTOR, NETWORK, BUFFER, TRANSMITTER>, BUFFER,
      TRANSMITTER extends TileEntityBufferedTransmitter<ACCEPTOR, NETWORK, BUFFER, TRANSMITTER>> extends TileEntityTransmitter<ACCEPTOR, NETWORK, TRANSMITTER> {

    public TileEntityBufferedTransmitter(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    public long getTransmitterNetworkCapacity() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().getCapacity() : getCapacity();
    }

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
    public boolean isValidTransmitter(TileEntityTransmitter<?, ?, ?> tile) {
        if (canHaveIncompatibleNetworks() && tile instanceof TileEntityBufferedTransmitter) {
            TileEntityBufferedTransmitter<?, ?, ?, ?> other = (TileEntityBufferedTransmitter<?, ?, ?, ?>) tile;
            if (other.canHaveIncompatibleNetworks()) {
                //If it is a transmitter, only allow declare it as valid, if we don't have a combination
                // of a transmitter with a network and an orphaned transmitter, but only bother if
                // we can have incompatible networks
                if (hasTransmitterNetwork() && other.isOrphan() || other.hasTransmitterNetwork() && isOrphan()) {
                    return false;
                }
            }
        }
        return super.isValidTransmitter(tile);
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
            acceptorCache.currentAcceptorConnections = possibleAcceptors;
            if (allPossibleConnections != allCurrentConnections) {
                //If they don't match get the difference
                byte changedTransmitters = (byte) (allPossibleConnections ^ allCurrentConnections);
                //Inform the neighboring tiles that they should refresh their connection on the side we changed
                // This happens because we are no longer an orphan and want to tell the neighboring tiles about it
                for (Direction side : EnumUtils.DIRECTIONS) {
                    if (connectionMapContainsSide(changedTransmitters, side)) {
                        TileEntityTransmitter<?, ?, ?> tile = MekanismUtils.getTileEntity(TileEntityTransmitter.class, getWorld(), getPos().offset(side));
                        if (tile != null) {
                            tile.refreshConnections(side.getOpposite());
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
        TileEntityBufferedTransmitter<?, ?, ?, ?> other = MekanismUtils.getTileEntity(TileEntityBufferedTransmitter.class, getWorld(), getPos().offset(side));
        if (other != null) {
            NETWORK network = getTransmitterNetwork();
            //The other one should always have the same incompatible networks state as us
            // But just in case it doesn't just check the boolean
            if (other.canHaveIncompatibleNetworks() && other.hasTransmitterNetwork()) {
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
                    network.adoptTransmittersAndAcceptorsFrom(otherNetwork);
                    List<TRANSMITTER> otherTransmitters = new ArrayList<>(otherNetwork.getTransmitters());

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