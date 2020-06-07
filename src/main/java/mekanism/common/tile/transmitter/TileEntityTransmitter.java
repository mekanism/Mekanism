package mekanism.common.tile.transmitter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IAlloyInteraction;
import mekanism.api.IConfigurable;
import mekanism.api.NBTConstants;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.math.FloatingLong;
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
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.IBlockableConnection;
import mekanism.common.lib.transmitter.IGridTransmitter;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.CapabilityUtils;
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
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import org.apache.commons.lang3.tuple.Pair;

//TODO - V10: Re-order various methods that are in this class
public abstract class TileEntityTransmitter<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, BUFFER>, BUFFER> extends CapabilityTileEntity implements
      IBlockableConnection, IConfigurable, ITickableTileEntity, IAlloyInteraction, IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> {

    public static final ModelProperty<TransmitterModelData> TRANSMITTER_PROPERTY = new ModelProperty<>();

    public static boolean connectionMapContainsSide(byte connections, Direction side) {
        byte tester = (byte) (1 << side.ordinal());
        return (connections & tester) > 0;
    }

    public static byte setConnectionBit(byte connections, boolean toSet, Direction side) {
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

    private final Map<Direction, NonNullConsumer<LazyOptional<?>>> cachedListeners = new EnumMap<>(Direction.class);
    public byte currentAcceptorConnections = 0x00;
    public byte currentTransmitterConnections = 0x00;

    private boolean redstonePowered = false;

    protected boolean redstoneReactive = false;

    public boolean forceUpdate = true;

    private boolean redstoneSet = false;

    public ConnectionType[] connectionTypes = {ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL,
                                               ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL};
    public TileEntity[] cachedAcceptors = new TileEntity[6];

    public NETWORK theNetwork = null;
    public boolean orphaned = true;

    public TileEntityTransmitter(IBlockProvider blockProvider) {
        super(((IHasTileEntity<? extends TileEntityTransmitter<?, ?, ?>>) blockProvider.getBlock()).getTileType());
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.ALLOY_INTERACTION_CAPABILITY, this));
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
    }

    @Override
    public World world() {
        return getWorld();
    }

    @Override
    public Coord4D coord() {
        return Coord4D.get(this);
    }

    @Override
    public NETWORK getTransmitterNetwork() {
        return theNetwork;
    }

    @Override
    public void setTransmitterNetwork(NETWORK network) {
        if (theNetwork == network) {
            return;
        }
        if (isRemote() && theNetwork != null) {
            theNetwork.removeTransmitter(this);
        }
        theNetwork = network;
        orphaned = theNetwork == null;
        if (isRemote()) {
            if (theNetwork != null) {
                theNetwork.addTransmitter(this);
            }
        } else {
            this.requestsUpdate();
        }
    }

    protected boolean canHaveIncompatibleNetworks() {
        return false;
    }

    @Override
    public boolean isOrphan() {
        return orphaned;
    }

    @Override
    public void setOrphan(boolean nowOrphaned) {
        orphaned = nowOrphaned;
    }

    @Override
    public boolean hasTransmitterNetwork() {
        return !isOrphan() && getTransmitterNetwork() != null;
    }

    @Override
    public int getTransmitterNetworkSize() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().transmittersSize() : 0;
    }

    @Override
    public int getTransmitterNetworkAcceptorSize() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().getAcceptorSize() : 0;
    }

    @Override
    public ITextComponent getTransmitterNetworkNeeded() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getNeededInfo();
        }
        return MekanismLang.NO_NETWORK.translate();
    }

    @Override
    public ITextComponent getTransmitterNetworkFlow() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getFlowInfo();
        }
        return MekanismLang.NO_NETWORK.translate();
    }

    @Override
    public ITextComponent getTransmitterNetworkBuffer() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getStoredInfo();
        }
        return MekanismLang.NO_NETWORK.translate();
    }

    @Override
    public long getTransmitterNetworkCapacity() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().getCapacity() : getCapacity();
    }

    @Nonnull
    @Override
    public FloatingLong getCapacityAsFloatingLong() {
        //Note: If you plan on actually using this, override it in your tile
        return FloatingLong.create(getCapacity());
    }

    /**
     * @return True if the buffer with fallback is null (or empty)
     */
    public boolean noBufferOrFallback() {
        return getBufferWithFallback() == null;
    }

    @Nullable
    @Override
    public BUFFER getBufferWithFallback() {
        BUFFER buffer = getShare();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer == null && hasTransmitterNetwork()) {
            return getTransmitterNetwork().getBuffer();
        }
        return buffer;
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

    public byte getPossibleTransmitterConnections() {
        byte connections = 0x00;
        if (handlesRedstone() && redstoneReactive && redstonePowered) {
            return connections;
        }
        for (Direction side : EnumUtils.DIRECTIONS) {
            TileEntity tile = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
            if (canConnectMutual(side, tile) && tile instanceof TileEntityTransmitter &&
                getTransmitterType().getTransmission().checkTransmissionType((TileEntityTransmitter<?, ?, ?>) tile) && isValidTransmitter(tile)) {
                connections |= 1 << side.ordinal();
            }
        }
        return connections;
    }

    public boolean getPossibleAcceptorConnection(Direction side) {
        if (handlesRedstone() && redstoneReactive && redstonePowered) {
            return false;
        }
        TileEntity tile = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
        if (canConnectMutual(side, tile) && isValidAcceptor(tile, side)) {
            if (cachedAcceptors[side.ordinal()] != tile) {
                cachedAcceptors[side.ordinal()] = tile;
                markDirtyAcceptor(side);
            }
            return true;
        }
        if (cachedAcceptors[side.ordinal()] != null) {
            cachedAcceptors[side.ordinal()] = null;
            markDirtyAcceptor(side);
        }
        return false;
    }

    public boolean getPossibleTransmitterConnection(Direction side) {
        if (handlesRedstone() && redstoneReactive && redstonePowered) {
            return false;
        }
        TileEntity tile = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
        if (canConnectMutual(side, tile) && tile instanceof TileEntityTransmitter) {
            return getTransmitterType().getTransmission().checkTransmissionType((TileEntityTransmitter<?, ?, ?>) tile) && isValidTransmitter(tile);
        }
        return false;
    }

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
                    if (cachedAcceptors[side.ordinal()] != tile) {
                        cachedAcceptors[side.ordinal()] = tile;
                        markDirtyAcceptor(side);
                    }
                    connections |= 1 << side.ordinal();
                    continue;
                }
            }
            if (cachedAcceptors[side.ordinal()] != null) {
                cachedAcceptors[side.ordinal()] = null;
                markDirtyAcceptor(side);
            }
        }
        return connections;
    }

    public byte getAllCurrentConnections() {
        return (byte) (currentTransmitterConnections | currentAcceptorConnections);
    }

    public boolean isValidTransmitter(TileEntity tile) {
        if (tile instanceof TileEntityTransmitter && canHaveIncompatibleNetworks()) {
            TileEntityTransmitter<?, ?, ?> other = (TileEntityTransmitter<?, ?, ?>) tile;
            if (other.canHaveIncompatibleNetworks()) {
                //If it is a transmitter, only allow declare it as valid, if we don't have a combination
                // of a transmitter with a network and an orphaned transmitter, but only bother if
                // we can have incompatible networks
                if (hasTransmitterNetwork() && other.isOrphan()) {
                    return false;
                } else if (other.hasTransmitterNetwork() && isOrphan()) {
                    return false;
                }
            }
        }
        return true;
    }

    protected TileEntity getCachedTile(Direction side) {
        ConnectionType type = connectionTypes[side.ordinal()];
        if (type == ConnectionType.PULL || type == ConnectionType.NONE) {
            return null;
        }
        return connectionMapContainsSide(currentAcceptorConnections, side) ? cachedAcceptors[side.ordinal()] : null;
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

    @Override
    public Coord4D getAdjacentConnectableTransmitterCoord(Direction side) {
        Coord4D sideCoord = coord().offset(side);
        TileEntity potentialTransmitterTile = MekanismUtils.getTileEntity(world, sideCoord.getPos());
        if (!canConnectMutual(side, potentialTransmitterTile)) {
            return null;
        }
        if (potentialTransmitterTile instanceof TileEntityTransmitter &&
            getTransmissionType().checkTransmissionType((TileEntityTransmitter<?, ?, ?>) potentialTransmitterTile) && isValidTransmitter(potentialTransmitterTile)) {
            return sideCoord;
        }
        return null;
    }

    @Override
    public boolean isCompatibleWith(TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> other) {
        return isValidTransmitter(other);
    }

    @Override
    public boolean isValid() {
        //TODO - V10: Re-evaluate the second part where we check the tile in the location is actually this
        // I don't believe that is needed anymore, though if something goes wrong maybe we do need to add it back
        return !isRemoved();// && MekanismUtils.getTileEntity(world(), getPos()) == this;
    }

    @Override
    public NETWORK getExternalNetwork(Coord4D from) {
        TileEntityTransmitter<?, ?, ?> transmitter = MekanismUtils.getTileEntity(TileEntityTransmitter.class, world, from.getPos());
        if (transmitter != null && getTransmissionType().checkTransmissionType(transmitter)) {
            return ((TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER>) transmitter).getTransmitterNetwork();
        }
        return null;
    }

    public abstract TransmitterType getTransmitterType();

    public abstract boolean isValidAcceptor(TileEntity tile, Direction side);

    //TODO - V10: Rewrite this to not be as "directly" needed/be less of a "patch".
    // Ideally we will end up instead having it so that all targets are fully cached rather than
    // just registering a listener and "forgetting" about it
    protected boolean isAcceptorAndListen(TileEntity tile, Direction side, Capability<?> capability) {
        LazyOptional<?> lazyOptional = CapabilityUtils.getCapability(tile, capability, side.getOpposite());
        if (lazyOptional.isPresent()) {
            //If the capability is present
            if (!isRemote()) {
                //And we are on the server, add a listener so that once it gets invalidated we recheck that side
                // assuming that the world and position is still loaded and our tile has not been removed
                CapabilityUtils.addListener(lazyOptional, getRefreshListener(side));
            }
            return true;
        }
        return false;
    }

    /**
     * Gets the listener that will refresh connections on a given side.
     */
    protected NonNullConsumer<LazyOptional<?>> getRefreshListener(@Nonnull Direction side) {
        return cachedListeners.computeIfAbsent(side, this::getUncachedRefreshListener);
    }

    private NonNullConsumer<LazyOptional<?>> getUncachedRefreshListener(@Nonnull Direction side) {
        return ignored -> {
            if (!isRemoved() && world != null && world.isBlockPresent(pos.offset(side))) {
                refreshConnections(side);
            }
        };
    }

    @Override
    public boolean canConnectMutual(Direction side, @Nullable TileEntity cachedTile) {
        if (!canConnect(side)) {
            return false;
        }
        if (cachedTile == null) {
            //If we don't already have the tile that is on the side calculated, do so
            cachedTile = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
        }
        return !(cachedTile instanceof IBlockableConnection) || ((IBlockableConnection) cachedTile).canConnect(side.getOpposite());
    }

    @Override
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

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        //If any of the block is in view, then allow rendering the contents
        return new AxisAlignedBB(pos, pos.add(1, 1, 1));
    }

    @Override
    public void onAlloyInteraction(PlayerEntity player, Hand hand, ItemStack stack, @Nonnull AlloyTier tier) {
        if (getWorld() != null && hasTransmitterNetwork()) {
            NETWORK transmitterNetwork = getTransmitterNetwork();
            List<TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER>> list = new ArrayList<>(transmitterNetwork.getTransmitters());
            list.sort((o1, o2) -> {
                if (o1 != null && o2 != null) {
                    BlockPos o1Pos = o1.coord().getPos();
                    BlockPos o2Pos = o2.coord().getPos();
                    return Double.compare(o1Pos.distanceSq(getPos()), o2Pos.distanceSq(getPos()));
                }
                return 0;
            });
            int upgraded = 0;
            for (TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter : list) {
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
        Mekanism.logger.warn("Unhandled upgrade data.", new IllegalStateException());
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
            currentAcceptorConnections = possibleAcceptors;
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
        sendUpdatePacket();
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.putByte(NBTConstants.CURRENT_CONNECTIONS, currentTransmitterConnections);
        updateTag.putByte(NBTConstants.CURRENT_ACCEPTORS, currentAcceptorConnections);
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
        NBTUtils.setByteIfPresent(tag, NBTConstants.CURRENT_ACCEPTORS, acceptors -> currentAcceptorConnections = acceptors);
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
            TransmitterNetworkRegistry networkRegistry = TransmitterNetworkRegistry.getInstance();
            DynamicNetwork<?, ?, ?> clientNetwork = networkRegistry.getClientNetwork(networkID);
            if (clientNetwork == null) {
                NETWORK network = createEmptyNetworkWithID(networkID);
                network.register();
                setTransmitterNetwork(network);
                network.updateCapacity();
                handleContentsUpdateTag(network, tag);
            } else {
                clientNetwork.register();
                //TODO: Validate network type?
                setTransmitterNetwork((NETWORK) clientNetwork);
                clientNetwork.updateCapacity();
            }
        } else {
            setTransmitterNetwork(null);
        }
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
            currentAcceptorConnections = possibleAcceptors;
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
            currentAcceptorConnections = setConnectionBit(currentAcceptorConnections, possibleAcceptor, side);
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
     * Only call this from server side
     *
     * @param newlyEnabledTransmitters The transmitters that are now enabled and were not before.
     */
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
            //If we don't have a transmitter network then recheck connection status both ways
            //TODO: Update this comment to flow better between the old one and the new one
            //If our connectivity changed on a side and it is also a sided pipe, inform it to recheck its connections
            //This fixes pipes not reconnecting cross chunk
            for (Direction side : EnumUtils.DIRECTIONS) {
                if (connectionMapContainsSide(newlyEnabledTransmitters, side)) {
                    TileEntityTransmitter tile = MekanismUtils.getTileEntity(TileEntityTransmitter.class, getWorld(), getPos().offset(side));
                    if (tile != null) {
                        tile.refreshConnections(side.getOpposite());
                    }
                }
            }
        }
    }

    /**
     * Only call this from server side
     *
     * @param side The side that a transmitter is now enabled on after having been disabled.
     */
    protected void recheckConnection(Direction side) {
        if (canHaveIncompatibleNetworks() && hasTransmitterNetwork()) {
            //We only need to check if we can have incompatible networks and if we actually have a network
            recheckConnectionPrechecked(side);
        }
    }

    private void recheckConnectionPrechecked(Direction side) {
        TileEntityTransmitter<?, ?, ?> other = MekanismUtils.getTileEntity(TileEntityTransmitter.class, getWorld(), getPos().offset(side));
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
                    List<TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER>> otherTransmitters = new ArrayList<>(otherNetwork.getTransmitters());

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
                    for (TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> otherTransmitter : otherTransmitters) {
                        otherTransmitter.requestsUpdate();
                    }
                }
            }
        }
    }

    protected void onModeChange(Direction side) {
        markDirtyAcceptor(side);
        if (getPossibleTransmitterConnections() != currentTransmitterConnections) {
            markDirtyTransmitters();
        }
        markDirty(false);
    }

    protected void markDirtyTransmitters() {
        notifyTileChange();
        if (hasTransmitterNetwork()) {
            TransmitterNetworkRegistry.invalidateTransmitter(this);
        }
    }

    protected void markDirtyAcceptor(Direction side) {
        if (hasTransmitterNetwork()) {
            getTransmitterNetwork().acceptorChanged(this, side);
        }
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

    @Override
    public void remove() {
        onWorldSeparate();
        super.remove();
        //Clear our cached listeners
        cachedListeners.clear();
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

    public void onAdded() {
        onWorldJoin();
        refreshConnections();
    }

    @Override
    public void onLoad() {
        onWorldJoin();
        super.onLoad();
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
            Direction hitSide = sideHit(result.hit.subHit + 1);
            if (hitSide == null) {
                if (connectionTypes[side.ordinal()] != ConnectionType.NONE && onConfigure(player, 6, side) == ActionResultType.SUCCESS) {
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

    protected Direction sideHit(int boxIndex) {
        List<Direction> list = new ArrayList<>();
        for (Direction side : EnumUtils.DIRECTIONS) {
            byte connections = getAllCurrentConnections();
            if (connectionMapContainsSide(connections, side)) {
                list.add(side);
            }
        }
        if (boxIndex < list.size()) {
            return list.get(boxIndex);
        }
        return null;
    }

    protected ActionResultType onConfigure(PlayerEntity player, int part, Direction side) {
        return ActionResultType.PASS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        if (!isRemote() && handlesRedstone()) {
            redstoneReactive ^= true;
            refreshConnections();
            notifyTileChange();
            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                  MekanismLang.REDSTONE_SENSITIVITY.translateColored(EnumColor.GRAY, EnumColor.INDIGO, OnOff.of(redstoneReactive))));
        }
        return ActionResultType.SUCCESS;
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
}