package mekanism.common.tile.transmitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IAlloyInteraction;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.DynamicNetwork.NetworkClientRequest;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.transmitters.TransmitterImpl;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public abstract class TileEntityTransmitter<A, N extends DynamicNetwork<A, N, BUFFER>, BUFFER> extends TileEntitySidedPipe implements IAlloyInteraction {

    @Nonnull
    public TransmitterImpl<A, N, BUFFER> transmitterDelegate;

    public boolean unloaded = true;
    public boolean dataRequest = false;
    public boolean delayedRefresh = false;

    private N lastClientNetwork = null;

    public TileEntityTransmitter(TileEntityType<? extends TileEntityTransmitter> type) {
        super(type);
        transmitterDelegate = new TransmitterImpl<>(this);
    }

    @Nonnull
    public TransmitterImpl<A, N, BUFFER> getTransmitter() {
        return transmitterDelegate;
    }

    public abstract N createNewNetwork();

    public abstract N createNetworkByMerging(Collection<N> networks);

    @Override
    public void onWorldJoin() {
        if (!getWorld().isRemote) {
            TransmitterNetworkRegistry.registerOrphanTransmitter(getTransmitter());
        } else if (lastClientNetwork != null) {
            getTransmitter().setTransmitterNetwork(lastClientNetwork);
        }
        unloaded = false;
    }

    @Override
    public void tick() {
        super.tick();
        if (delayedRefresh) {
            //Gets run the tick after the variable has been set. This is enough
            // time to ensure that the transmitter has been registered.
            delayedRefresh = false;
            refreshConnections();
        }

        if (getWorld().isRemote) {
            if (!dataRequest) {
                dataRequest = true;
                MinecraftForge.EVENT_BUS.post(new NetworkClientRequest(getWorld().getTileEntity(getPos())));
            }
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (!getWorld().isRemote) {
            getTransmitter().takeShare();
        }
        super.onChunkUnloaded();
    }

    @Override
    public void onWorldSeparate() {
        unloaded = true;
        if (!getWorld().isRemote) {
            TransmitterNetworkRegistry.invalidateTransmitter(getTransmitter());
        } else {
            lastClientNetwork = getTransmitter().getTransmitterNetwork();
            getTransmitter().setTransmitterNetwork(null);
        }
    }

    @Override
    public void markDirtyTransmitters() {
        super.markDirtyTransmitters();
        if (getTransmitter().hasTransmitterNetwork()) {
            TransmitterNetworkRegistry.invalidateTransmitter(getTransmitter());
        }
    }

    @Override
    public void markDirtyAcceptor(Direction side) {
        super.markDirtyAcceptor(side);
        if (getTransmitter().hasTransmitterNetwork()) {
            getTransmitter().getTransmitterNetwork().acceptorChanged(getTransmitter(), side);
        }
    }

    protected boolean canHaveIncompatibleNetworks() {
        return false;
    }

    @Override
    protected void recheckConnections(byte newlyEnabledTransmitters) {
        if (getTransmitter().hasTransmitterNetwork()) {
            if (canHaveIncompatibleNetworks()) {
                //We only need to check if we can have incompatible networks and if we actually have a network
                boolean networkUpdated = false;
                for (Direction side : Direction.values()) {
                    if (connectionMapContainsSide(newlyEnabledTransmitters, side)) {
                        //Recheck the side that is now enabled, as we manually merge this
                        // cannot be simplified to a first match is good enough
                        networkUpdated |= recheckConnectionPrechecked(side);
                    }
                }
                if (networkUpdated) {
                    refreshNetwork();
                }
            }
        } else {
            //If we don't have a transmitter network then recheck connection status both ways
            super.recheckConnections(newlyEnabledTransmitters);
        }
    }

    @Override
    protected void recheckConnection(Direction side) {
        if (canHaveIncompatibleNetworks() && getTransmitter().hasTransmitterNetwork()) {
            //We only need to check if we can have incompatible networks and if we actually have a network
            if (recheckConnectionPrechecked(side)) {
                refreshNetwork();
            }
        }
    }

    private void refreshNetwork() {
        //Queue an update for all the transmitters in the network just in case something went wrong
        // and to update the rendering of them
        N network = getTransmitter().getTransmitterNetwork();
        network.queueClientUpdate(network.getTransmitters());
        //Copy values into an array so that we don't risk a CME
        IGridTransmitter[] transmitters = network.getTransmitters().toArray(new IGridTransmitter[0]);
        //TODO: Make some better way of refreshing the connections, given we only need to refresh
        // connections to ourself anyways
        // The best way to do this is probably by making a method that updates the values for
        // the valid transmitters manually if the network is the same object.
        for (IGridTransmitter transmitter : transmitters) {
            if (transmitter instanceof TransmitterImpl) {
                //Refresh the connections because otherwise sometimes they need to wait for a block update
                ((TransmitterImpl) transmitter).containingTile.refreshConnections();
            }
        }
    }

    private boolean recheckConnectionPrechecked(Direction side) {
        final TileEntity tileEntity = MekanismUtils.getTileEntity(world, getPos().offset(side));
        if (tileEntity instanceof TileEntityTransmitter) {
            N network = getTransmitter().getTransmitterNetwork();
            TileEntityTransmitter other = (TileEntityTransmitter) tileEntity;
            //The other one should always have the same incompatible networks state as us
            // But just in case it doesn't just check the boolean
            if (other.canHaveIncompatibleNetworks() && other.getTransmitter().hasTransmitterNetwork()) {
                N otherNetwork = (N) other.getTransmitter().getTransmitterNetwork();
                if (network != otherNetwork && network.isCompatibleWith(otherNetwork)) {
                    //We have two networks that are now compatible and they are not the same source network
                    // The most common cause they would be same source network is that they would merge
                    // from the first pipe checking when it attempts to reconnect, and then the second
                    // pipe still is going to be checking the connection.

                    if (getBufferWithFallback() == null) {
                        //If we don't have any use them as primary network
                        N tempNetwork = network;
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
                    //Unregister the other network
                    otherNetwork.deregister();
                    //Commit the changes of the new network
                    network.commit();

                    //We did not have these as part of the update because they got directly added
                    // This means that we have to update the capacity/buffer and queue client updates
                    // ourselves
                    network.updateCapacity();
                    network.clampBuffer();
                    return true;
                }
            }
        }
        return false;
    }

    public abstract A getCachedAcceptor(Direction side);

    protected TileEntity getCachedTile(Direction side) {
        ConnectionType type = connectionTypes[side.ordinal()];
        if (type == ConnectionType.PULL || type == ConnectionType.NONE) {
            return null;
        }
        return connectionMapContainsSide(currentAcceptorConnections, side) ? cachedAcceptors[side.ordinal()] : null;
    }

    @Override
    public void onAlloyInteraction(PlayerEntity player, Hand hand, ItemStack stack, int tierOrdinal) {
        if (getTransmitter().hasTransmitterNetwork()) {
            int upgraded = 0;
            List<IGridTransmitter<A, N, BUFFER>> list = new ArrayList<>(getTransmitter().getTransmitterNetwork().getTransmitters());
            list.sort((o1, o2) -> {
                if (o1 != null && o2 != null) {
                    Coord4D thisCoord = new Coord4D(getPos(), getWorld());

                    Coord4D o1Coord = o1.coord();
                    Coord4D o2Coord = o2.coord();

                    return Integer.compare(o1Coord.distanceTo(thisCoord), o2Coord.distanceTo(thisCoord));
                }

                return 0;
            });
            for (IGridTransmitter<A, N, BUFFER> iter : list) {
                if (iter instanceof TransmitterImpl) {
                    TileEntityTransmitter t = ((TransmitterImpl) iter).containingTile;
                    if (t.upgrade(tierOrdinal)) {
                        upgraded++;
                        if (upgraded == 8) {
                            break;
                        }
                    }
                }
            }
            if (upgraded > 0) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                    if (stack.getCount() == 0) {
                        player.setHeldItem(hand, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    public boolean upgrade(int tierOrdinal) {
        return false;
    }

    public abstract int getCapacity();

    @Nullable
    public abstract BUFFER getBuffer();

    @Nullable
    public BUFFER getBufferWithFallback() {
        BUFFER buffer = getBuffer();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer == null && getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().getBuffer();
        }
        return buffer;
    }

    public abstract void takeShare();

    public abstract void updateShare();

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.GRID_TRANSMITTER_CAPABILITY) {
            return Capabilities.GRID_TRANSMITTER_CAPABILITY.orEmpty(capability, LazyOptional.of(this::getTransmitter));
        }
        if (capability == Capabilities.ALLOY_INTERACTION_CAPABILITY) {
            return Capabilities.ALLOY_INTERACTION_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }
}