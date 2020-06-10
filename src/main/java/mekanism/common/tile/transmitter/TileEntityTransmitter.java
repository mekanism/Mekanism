package mekanism.common.tile.transmitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IAlloyInteraction;
import mekanism.api.IConfigurable;
import mekanism.api.NBTConstants;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.states.TransmitterType.Size;
import mekanism.common.block.transmitter.BlockLargeTransmitter;
import mekanism.common.block.transmitter.BlockSmallTransmitter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.lib.transmitter.AcceptorCache;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.MultipartUtils.AdvancedRayTraceResult;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Pair;

//TODO - V10: Re-order various methods that are in this class
public abstract class TileEntityTransmitter<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER>,
      TRANSMITTER extends TileEntityTransmitter<ACCEPTOR, NETWORK, TRANSMITTER>>
      extends CapabilityTileEntity implements IConfigurable, ITickableTileEntity, IAlloyInteraction {

    public static final ModelProperty<TransmitterModelData> TRANSMITTER_PROPERTY = new ModelProperty<>();

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

    public ConnectionType[] connectionTypes = {ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL,
                                               ConnectionType.NORMAL};
    protected final AcceptorCache<ACCEPTOR> acceptorCache;
    public byte currentTransmitterConnections = 0x00;
    protected boolean redstoneReactive;
    private boolean redstonePowered;
    private boolean redstoneSet;
    private boolean forceUpdate = true;
    private NETWORK theNetwork = null;
    private boolean orphaned = true;

    public TileEntityTransmitter(IBlockProvider blockProvider) {
        super(((IHasTileEntity<? extends TileEntityTransmitter<?, ?, ?>>) blockProvider.getBlock()).getTileType());
        acceptorCache = new AcceptorCache<>(this);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.ALLOY_INTERACTION_CAPABILITY, this));
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
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
        if (theNetwork == network) {
            return;
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
        } else {
            requestsUpdate();
        }
    }

    public boolean hasTransmitterNetwork() {
        return !isOrphan() && getTransmitterNetwork() != null;
    }

    public abstract NETWORK createEmptyNetwork();

    public abstract NETWORK createEmptyNetworkWithID(UUID networkID);

    public abstract NETWORK createNetworkByMerging(Collection<NETWORK> toMerge);

    public NETWORK getExternalNetwork(BlockPos from) {
        TileEntityTransmitter<?, NETWORK, ?> transmitter = MekanismUtils.getTileEntity(TileEntityTransmitter.class, world, from);
        if (transmitter != null && getTransmissionType().checkTransmissionType(transmitter)) {
            return transmitter.getTransmitterNetwork();
        }
        return null;
    }

    public boolean isValid() {
        return !isRemoved();
    }


    public boolean isOrphan() {
        return orphaned;
    }

    public void setOrphan(boolean nowOrphaned) {
        orphaned = nowOrphaned;
    }

    public abstract TransmitterType getTransmitterType();

    /**
     * Get the transmitter's transmission type
     *
     * @return TransmissionType this transmitter uses
     */
    public TransmissionType getTransmissionType() {
        return getTransmitterType().getTransmission();
    }

    @Nonnull
    public LazyOptional<ACCEPTOR> getAcceptor(Direction side) {
        return acceptorCache.getCachedAcceptor(side);
    }

    @Override
    public void tick() {
        if (!isRemote() && forceUpdate) {
            refreshConnections();
            forceUpdate = false;
        }
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
            TileEntity tile = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
            if (canConnectMutual(side, tile) && tile instanceof TileEntityTransmitter) {
                TileEntityTransmitter<?, ?, ?> transmitter = (TileEntityTransmitter<?, ?, ?>) tile;
                if (getTransmitterType().getTransmission().checkTransmissionType(transmitter) && isValidTransmitter(transmitter)) {
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
        TileEntity tile = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
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
        TileEntity tile = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
        if (canConnectMutual(side, tile) && tile instanceof TileEntityTransmitter) {
            TileEntityTransmitter<?, ?, ?> transmitter = (TileEntityTransmitter<?, ?, ?>) tile;
            return getTransmitterType().getTransmission().checkTransmissionType(transmitter) && isValidTransmitter(transmitter);
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
            BlockPos offset = getPos().offset(side);
            TileEntity tile = MekanismUtils.getTileEntity(getWorld(), offset);
            if (canConnectMutual(side, tile)) {
                if (!isRemote() && !getWorld().isBlockPresent(offset)) {
                    forceUpdate = true;
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

    public boolean isValidTransmitter(TileEntityTransmitter<?, ?, ?> tile) {
        return true;
    }

    public boolean canConnectToAcceptor(Direction side) {
        ConnectionType type = connectionTypes[side.ordinal()];
        return type == ConnectionType.NORMAL || type == ConnectionType.PUSH;
    }

    @Nullable
    public BlockPos getAdjacentConnectableTransmitterPos(Direction side) {
        BlockPos sidePos = pos.offset(side);
        TileEntity potentialTransmitterTile = MekanismUtils.getTileEntity(world, sidePos);
        if (canConnectMutual(side, potentialTransmitterTile) && potentialTransmitterTile instanceof TileEntityTransmitter) {
            TileEntityTransmitter<?, ?, ?> transmitter = (TileEntityTransmitter<?, ?, ?>) potentialTransmitterTile;
            if (getTransmissionType().checkTransmissionType(transmitter) && isValidTransmitter(transmitter)) {
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
        return !(tile instanceof TileEntityTransmitter) || !getTransmissionType().checkTransmissionType(((TileEntityTransmitter<?, ?, ?>) tile));
    }

    public boolean canConnectMutual(Direction side, @Nullable TileEntity cachedTile) {
        if (!canConnect(side)) {
            return false;
        }
        if (cachedTile == null) {
            //If we don't already have the tile that is on the side calculated, do so
            cachedTile = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
        }
        return !(cachedTile instanceof TileEntityTransmitter) || ((TileEntityTransmitter<?, ?, ?>) cachedTile).canConnect(side.getOpposite());
    }

    public boolean canConnect(Direction side) {
        if (connectionTypes[side.ordinal()] == ConnectionType.NONE) {
            return false;
        }
        if (handlesRedstone()) {
            if (!redstoneSet) {
                if (redstoneReactive) {
                    redstonePowered = MekanismUtils.isGettingPowered(getWorld(), getPos());
                } else {
                    redstonePowered = false;
                }
                redstoneSet = true;
            }
            return !redstoneReactive || !redstonePowered;
        }
        return true;
    }

    protected boolean canUpgrade(AlloyTier tier) {
        return false;
    }

    @Override
    public void onAlloyInteraction(PlayerEntity player, Hand hand, ItemStack stack, @Nonnull AlloyTier tier) {
        if (getWorld() != null && hasTransmitterNetwork()) {
            NETWORK transmitterNetwork = getTransmitterNetwork();
            List<TRANSMITTER> list = new ArrayList<>(transmitterNetwork.getTransmitters());
            list.sort((o1, o2) -> {
                if (o1 != null && o2 != null) {
                    return Double.compare(o1.getPos().distanceSq(pos), o2.getPos().distanceSq(pos));
                }
                return 0;
            });
            int upgraded = 0;
            for (TRANSMITTER transmitter : list) {
                if (transmitter.canUpgrade(tier)) {
                    BlockState state = transmitter.getBlockState();
                    BlockState upgradeState = transmitter.upgradeResult(state, tier.getBaseTier());
                    if (state == upgradeState) {
                        //Skip if it would not actually upgrade anything
                        continue;
                    }
                    transmitter.takeShare();
                    transmitter.setTransmitterNetwork(null);
                    TransmitterUpgradeData upgradeData = transmitter.getUpgradeData();
                    if (upgradeData == null) {
                        Mekanism.logger.warn("Got no upgrade data for transmitter at position: {} in {} but it said it would be able to provide some.",
                              transmitter.getPos(), transmitter.getWorld());
                    } else {
                        transmitter.getWorld().setBlockState(transmitter.getPos(), upgradeState);
                        TileEntityTransmitter<?, ?, ?> upgradedTile = MekanismUtils.getTileEntity(TileEntityTransmitter.class, transmitter.getWorld(), transmitter.getPos());
                        if (upgradedTile == null) {
                            Mekanism.logger.warn("Error upgrading transmitter at position: {} in {}.", transmitter.getPos(), transmitter.getWorld());
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

    @Nonnull
    protected BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
        return current;
    }

    @Nullable
    protected TransmitterUpgradeData getUpgradeData() {
        return null;
    }

    protected void parseUpgradeData(@Nonnull TransmitterUpgradeData upgradeData) {
        Mekanism.logger.warn("Unhandled upgrade data.", new IllegalStateException());
    }

    /**
     * Only call on the server
     */
    public void requestsUpdate() {
        sendUpdatePacket();
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.putByte(NBTConstants.CURRENT_CONNECTIONS, currentTransmitterConnections);
        updateTag.putByte(NBTConstants.CURRENT_ACCEPTORS, acceptorCache.currentAcceptorConnections);
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            updateTag.putInt(NBTConstants.SIDE + i, connectionTypes[i].ordinal());
        }
        //Transmitter
        if (hasTransmitterNetwork()) {
            updateTag.putUniqueId(NBTConstants.NETWORK, getTransmitterNetwork().getUUID());
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setByteIfPresent(tag, NBTConstants.CURRENT_CONNECTIONS, connections -> currentTransmitterConnections = connections);
        NBTUtils.setByteIfPresent(tag, NBTConstants.CURRENT_ACCEPTORS, acceptors -> acceptorCache.currentAcceptorConnections = acceptors);
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            int index = i;
            NBTUtils.setEnumIfPresent(tag, NBTConstants.SIDE + index, ConnectionType::byIndexStatic, type -> connectionTypes[index] = type);
        }
        //Transmitter
        if (tag.hasUniqueId(NBTConstants.NETWORK)) {
            UUID networkID = tag.getUniqueId(NBTConstants.NETWORK);
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
        } else {
            setTransmitterNetwork(null);
        }
    }

    protected void updateClientNetwork(@Nonnull NETWORK network) {
        network.register();
        setTransmitterNetwork(network);
    }

    protected void handleContentsUpdateTag(@Nonnull NETWORK network, @Nonnull CompoundNBT tag) {
    }

    @Override
    public void handleUpdatePacket(@Nonnull CompoundNBT tag) {
        super.handleUpdatePacket(tag);
        //Delay requesting the model data update and actually updating the packet until we have finished parsing the update tag
        requestModelDataUpdate();
        MekanismUtils.updateBlock(getWorld(), getPos());
    }

    @Override
    public void read(@Nonnull CompoundNBT nbtTags) {
        super.read(nbtTags);
        redstoneReactive = nbtTags.getBoolean(NBTConstants.REDSTONE);
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            int index = i;
            NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.CONNECTION + index, ConnectionType::byIndexStatic, color -> connectionTypes[index] = color);
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.REDSTONE, redstoneReactive);
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            nbtTags.putInt(NBTConstants.CONNECTION + i, connectionTypes[i].ordinal());
        }
        return nbtTags;
    }

    private void recheckRedstone() {
        if (handlesRedstone()) {
            boolean previouslyPowered = redstonePowered;
            if (redstoneReactive) {
                redstonePowered = MekanismUtils.isGettingPowered(getWorld(), getPos());
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
                sendUpdatePacket();
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
                sendUpdatePacket();
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
            //If we don't have a transmitter network then recheck connection status both ways
            //TODO: Update this comment to flow better between the old one and the new one
            //If our connectivity changed on a side and it is also a sided pipe, inform it to recheck its connections
            //This fixes pipes not reconnecting cross chunk
            for (Direction side : EnumUtils.DIRECTIONS) {
                if (connectionMapContainsSide(newlyEnabledTransmitters, side)) {
                    TileEntityTransmitter<?, ?, ?> tile = MekanismUtils.getTileEntity(TileEntityTransmitter.class, getWorld(), getPos().offset(side));
                    if (tile != null) {
                        tile.refreshConnections(side.getOpposite());
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

    protected void onModeChange(Direction side) {
        markDirtyAcceptor(side);
        if (getPossibleTransmitterConnections() != currentTransmitterConnections) {
            markDirtyTransmitters();
        }
        markDirty(false);
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
            TransmitterNetworkRegistry.invalidateTransmitter(this);
        }
    }

    public void markDirtyAcceptor(Direction side) {
        if (hasTransmitterNetwork()) {
            getTransmitterNetwork().acceptorChanged(getTransmitter(), side);
        }
    }

    @Override
    public void validate() {
        onWorldJoin();
        super.validate();
    }

    @Override
    public void onChunkUnloaded() {
        if (!isRemote()) {
            takeShare();
        }
        onWorldSeparate();
        super.onChunkUnloaded();
    }

    @Override
    public void remove() {
        onWorldSeparate();
        super.remove();
        //Clear our cached listeners
        acceptorCache.clear();
    }

    @Override
    public void onLoad() {
        onWorldJoin();
        super.onLoad();
    }

    public void onAdded() {
        onWorldJoin();
        refreshConnections();
    }

    public void onWorldJoin() {
        if (!isRemote()) {
            TransmitterNetworkRegistry.registerOrphanTransmitter(this);
        }
    }

    public void onWorldSeparate() {
        if (isRemote()) {
            setTransmitterNetwork(null);
        } else {
            TransmitterNetworkRegistry.invalidateTransmitter(this);
        }
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

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        if (!isRemote()) {
            Pair<Vec3d, Vec3d> vecs = MultipartUtils.getRayTraceVectors(player);
            AdvancedRayTraceResult result = MultipartUtils.collisionRayTrace(getPos(), vecs.getLeft(), vecs.getRight(), getCollisionBoxes());
            if (result == null) {
                return ActionResultType.PASS;
            }
            List<Direction> list = new ArrayList<>();
            byte connections = getAllCurrentConnections();
            for (Direction dir : EnumUtils.DIRECTIONS) {
                if (connectionMapContainsSide(connections, dir)) {
                    list.add(dir);
                }
            }
            Direction hitSide;
            int boxIndex = result.hit.subHit + 1;
            if (boxIndex < list.size()) {
                hitSide = list.get(boxIndex);
            } else {
                if (connectionTypes[side.ordinal()] != ConnectionType.NONE && onConfigure(player, side) == ActionResultType.SUCCESS) {
                    //Refresh/notify so that we actually update the block and how it can connect given color or things might have changed
                    refreshConnections();
                    notifyTileChange();
                    return ActionResultType.SUCCESS;
                }
                hitSide = side;
            }
            connectionTypes[hitSide.ordinal()] = connectionTypes[hitSide.ordinal()].getNext();
            onModeChange(Direction.byIndex(hitSide.ordinal()));

            refreshConnections();
            notifyTileChange();
            player.sendMessage(MekanismLang.CONNECTION_TYPE.translate(connectionTypes[hitSide.ordinal()]));
            sendUpdatePacket();
        }
        return ActionResultType.SUCCESS;
    }

    protected ActionResultType onConfigure(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        if (handlesRedstone()) {
            redstoneReactive ^= true;
            refreshConnections();
            notifyTileChange();
            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                  MekanismLang.REDSTONE_SENSITIVITY.translateColored(EnumColor.GRAY, EnumColor.INDIGO, OnOff.of(redstoneReactive))));
        }
        return ActionResultType.SUCCESS;
    }

    public List<VoxelShape> getCollisionBoxes() {
        List<VoxelShape> list = new ArrayList<>();
        byte connections = getAllCurrentConnections();
        boolean isSmall = getTransmitterType().getSize() == Size.SMALL;
        for (Direction side : EnumUtils.DIRECTIONS) {
            ConnectionType connectionType = getConnectionType(side, connections, currentTransmitterConnections, connectionTypes);
            if (connectionType != ConnectionType.NONE) {
                if (isSmall) {
                    list.add(BlockSmallTransmitter.getSideForType(connectionType, side));
                } else {
                    list.add(BlockLargeTransmitter.getSideForType(connectionType, side));
                }
            }
        }
        //Center position
        list.add(isSmall ? BlockSmallTransmitter.center : BlockLargeTransmitter.center);
        return list;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        //If any of the block is in view, then allow rendering the contents
        return new AxisAlignedBB(pos, pos.add(1, 1, 1));
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        TransmitterModelData data = initModelData();
        updateModelData(data);
        return new ModelDataMap.Builder().withInitial(TRANSMITTER_PROPERTY, data).build();
    }

    protected void updateModelData(TransmitterModelData modelData) {
        //Update the data, using information about if there is actually a connection on a given side
        for (Direction side : EnumUtils.DIRECTIONS) {
            modelData.setConnectionData(side, getConnectionType(side));
        }
    }

    @Nonnull
    protected TransmitterModelData initModelData() {
        return new TransmitterModelData();
    }

    public void notifyTileChange() {
        MekanismUtils.notifyLoadedNeighborsOfTileChange(getWorld(), getPos());
    }

    public abstract void takeShare();

    public int getTransmitterNetworkSize() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().transmittersSize() : 0;
    }

    public int getTransmitterNetworkAcceptorSize() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().getAcceptorCount() : 0;
    }

    public ITextComponent getTransmitterNetworkNeeded() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getNeededInfo();
        }
        return MekanismLang.NO_NETWORK.translate();
    }

    public ITextComponent getTransmitterNetworkFlow() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getFlowInfo();
        }
        return MekanismLang.NO_NETWORK.translate();
    }

    public ITextComponent getTransmitterNetworkBuffer() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getStoredInfo();
        }
        return MekanismLang.NO_NETWORK.translate();
    }
}