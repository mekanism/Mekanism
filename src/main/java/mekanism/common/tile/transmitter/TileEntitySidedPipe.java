package mekanism.common.tile.transmitter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigurable;
import mekanism.api.IIncrementalEnum;
import mekanism.api.NBTConstants;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.transmitters.IBlockableConnection;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.states.TransmitterType.Size;
import mekanism.common.block.transmitter.BlockLargeTransmitter;
import mekanism.common.block.transmitter.BlockSmallTransmitter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.MultipartUtils.AdvancedRayTraceResult;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Pair;

public abstract class TileEntitySidedPipe extends CapabilityTileEntity implements IBlockableConnection, IConfigurable, ITransmitter, ITickableTileEntity {

    public byte currentAcceptorConnections = 0x00;
    public byte currentTransmitterConnections = 0x00;

    private boolean redstonePowered = false;

    protected boolean redstoneReactive = false;

    public boolean forceUpdate = true;

    private boolean redstoneSet = false;

    public ConnectionType[] connectionTypes = {ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL,
                                               ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL};
    public TileEntity[] cachedAcceptors = new TileEntity[6];

    @Nullable
    private TransmitterModelData modelData;

    public TileEntitySidedPipe(TileEntityType<? extends TileEntitySidedPipe> type) {
        super(type);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.BLOCKABLE_CONNECTION_CAPABILITY, this));
    }

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
            if (canConnectMutual(side, tile)) {
                Optional<IGridTransmitter<?, ?, ?>> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite()));
                if (capability.isPresent() && TransmissionType.checkTransmissionType(capability.get(), getTransmitterType().getTransmission()) && isValidTransmitter(tile)) {
                    connections |= 1 << side.ordinal();
                }
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
        if (canConnectMutual(side, tile)) {
            Optional<IGridTransmitter<?, ?, ?>> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite()));
            if (capability.isPresent()) {
                return TransmissionType.checkTransmissionType(capability.get(), getTransmitterType().getTransmission()) && isValidTransmitter(tile);
            }
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

    public abstract boolean isValidTransmitter(TileEntity tile);

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
                BlockPos pos = tile.getPos();
                lazyOptional.addListener(invalidated -> {
                    if (!isRemoved() && world != null && world.isBlockPresent(pos)) {
                        refreshConnections(side);
                    }
                });
            }
            return true;
        }
        return false;
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
        return CapabilityUtils.getCapability(cachedTile, Capabilities.BLOCKABLE_CONNECTION_CAPABILITY, side.getOpposite())
              .map(connection -> connection.canConnect(side.getOpposite())).orElse(true);
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
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.putByte(NBTConstants.CURRENT_CONNECTIONS, currentTransmitterConnections);
        updateTag.putByte(NBTConstants.CURRENT_ACCEPTORS, currentAcceptorConnections);
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            updateTag.putInt(NBTConstants.SIDE + i, connectionTypes[i].ordinal());
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
    }

    @Override
    public void handleUpdatePacket(@Nonnull CompoundNBT tag) {
        super.handleUpdatePacket(tag);
        //Delay requesting the model data update and actually updating the packet until we have finished parsing the update tag
        requestModelDataUpdate();
        MekanismUtils.updateBlock(getWorld(), getPos());
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        redstoneReactive = nbtTags.getBoolean(NBTConstants.REDSTONE);
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            int index = i;
            NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.CONNECTION + index, ConnectionType::byIndexStatic, color -> connectionTypes[index] = color);
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
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
        //If our connectivity changed on a side and it is also a sided pipe, inform it to recheck its connections
        //This fixes pipes not reconnecting cross chunk
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (connectionMapContainsSide(newlyEnabledTransmitters, side)) {
                TileEntitySidedPipe tile = MekanismUtils.getTileEntity(TileEntitySidedPipe.class, getWorld(), getPos().offset(side));
                if (tile != null) {
                    tile.refreshConnections(side.getOpposite());
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
    }

    protected void markDirtyAcceptor(Direction side) {
    }

    public abstract void onWorldJoin();

    public abstract void onWorldSeparate();

    @Override
    public void remove() {
        onWorldSeparate();
        super.remove();
    }

    @Override
    public void validate() {
        onWorldJoin();
        super.validate();
    }

    @Override
    public void onChunkUnloaded() {
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

    public EnumColor getRenderColor() {
        return null;
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

    @Override
    public void requestModelDataUpdate() {
        //We set our model data to null so that we make sure an updated variant is called when getModelData is next called
        modelData = null;
        super.requestModelDataUpdate();
    }

    @Nonnull
    @Override
    public TransmitterModelData getModelData() {
        if (modelData == null) {
            modelData = initModelData();
            //Update the model data with side/color specific information
            updateModelData(modelData);
        }
        return modelData;
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

    @Override
    public boolean canRenderBreaking() {
        return false;
    }

    public enum ConnectionType implements IIncrementalEnum<ConnectionType>, IStringSerializable, IHasTranslationKey {
        NORMAL(MekanismLang.CONNECTION_NORMAL),
        PUSH(MekanismLang.CONNECTION_PUSH),
        PULL(MekanismLang.CONNECTION_PULL),
        NONE(MekanismLang.CONNECTION_NONE);

        private static final ConnectionType[] TYPES = values();
        private final ILangEntry langEntry;

        ConnectionType(ILangEntry langEntry) {
            this.langEntry = langEntry;
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String getTranslationKey() {
            return langEntry.getTranslationKey();
        }

        @Nonnull
        @Override
        public ConnectionType byIndex(int index) {
            return byIndexStatic(index);
        }

        public static ConnectionType byIndexStatic(int index) {
            return MathUtils.getByIndexMod(TYPES, index);
        }
    }
}