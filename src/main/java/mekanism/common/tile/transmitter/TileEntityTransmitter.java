package mekanism.common.tile.transmitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IAlloyInteraction;
import mekanism.api.NBTConstants;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.transmitters.TransmitterImpl;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public abstract class TileEntityTransmitter<A, N extends DynamicNetwork<A, N, BUFFER>, BUFFER> extends TileEntitySidedPipe implements IAlloyInteraction {

    @Nonnull
    public TransmitterImpl<A, N, BUFFER> transmitterDelegate;

    public TileEntityTransmitter(IBlockProvider blockProvider) {
        super(((IHasTileEntity<? extends TileEntityTransmitter>) blockProvider.getBlock()).getTileType());
        transmitterDelegate = new TransmitterImpl<>(this);
    }

    @Nonnull
    public TransmitterImpl<A, N, BUFFER> getTransmitter() {
        return transmitterDelegate;
    }

    public abstract N createNewNetwork();

    public abstract N createNewNetworkWithID(UUID networkID);

    public abstract N createNetworkByMerging(Collection<N> networks);

    @Override
    public void onWorldJoin() {
        if (!isRemote()) {
            TransmitterNetworkRegistry.registerOrphanTransmitter(getTransmitter());
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (!isRemote()) {
            getTransmitter().takeShare();
        }
        super.onChunkUnloaded();
    }

    @Override
    public void onWorldSeparate() {
        if (isRemote()) {
            getTransmitter().setTransmitterNetwork(null);
        } else {
            TransmitterNetworkRegistry.invalidateTransmitter(getTransmitter());
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
                for (Direction side : EnumUtils.DIRECTIONS) {
                    if (connectionMapContainsSide(newlyEnabledTransmitters, side)) {
                        //Recheck the side that is now enabled, as we manually merge this
                        // cannot be simplified to a first match is good enough
                        recheckConnectionPrechecked(side);
                    }
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
            recheckConnectionPrechecked(side);
        }
    }

    private void recheckConnectionPrechecked(Direction side) {
        TileEntityTransmitter<?, ?, ?> other = MekanismUtils.getTileEntity(TileEntityTransmitter.class, getWorld(), getPos().offset(side));
        if (other != null) {
            N network = getTransmitter().getTransmitterNetwork();
            //The other one should always have the same incompatible networks state as us
            // But just in case it doesn't just check the boolean
            if (other.canHaveIncompatibleNetworks() && other.getTransmitter().hasTransmitterNetwork()) {
                N otherNetwork = (N) other.getTransmitter().getTransmitterNetwork();
                if (network != otherNetwork && network.isCompatibleWith(otherNetwork)) {
                    //We have two networks that are now compatible and they are not the same source network
                    // The most common cause they would be same source network is that they would merge
                    // from the first pipe checking when it attempts to reconnect, and then the second
                    // pipe still is going to be checking the connection.

                    if (noBufferOrFallback()) {
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
                    List<IGridTransmitter<A, N, BUFFER>> otherTransmitters = new ArrayList<>(otherNetwork.getTransmitters());

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
                    for (IGridTransmitter<A, N, BUFFER> otherTransmitter : otherTransmitters) {
                        otherTransmitter.setRequestsUpdate();
                    }
                }
            }
        }
    }

    /**
     * Only call on the server
     */
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
            currentAcceptorConnections = possibleAcceptors;
            if (allPossibleConnections != allCurrentConnections) {
                //If they don't match get the difference
                byte changedTransmitters = (byte) (allPossibleConnections ^ allCurrentConnections);
                //Inform the neighboring tiles that they should refresh their connection on the side we changed
                // This happens because we are no longer an orphan and want to tell the neighboring tiles about it
                for (Direction side : EnumUtils.DIRECTIONS) {
                    if (connectionMapContainsSide(changedTransmitters, side)) {
                        TileEntitySidedPipe tile = MekanismUtils.getTileEntity(TileEntitySidedPipe.class, getWorld(), getPos().offset(side));
                        if (tile != null) {
                            tile.refreshConnections(side.getOpposite());
                        }
                    }
                }
            }
        }
        sendUpdatePacket();
    }

    @Override
    public boolean isValidTransmitter(TileEntity tile) {
        if (tile instanceof TileEntityTransmitter && canHaveIncompatibleNetworks()) {
            TileEntityTransmitter<?, ?, ?> other = (TileEntityTransmitter<?, ?, ?>) tile;
            if (other.canHaveIncompatibleNetworks()) {
                //If it is a transmitter, only allow declare it as valid, if we don't have a combination
                // of a transmitter with a network and an orphaned transmitter, but only bother if
                // we can have incompatible networks
                if (getTransmitter().hasTransmitterNetwork() && other.getTransmitter().isOrphan()) {
                    return false;
                } else if (other.getTransmitter().hasTransmitterNetwork() && getTransmitter().isOrphan()) {
                    return false;
                }
            }
        }
        return true;
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
    public void onAlloyInteraction(PlayerEntity player, Hand hand, ItemStack stack, @Nonnull AlloyTier tier) {
        if (getWorld() != null && getTransmitter().hasTransmitterNetwork()) {
            N transmitterNetwork = getTransmitter().getTransmitterNetwork();
            List<IGridTransmitter<A, N, BUFFER>> list = new ArrayList<>(transmitterNetwork.getTransmitters());
            list.sort((o1, o2) -> {
                if (o1 != null && o2 != null) {
                    BlockPos o1Pos = o1.coord().getPos();
                    BlockPos o2Pos = o2.coord().getPos();
                    return Double.compare(o1Pos.distanceSq(getPos()), o2Pos.distanceSq(getPos()));
                }
                return 0;
            });
            int upgraded = 0;
            for (IGridTransmitter<A, N, BUFFER> iter : list) {
                if (iter instanceof TransmitterImpl) {
                    TransmitterImpl<A, N, BUFFER> transmitter = (TransmitterImpl<A, N, BUFFER>) iter;
                    TileEntityTransmitter<A, N, BUFFER> t = transmitter.containingTile;
                    if (t.canUpgrade(tier)) {
                        BlockState state = t.getBlockState();
                        BlockState upgradeState = t.upgradeResult(state, tier.getBaseTier());
                        if (state == upgradeState) {
                            //Skip if it would not actually upgrade anything
                            continue;
                        }
                        transmitter.takeShare();
                        transmitter.setTransmitterNetwork(null);
                        TransmitterUpgradeData upgradeData = t.getUpgradeData();
                        if (upgradeData == null) {
                            Mekanism.logger.warn("Got no upgrade data for transmitter at position: {} in {} but it said it would be able to provide some.", t.getPos(), t.getWorld());
                        } else {
                            t.getWorld().setBlockState(t.getPos(), upgradeState);
                            TileEntityTransmitter<?, ?, ?> upgradedTile = MekanismUtils.getTileEntity(TileEntityTransmitter.class, t.getWorld(), t.getPos());
                            if (upgradedTile == null) {
                                Mekanism.logger.warn("Error upgrading transmitter at position: {} in {}.", t.getPos(), t.getWorld());
                            } else {
                                upgradedTile.parseUpgradeData(upgradeData);
                                upgraded++;
                                if (upgraded == 8) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (upgraded > 0) {
                //Invalidate the network so that it properly has new references to everything
                transmitterNetwork.invalidate(null);
                if (!player.isCreative()) {
                    stack.shrink(1);
                    if (stack.getCount() == 0) {
                        player.setHeldItem(hand, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    protected boolean canUpgrade(AlloyTier tier) {
        return false;
    }

    @Nonnull
    protected BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
        return current;
    }

    @Nullable
    protected TransmitterUpgradeData getUpgradeData() {
        return null;
    }

    protected void parseUpgradeData(@Nonnull TransmitterUpgradeData upgradeData) {
        Mekanism.logger.warn("Unhandled upgrade data.", new Throwable());
    }

    @Nonnull
    public FloatingLong getCapacityAsFloatingLong() {
        //Note: If you plan on actually using this, override it in your tile
        return FloatingLong.create(getCapacity());
    }

    public abstract int getCapacity();

    @Nullable
    public abstract BUFFER getBuffer();

    /**
     * @return True if the buffer with fallback is null (or empty)
     */
    public boolean noBufferOrFallback() {
        return getBufferWithFallback() == null;
    }

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

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        //If any of the block is in view, then allow rendering the contents
        return new AxisAlignedBB(pos, pos.add(1, 1, 1));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.GRID_TRANSMITTER_CAPABILITY) {
            return Capabilities.GRID_TRANSMITTER_CAPABILITY.orEmpty(capability, LazyOptional.of(this::getTransmitter));
        } else if (capability == Capabilities.ALLOY_INTERACTION_CAPABILITY) {
            return Capabilities.ALLOY_INTERACTION_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        TransmitterImpl<A, N, BUFFER> transmitter = getTransmitter();
        if (transmitter.hasTransmitterNetwork()) {
            updateTag.putUniqueId(NBTConstants.NETWORK, transmitter.getTransmitterNetwork().getUUID());
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        TransmitterImpl<A, N, BUFFER> transmitter = getTransmitter();
        if (tag.hasUniqueId(NBTConstants.NETWORK)) {
            UUID networkID = tag.getUniqueId(NBTConstants.NETWORK);
            if (transmitter.hasTransmitterNetwork() && transmitter.getTransmitterNetwork().getUUID().equals(networkID)) {
                //Nothing needs to be done
                return;
            }
            TransmitterNetworkRegistry networkRegistry = TransmitterNetworkRegistry.getInstance();
            DynamicNetwork<?, ?, ?> clientNetwork = networkRegistry.getClientNetwork(networkID);
            if (clientNetwork == null) {
                N network = transmitter.createEmptyNetworkWithID(networkID);
                network.register();
                transmitter.setTransmitterNetwork(network);
                network.updateCapacity();
                handleContentsUpdateTag(network, tag);
            } else {
                clientNetwork.register();
                //TODO: Validate network type?
                transmitter.setTransmitterNetwork((N) clientNetwork);
                clientNetwork.updateCapacity();
            }
        } else {
            transmitter.setTransmitterNetwork(null);
        }
    }

    protected void handleContentsUpdateTag(@Nonnull N network, @Nonnull CompoundNBT tag) {
    }
}