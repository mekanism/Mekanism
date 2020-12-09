package mekanism.common.content.network.transmitter;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.lib.transmitter.acceptor.AbstractAcceptorCache;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.tile.interfaces.ITileWrapper;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

public abstract class Transmitter<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER>,
      TRANSMITTER extends Transmitter<ACCEPTOR, NETWORK, TRANSMITTER>> implements ITileWrapper {

    public static boolean connectionMapContainsSide(byte connections, Direction side) {
        byte tester = (byte) (1 << side.ordinal());
        return (connections & tester) > 0;
    }

    private static byte setConnectionBit(byte connections, boolean toSet, Direction side) {
        return (byte) ((connections & ~(byte) (1 << side.ordinal())) | (byte) ((toSet ? 1 : 0) << side.ordinal()));
    }

    public static ConnectionType getConnectionType(Direction side, byte allConnections, byte transmitterConnections, ConnectionType[] types) {
        if (!connectionMapContainsSide(allConnections, side)) {
            return ConnectionType.NONE;
        } else if (connectionMapContainsSide(transmitterConnections, side)) {
            return ConnectionType.NORMAL;
        }
        return types[side.ordinal()];
    }

    private ConnectionType[] connectionTypes = {ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL,
                                                ConnectionType.NORMAL};
    private final AbstractAcceptorCache<ACCEPTOR, ?> acceptorCache;
    public byte currentTransmitterConnections = 0x00;

    private final TileEntityTransmitter transmitterTile;
    private final Set<TransmissionType> supportedTransmissionTypes;
    protected boolean redstoneReactive;
    private boolean redstonePowered;
    private boolean redstoneSet;
    private NETWORK theNetwork = null;
    private boolean orphaned = true;
    protected boolean isUpgrading;

    public Transmitter(TileEntityTransmitter transmitterTile, TransmissionType... transmissionTypes) {
        this.transmitterTile = transmitterTile;
        acceptorCache = createAcceptorCache();
        supportedTransmissionTypes = EnumSet.noneOf(TransmissionType.class);
        supportedTransmissionTypes.addAll(Arrays.asList(transmissionTypes));
    }

    protected AbstractAcceptorCache<ACCEPTOR, ?> createAcceptorCache() {
        return new AcceptorCache<>(this, getTransmitterTile());
    }

    public AbstractAcceptorCache<ACCEPTOR, ?> getAcceptorCache() {
        return acceptorCache;
    }

    public TileEntityTransmitter getTransmitterTile() {
        return transmitterTile;
    }

    /**
     * @apiNote Don't use this to directly modify the backing array, use the helper set methods.
     */
    public ConnectionType[] getConnectionTypesRaw() {
        return connectionTypes;
    }

    public void setConnectionTypesRaw(@Nonnull ConnectionType[] connectionTypes) {
        if (this.connectionTypes.length != connectionTypes.length) {
            throw new IllegalArgumentException("Mismatched connection types length");
        }
        this.connectionTypes = connectionTypes;
    }

    public ConnectionType getConnectionTypeRaw(@Nonnull Direction side) {
        return connectionTypes[side.ordinal()];
    }

    public void setConnectionTypeRaw(@Nonnull Direction side, @Nonnull ConnectionType type) {
        int index = side.ordinal();
        ConnectionType old = connectionTypes[index];
        if (old != type) {
            connectionTypes[index] = type;
            getTransmitterTile().sideChanged(side, old, type);
        }
    }

    @Override
    public BlockPos getTilePos() {
        return transmitterTile.getPos();
    }

    @Override
    public World getTileWorld() {
        return transmitterTile.getWorld();
    }

    public boolean isRemote() {
        return transmitterTile.isRemote();
    }

    protected TRANSMITTER getTransmitter() {
        return (TRANSMITTER) this;
    }

    /**
     * Gets the network currently in use by this transmitter segment.
     *
     * @return network this transmitter is using
     */
    public NETWORK getTransmitterNetwork() {
        return theNetwork;
    }

    /**
     * Sets this transmitter segment's network to a new value.
     *
     * @param network - network to set to
     */
    public void setTransmitterNetwork(NETWORK network) {
        setTransmitterNetwork(network, true);
    }

    /**
     * Sets this transmitter segment's network to a new value.
     *
     * @param network    - network to set to
     * @param requestNow - Force a request now if not the return value will be if a request is needed
     */
    public boolean setTransmitterNetwork(NETWORK network, boolean requestNow) {
        if (theNetwork == network) {
            return false;
        }
        if (isRemote() && theNetwork != null) {
            theNetwork.removeTransmitter(getTransmitter());
        }
        theNetwork = network;
        orphaned = theNetwork == null;
        if (isRemote()) {
            if (theNetwork != null) {
                theNetwork.addTransmitter(getTransmitter());
            }
        } else if (requestNow) {
            //If we are requesting now request the update
            requestsUpdate();
        } else {
            //Otherwise return that we need to update it
            return true;
        }
        return false;
    }

    public boolean hasTransmitterNetwork() {
        return !isOrphan() && getTransmitterNetwork() != null;
    }

    public abstract NETWORK createEmptyNetwork();

    public abstract NETWORK createEmptyNetworkWithID(UUID networkID);

    public abstract NETWORK createNetworkByMerging(Collection<NETWORK> toMerge);

    public NETWORK getExternalNetwork(BlockPos from) {
        TileEntityTransmitter transmitter = WorldUtils.getTileEntity(TileEntityTransmitter.class, getTileWorld(), from);
        if (transmitter != null && supportsTransmissionType(transmitter)) {
            return (NETWORK) transmitter.getTransmitter().getTransmitterNetwork();
        }
        return null;
    }

    public boolean isValid() {
        return !getTransmitterTile().isRemoved() && getTransmitterTile().isLoaded();
    }


    public boolean isOrphan() {
        return orphaned;
    }

    public void setOrphan(boolean nowOrphaned) {
        orphaned = nowOrphaned;
    }

    /**
     * Get the transmitter's transmission types
     *
     * @return TransmissionType this transmitter uses
     */
    public Set<TransmissionType> getSupportedTransmissionTypes() {
        return supportedTransmissionTypes;
    }

    public boolean supportsTransmissionType(Transmitter<?, ?, ?> transmitter) {
        return transmitter.getSupportedTransmissionTypes().stream().anyMatch(supportedTransmissionTypes::contains);
    }

    public boolean supportsTransmissionType(TileEntityTransmitter transmitter) {
        return supportsTransmissionType(transmitter.getTransmitter());
    }

    @Nonnull
    public LazyOptional<ACCEPTOR> getAcceptor(Direction side) {
        return acceptorCache.getCachedAcceptor(side);
    }

    public boolean handlesRedstone() {
        return true;
    }

    /**
     * @apiNote Only call this from the server side
     */
    public byte getPossibleTransmitterConnections() {
        byte connections = 0x00;
        if (handlesRedstone() && redstoneReactive && redstonePowered) {
            return connections;
        }
        for (Direction side : EnumUtils.DIRECTIONS) {
            TileEntity tile = WorldUtils.getTileEntity(getTileWorld(), getTilePos().offset(side));
            if (canConnectMutual(side, tile) && tile instanceof TileEntityTransmitter) {
                Transmitter<?, ?, ?> transmitter = ((TileEntityTransmitter) tile).getTransmitter();
                if (supportsTransmissionType(transmitter) && isValidTransmitter(transmitter)) {
                    connections |= 1 << side.ordinal();
                }
            }
        }
        return connections;
    }

    /**
     * @apiNote Only call this from the server side
     */
    private boolean getPossibleAcceptorConnection(Direction side) {
        if (handlesRedstone() && redstoneReactive && redstonePowered) {
            return false;
        }
        TileEntity tile = WorldUtils.getTileEntity(getTileWorld(), getTilePos().offset(side));
        if (canConnectMutual(side, tile) && isValidAcceptor(tile, side)) {
            return true;
        }
        acceptorCache.invalidateCachedAcceptor(side);
        return false;
    }

    /**
     * @apiNote Only call this from the server side
     */
    private boolean getPossibleTransmitterConnection(Direction side) {
        if (handlesRedstone() && redstoneReactive && redstonePowered) {
            return false;
        }
        TileEntity tile = WorldUtils.getTileEntity(getTileWorld(), getTilePos().offset(side));
        if (canConnectMutual(side, tile) && tile instanceof TileEntityTransmitter) {
            Transmitter<?, ?, ?> transmitter = ((TileEntityTransmitter) tile).getTransmitter();
            return supportsTransmissionType(transmitter) && isValidTransmitter(transmitter);
        }
        return false;
    }

    /**
     * @apiNote Only call this from the server side
     */
    public byte getPossibleAcceptorConnections() {
        byte connections = 0x00;
        if (handlesRedstone() && redstoneReactive && redstonePowered) {
            return connections;
        }
        for (Direction side : EnumUtils.DIRECTIONS) {
            BlockPos offset = getTilePos().offset(side);
            TileEntity tile = WorldUtils.getTileEntity(getTileWorld(), offset);
            if (canConnectMutual(side, tile)) {
                if (!isRemote() && !WorldUtils.isBlockLoaded(getTileWorld(), offset)) {
                    getTransmitterTile().setForceUpdate();
                    continue;
                }
                if (isValidAcceptor(tile, side)) {
                    connections |= 1 << side.ordinal();
                    continue;
                }
            }
            acceptorCache.invalidateCachedAcceptor(side);
        }
        return connections;
    }

    public byte getAllCurrentConnections() {
        return (byte) (currentTransmitterConnections | acceptorCache.currentAcceptorConnections);
    }

    public boolean isValidTransmitter(Transmitter<?, ?, ?> transmitter) {
        return true;
    }

    public boolean canConnectToAcceptor(Direction side) {
        ConnectionType type = getConnectionTypeRaw(side);
        return type == ConnectionType.NORMAL || type == ConnectionType.PUSH;
    }

    @Nullable
    public BlockPos getAdjacentConnectableTransmitterPos(Direction side) {
        BlockPos sidePos = getTilePos().offset(side);
        TileEntity potentialTransmitterTile = WorldUtils.getTileEntity(getTileWorld(), sidePos);
        if (canConnectMutual(side, potentialTransmitterTile) && potentialTransmitterTile instanceof TileEntityTransmitter) {
            Transmitter<?, ?, ?> transmitter = ((TileEntityTransmitter) potentialTransmitterTile).getTransmitter();
            if (supportsTransmissionType(transmitter) && isValidTransmitter(transmitter)) {
                return sidePos;
            }
        }
        return null;
    }

    /**
     * @apiNote Only call this from the server side
     */
    public boolean isValidAcceptor(TileEntity tile, Direction side) {
        //TODO: Rename this method better to make it more apparent that it caches and also listens to the acceptor
        //If it isn't a transmitter or the transmission type is different than the one the transmitter has
        return !(tile instanceof TileEntityTransmitter) || !supportsTransmissionType((TileEntityTransmitter) tile);
    }

    public boolean canConnectMutual(Direction side, @Nullable TileEntity cachedTile) {
        if (!canConnect(side)) {
            return false;
        }
        if (cachedTile == null) {
            //If we don't already have the tile that is on the side calculated, do so
            cachedTile = WorldUtils.getTileEntity(getTileWorld(), getTilePos().offset(side));
        }
        return !(cachedTile instanceof TileEntityTransmitter) || ((TileEntityTransmitter) cachedTile).getTransmitter().canConnect(side.getOpposite());
    }

    public boolean canConnect(Direction side) {
        if (getConnectionTypeRaw(side) == ConnectionType.NONE) {
            return false;
        }
        if (handlesRedstone()) {
            if (!redstoneSet) {
                if (redstoneReactive) {
                    redstonePowered = WorldUtils.isGettingPowered(getTileWorld(), getTilePos());
                } else {
                    redstonePowered = false;
                }
                redstoneSet = true;
            }
            return !redstoneReactive || !redstonePowered;
        }
        return true;
    }

    /**
     * Only call on the server
     */
    public void requestsUpdate() {
        getTransmitterTile().sendUpdatePacket();
    }

    @Nonnull
    public CompoundNBT getReducedUpdateTag(CompoundNBT updateTag) {
        updateTag.putByte(NBTConstants.CURRENT_CONNECTIONS, currentTransmitterConnections);
        updateTag.putByte(NBTConstants.CURRENT_ACCEPTORS, acceptorCache.currentAcceptorConnections);
        for (Direction direction : EnumUtils.DIRECTIONS) {
            updateTag.putInt(NBTConstants.SIDE + direction.ordinal(), getConnectionTypeRaw(direction).ordinal());
        }
        //Transmitter
        if (hasTransmitterNetwork()) {
            updateTag.putUniqueId(NBTConstants.NETWORK, getTransmitterNetwork().getUUID());
        }
        return updateTag;
    }

    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        NBTUtils.setByteIfPresent(tag, NBTConstants.CURRENT_CONNECTIONS, connections -> currentTransmitterConnections = connections);
        NBTUtils.setByteIfPresent(tag, NBTConstants.CURRENT_ACCEPTORS, acceptors -> acceptorCache.currentAcceptorConnections = acceptors);
        for (Direction direction : EnumUtils.DIRECTIONS) {
            NBTUtils.setEnumIfPresent(tag, NBTConstants.SIDE + direction.ordinal(), ConnectionType::byIndexStatic, type -> setConnectionTypeRaw(direction, type));
        }
        //Transmitter
        NBTUtils.setUUIDIfPresentElse(tag, NBTConstants.NETWORK, networkID -> {
            if (hasTransmitterNetwork() && getTransmitterNetwork().getUUID().equals(networkID)) {
                //Nothing needs to be done
                return;
            }
            DynamicNetwork<?, ?, ?> clientNetwork = TransmitterNetworkRegistry.getInstance().getClientNetwork(networkID);
            if (clientNetwork == null) {
                NETWORK network = createEmptyNetworkWithID(networkID);
                network.register();
                setTransmitterNetwork(network);
                handleContentsUpdateTag(network, tag);
            } else {
                //TODO: Validate network type?
                updateClientNetwork((NETWORK) clientNetwork);
            }
        }, () -> setTransmitterNetwork(null));
    }

    protected void updateClientNetwork(@Nonnull NETWORK network) {
        network.register();
        setTransmitterNetwork(network);
    }

    protected void handleContentsUpdateTag(@Nonnull NETWORK network, @Nonnull CompoundNBT tag) {
    }

    public void read(@Nonnull CompoundNBT nbtTags) {
        redstoneReactive = nbtTags.getBoolean(NBTConstants.REDSTONE);
        for (Direction direction : EnumUtils.DIRECTIONS) {
            NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.CONNECTION + direction.ordinal(), ConnectionType::byIndexStatic, type -> setConnectionTypeRaw(direction, type));
        }
    }

    @Nonnull
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        nbtTags.putBoolean(NBTConstants.REDSTONE, redstoneReactive);
        for (Direction direction : EnumUtils.DIRECTIONS) {
            nbtTags.putInt(NBTConstants.CONNECTION + direction.ordinal(), getConnectionTypeRaw(direction).ordinal());
        }
        return nbtTags;
    }

    private void recheckRedstone() {
        if (handlesRedstone()) {
            boolean previouslyPowered = redstonePowered;
            if (redstoneReactive) {
                redstonePowered = WorldUtils.isGettingPowered(getTileWorld(), getTilePos());
            } else {
                redstonePowered = false;
            }
            //If the redstone mode changed properly update the connection to other transmitters/networks
            if (previouslyPowered != redstonePowered) {
                //Has to be markDirtyTransmitters instead of notify tile change
                // or it will not properly tell the neighboring connections that
                // it is no longer valid
                markDirtyTransmitters();
            }
            redstoneSet = true;
        }
    }

    public void refreshConnections() {
        if (!isRemote()) {
            recheckRedstone();
            byte possibleTransmitters = getPossibleTransmitterConnections();
            byte possibleAcceptors = getPossibleAcceptorConnections();
            byte newlyEnabledTransmitters = 0;
            boolean sendDesc = false;
            if ((possibleTransmitters | possibleAcceptors) != getAllCurrentConnections()) {
                sendDesc = true;
                if (possibleTransmitters != currentTransmitterConnections) {
                    //If they don't match get the difference
                    newlyEnabledTransmitters = (byte) (possibleTransmitters ^ currentTransmitterConnections);
                    //Now remove all bits that already where enabled so we only have the
                    // ones that are newly enabled. There is no need to recheck for a
                    // network merge on two transmitters if one is no longer accessible
                    newlyEnabledTransmitters &= ~currentTransmitterConnections;
                }
            }

            currentTransmitterConnections = possibleTransmitters;
            acceptorCache.currentAcceptorConnections = possibleAcceptors;
            if (newlyEnabledTransmitters != 0) {
                //If any sides are now valid transmitters that were not before recheck the connection
                recheckConnections(newlyEnabledTransmitters);
            }
            if (sendDesc) {
                getTransmitterTile().sendUpdatePacket();
            }
        }
    }

    public void refreshConnections(Direction side) {
        if (!isRemote()) {
            boolean possibleTransmitter = getPossibleTransmitterConnection(side);
            boolean possibleAcceptor = getPossibleAcceptorConnection(side);
            boolean transmitterChanged = false;
            boolean sendDesc = false;
            if ((possibleTransmitter || possibleAcceptor) != connectionMapContainsSide(getAllCurrentConnections(), side)) {
                sendDesc = true;
                if (possibleTransmitter != connectionMapContainsSide(currentTransmitterConnections, side)) {
                    //If it doesn't match check if it is now enabled, as we don't care about it changing to disabled
                    transmitterChanged = possibleTransmitter;
                }
            }

            currentTransmitterConnections = setConnectionBit(currentTransmitterConnections, possibleTransmitter, side);
            acceptorCache.currentAcceptorConnections = setConnectionBit(acceptorCache.currentAcceptorConnections, possibleAcceptor, side);
            if (transmitterChanged) {
                //If this side is now a valid transmitter and it wasn't before recheck the connection
                recheckConnection(side);
            }
            if (sendDesc) {
                getTransmitterTile().sendUpdatePacket();
            }
        }
    }

    /**
     * @param newlyEnabledTransmitters The transmitters that are now enabled and were not before.
     *
     * @apiNote Only call this from the server side
     */
    protected void recheckConnections(byte newlyEnabledTransmitters) {
        if (!hasTransmitterNetwork()) {
            //If we don't have a transmitter network then recheck connection status both ways if the other tile is also a transmitter
            //This fixes pipes not reconnecting cross chunk
            for (Direction side : EnumUtils.DIRECTIONS) {
                if (connectionMapContainsSide(newlyEnabledTransmitters, side)) {
                    TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, getTileWorld(), getTilePos().offset(side));
                    if (tile != null) {
                        tile.getTransmitter().refreshConnections(side.getOpposite());
                    }
                }
            }
        }
    }

    /**
     * @param side The side that a transmitter is now enabled on after having been disabled.
     *
     * @apiNote Only call this from the server side
     */
    protected void recheckConnection(Direction side) {
    }

    public void onModeChange(Direction side) {
        markDirtyAcceptor(side);
        if (getPossibleTransmitterConnections() != currentTransmitterConnections) {
            markDirtyTransmitters();
        }
        getTransmitterTile().markDirty(false);
    }

    public void onNeighborTileChange(Direction side) {
        refreshConnections(side);
    }

    public void onNeighborBlockChange(Direction side) {
        if (handlesRedstone() && redstoneReactive) {
            //If our tile can handle redstone and we are redstone reactive we need to recheck all connections
            // as the power might have changed and we may have to update our own visuals
            refreshConnections();
        } else {
            //Otherwise we can just get away with checking the single side
            refreshConnections(side);
        }
    }

    protected void markDirtyTransmitters() {
        notifyTileChange();
        if (hasTransmitterNetwork()) {
            TransmitterNetworkRegistry.invalidateTransmitter(getTransmitter());
        }
    }

    public void markDirtyAcceptor(Direction side) {
        if (hasTransmitterNetwork()) {
            getTransmitterNetwork().acceptorChanged(getTransmitter(), side);
        }
    }

    public void remove() {
        //Clear our cached listeners
        acceptorCache.clear();
    }

    public void onChunkUnload() {
        //Clear our cached listeners
        acceptorCache.clear();
    }

    public ConnectionType getConnectionType(Direction side) {
        return getConnectionType(side, getAllCurrentConnections(), currentTransmitterConnections, connectionTypes);
    }

    public Set<Direction> getConnections(ConnectionType type) {
        Set<Direction> sides = EnumSet.noneOf(Direction.class);
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (getConnectionType(side) == type) {
                sides.add(side);
            }
        }
        return sides;
    }

    public ActionResultType onConfigure(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        if (handlesRedstone()) {
            redstoneReactive ^= true;
            refreshConnections();
            notifyTileChange();
            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, EnumColor.GRAY,
                  MekanismLang.REDSTONE_SENSITIVITY.translate(EnumColor.INDIGO, OnOff.of(redstoneReactive))), Util.DUMMY_UUID);
        }
        return ActionResultType.SUCCESS;
    }

    public void notifyTileChange() {
        WorldUtils.notifyLoadedNeighborsOfTileChange(getTileWorld(), getTilePos());
    }

    public abstract void takeShare();

    public void startUpgrading() {
        isUpgrading = true;
        takeShare();
        setTransmitterNetwork(null);
    }
}