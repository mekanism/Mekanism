package mekanism.common.tile.transmitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.IIncrementalEnum;
import mekanism.api.NBTConstants;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.transmitters.IBlockableConnection;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.base.ITileNetwork;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.states.TransmitterType.Size;
import mekanism.common.block.transmitter.BlockLargeTransmitter;
import mekanism.common.block.transmitter.BlockSmallTransmitter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.MultipartUtils.AdvancedRayTraceResult;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import org.apache.commons.lang3.tuple.Pair;

public abstract class TileEntitySidedPipe extends TileEntity implements ITileNetwork, IBlockableConnection, IConfigurable, ITransmitter, ITickableTileEntity {

    public int delayTicks;

    public byte currentAcceptorConnections = 0x00;
    public byte currentTransmitterConnections = 0x00;

    public boolean sendDesc = false;
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

    public boolean isRemote() {
        //TODO: See if there is anyway to improve this so we don't have to call EffectiveSide.get
        return getWorld() == null ? EffectiveSide.get().isClient() : getWorld().isRemote();
    }

    @Override
    public void markDirty() {
        //Copy of the base impl of markDirty in TileEntity, except as none of our transmitters supports comparators
        // don't bother doing notifying neighbors
        if (world != null) {
            cachedBlockState = world.getBlockState(pos);
            world.markChunkDirty(pos, this);
            //TODO: Test if this majorly breaks things
        }
    }

    @Override
    public void tick() {
        if (isRemote()) {
            if (delayTicks == 5) {
                delayTicks = 6;
                refreshConnections();
                // refresh the model to fix weird model caching issues
                requestModelDataUpdate();
                MekanismUtils.updateBlock(getWorld(), pos);
            } else if (delayTicks < 5) {
                delayTicks++;
            }
        } else {
            if (forceUpdate) {
                refreshConnections();
                forceUpdate = false;
            }
            if (sendDesc) {
                Mekanism.packetHandler.sendUpdatePacket(this);
                sendDesc = false;
            }
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

    public boolean isValidTransmitter(TileEntity tile) {
        return true;
    }

    public List<AxisAlignedBB> getCollisionBoxes() {
        List<AxisAlignedBB> list = new ArrayList<>();
        byte connections = getAllCurrentConnections();
        AxisAlignedBB[] sides = getTransmitterType().getSize() == Size.SMALL ? BlockSmallTransmitter.smallSides : BlockLargeTransmitter.largeSides;
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (connectionMapContainsSide(connections, side)) {
                list.add(sides[side.ordinal()]);
            }
        }
        //Center position
        list.add(sides[6]);
        return list;
    }

    public abstract TransmitterType getTransmitterType();

    public abstract boolean isValidAcceptor(TileEntity tile, Direction side);

    @Override
    public boolean canConnectMutual(Direction side, @Nullable TileEntity cachedTile) {
        if (!canConnect(side)) {
            return false;
        }
        if (cachedTile == null) {
            //If we don't already have the tile that is on the side calculated, do so
            cachedTile = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
        }
        Optional<IBlockableConnection> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(cachedTile,
              Capabilities.BLOCKABLE_CONNECTION_CAPABILITY, side.getOpposite()));
        return capability.map(connection -> connection.canConnect(side.getOpposite())).orElse(true);
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

    @Override
    public void handlePacketData(PacketBuffer dataStream) throws Exception {
        if (isRemote()) {
            currentTransmitterConnections = dataStream.readByte();
            currentAcceptorConnections = dataStream.readByte();
            for (int i = 0; i < 6; i++) {
                connectionTypes[i] = dataStream.readEnumValue(ConnectionType.class);
            }
            requestModelDataUpdate();
            MekanismUtils.updateBlock(getWorld(), pos);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        data.add(currentTransmitterConnections);
        data.add(currentAcceptorConnections);
        data.addAll(Arrays.asList(connectionTypes).subList(0, 6));
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        redstoneReactive = nbtTags.getBoolean(NBTConstants.REDSTONE);
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            int index = i;
            NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.CONNECTION + index, ConnectionType::byIndexStatic, color -> connectionTypes[index] = color);
        }
        //TODO: Do we need to update the model here
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

    protected void onRefresh() {
    }

    public void refreshConnections() {
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

        if (!isRemote()) {
            byte possibleTransmitters = getPossibleTransmitterConnections();
            byte possibleAcceptors = getPossibleAcceptorConnections();
            byte newlyEnabledTransmitters = 0;

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
        }
    }

    public void refreshConnections(Direction side) {
        if (!isRemote()) {
            boolean possibleTransmitter = getPossibleTransmitterConnection(side);
            boolean possibleAcceptor = getPossibleAcceptorConnection(side);
            boolean transmitterChanged = false;

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
                    tile.refreshConnections();
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
        markDirty();
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
        //TODO: Temporarily commented out to stop crashing from starting world with pipes in it
        // For the most part is seems to be working just fine without this, except some spots still behave strangely
        /*if (getPossibleTransmitterConnections() != currentTransmitterConnections) {
            //Mark the transmitters as invalidated if they do not match what we have stored/calculated
            refreshConnections();
        }//*/
        super.onLoad();
    }

    public void onNeighborTileChange(Direction side) {
        refreshConnections(side);
    }

    public void onNeighborBlockChange(Direction side) {
        //TODO: Figure out why does this not check the side specific one
        refreshConnections();
    }

    public ConnectionType getConnectionType(Direction side) {
        return getConnectionType(side, getAllCurrentConnections(), currentTransmitterConnections, connectionTypes);
    }

    public List<Direction> getConnections(ConnectionType type) {
        List<Direction> sides = new ArrayList<>();
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
            } else {
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
                sendDesc = true;
                onModeChange(Direction.byIndex(hitSide.ordinal()));

                refreshConnections();
                notifyTileChange();
                player.sendMessage(MekanismLang.CONNECTION_TYPE.translate(connectionTypes[hitSide.ordinal()]));
                return ActionResultType.SUCCESS;
            }
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
        updateModelData();
        super.requestModelDataUpdate();
    }

    @Nonnull
    @Override
    public TransmitterModelData getModelData() {
        if (modelData == null) {
            modelData = initModelData();
        }
        return modelData;
    }

    protected void updateModelData() {
        TransmitterModelData modelData = getModelData();
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
        MekanismUtils.notifyLoadedNeighborsOfTileChange(getWorld(), new Coord4D(getPos(), getWorld()));
    }

    @Override
    public boolean canRenderBreaking() {
        return false;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.TILE_NETWORK_CAPABILITY) {
            return Capabilities.TILE_NETWORK_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.BLOCKABLE_CONNECTION_CAPABILITY) {
            return Capabilities.BLOCKABLE_CONNECTION_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
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
            //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
            return TYPES[Math.floorMod(index, TYPES.length)];
        }
    }
}