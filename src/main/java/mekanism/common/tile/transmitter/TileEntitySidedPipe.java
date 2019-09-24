package mekanism.common.tile.transmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.transmitters.IBlockableConnection;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.base.ITileNetwork;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.states.TransmitterType.Size;
import mekanism.common.block.transmitter.BlockLargeTransmitter;
import mekanism.common.block.transmitter.BlockSmallTransmitter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tier.BaseTier;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.MultipartUtils.AdvancedRayTraceResult;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Pair;

public abstract class TileEntitySidedPipe extends TileEntity implements ITileNetwork, IBlockableConnection, IConfigurable, ITransmitter, ITickableTileEntity {

    public int delayTicks;

    public byte currentAcceptorConnections = 0x00;
    public byte currentTransmitterConnections = 0x00;

    public boolean sendDesc = false;
    private boolean redstonePowered = false;

    private boolean redstoneReactive = false;

    public boolean forceUpdate = true;

    private boolean redstoneSet = false;

    public ConnectionType[] connectionTypes = {ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL,
                                               ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL};
    public TileEntity[] cachedAcceptors = new TileEntity[6];

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

    @Override
    public void tick() {
        if (getWorld().isRemote) {
            if (delayTicks == 5) {
                delayTicks = 6; /* don't refresh again */
                refreshConnections();
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

    public BaseTier getBaseTier() {
        return BaseTier.BASIC;
    }

    public void setBaseTier(BaseTier baseTier) {
    }

    public boolean handlesRedstone() {
        return true;
    }

    public byte getPossibleTransmitterConnections() {
        byte connections = 0x00;
        if (handlesRedstone() && redstoneReactive && redstonePowered) {
            return connections;
        }
        for (Direction side : Direction.values()) {
            if (canConnectMutual(side)) {
                TileEntity tileEntity = MekanismUtils.getTileEntity(world, getPos().offset(side));
                if (CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite()).matches(transmitter ->
                      TransmissionType.checkTransmissionType(transmitter, getTransmitterType().getTransmission()) && isValidTransmitter(tileEntity))) {
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
        if (canConnectMutual(side)) {
            TileEntity tileEntity = MekanismUtils.getTileEntity(world, getPos().offset(side));
            if (isValidAcceptor(tileEntity, side)) {
                if (cachedAcceptors[side.ordinal()] != tileEntity) {
                    cachedAcceptors[side.ordinal()] = tileEntity;
                    markDirtyAcceptor(side);
                }
                return true;
            }
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
        if (canConnectMutual(side)) {
            TileEntity tileEntity = MekanismUtils.getTileEntity(world, getPos().offset(side));
            return CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite()).matches(
                  transmitter -> TransmissionType.checkTransmissionType(transmitter, getTransmitterType().getTransmission()) && isValidTransmitter(tileEntity)
            );
        }
        return false;
    }

    public byte getPossibleAcceptorConnections() {
        byte connections = 0x00;

        if (handlesRedstone() && redstoneReactive && redstonePowered) {
            return connections;
        }

        for (Direction side : Direction.values()) {
            if (canConnectMutual(side)) {
                Coord4D coord = new Coord4D(getPos(), getWorld()).offset(side);
                if (!getWorld().isRemote && !coord.exists(getWorld())) {
                    forceUpdate = true;
                    continue;
                }

                TileEntity tileEntity = coord.getTileEntity(getWorld());
                if (isValidAcceptor(tileEntity, side)) {
                    if (cachedAcceptors[side.ordinal()] != tileEntity) {
                        cachedAcceptors[side.ordinal()] = tileEntity;
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

    public boolean isValidTransmitter(TileEntity tileEntity) {
        return true;
    }

    public List<AxisAlignedBB> getCollisionBoxes() {
        List<AxisAlignedBB> list = new ArrayList<>();
        for (Direction side : Direction.values()) {
            int ord = side.ordinal();
            byte connections = getAllCurrentConnections();
            if (connectionMapContainsSide(connections, side)) {
                list.add(getTransmitterType().getSize() == Size.SMALL ? BlockSmallTransmitter.smallSides[ord] : BlockLargeTransmitter.largeSides[ord]);
            }
        }
        list.add(getTransmitterType().getSize() == Size.SMALL ? BlockSmallTransmitter.smallSides[6] : BlockLargeTransmitter.largeSides[6]);
        return list;
    }

    public abstract TransmitterType getTransmitterType();

    public List<AxisAlignedBB> getCollisionBoxes(AxisAlignedBB entityBox) {
        List<AxisAlignedBB> list = new ArrayList<>();
        for (Direction side : Direction.values()) {
            int ord = side.ordinal();
            byte connections = getAllCurrentConnections();
            if (connectionMapContainsSide(connections, side)) {
                AxisAlignedBB box = getTransmitterType().getSize() == Size.SMALL ? BlockSmallTransmitter.smallSides[ord] : BlockLargeTransmitter.largeSides[ord];
                if (box.intersects(entityBox)) {
                    list.add(box);
                }
            }
        }
        AxisAlignedBB box = getTransmitterType().getSize() == Size.SMALL ? BlockSmallTransmitter.smallSides[6] : BlockLargeTransmitter.largeSides[6];
        if (box.intersects(entityBox)) {
            list.add(box);
        }
        return list;
    }

    public abstract boolean isValidAcceptor(TileEntity tile, Direction side);

    @Override
    public boolean canConnectMutual(Direction side) {
        if (!canConnect(side)) {
            return false;
        }
        BlockPos testPos = getPos().offset(side);
        LazyOptionalHelper<IBlockableConnection> blockableConnection = CapabilityUtils.getCapabilityHelper(MekanismUtils.getTileEntity(world, testPos),
              Capabilities.BLOCKABLE_CONNECTION_CAPABILITY, side.getOpposite());
        return !blockableConnection.isPresent() || blockableConnection.matches(connection -> connection.canConnect(side.getOpposite()));
    }

    @Override
    public boolean canConnect(Direction side) {
        if (connectionTypes[side.ordinal()] == ConnectionType.NONE) {
            return false;
        }
        if (handlesRedstone()) {
            if (!redstoneSet) {
                if (redstoneReactive) {
                    redstonePowered = MekanismUtils.isGettingPowered(getWorld(), new Coord4D(getPos(), getWorld()));
                } else {
                    redstonePowered = false;
                }
                redstoneSet = true;
            }
            if (redstoneReactive && redstonePowered) {
                return false;
            }
        }
        //TODO: Multipart
        /*if (Mekanism.hooks.MCMPLoaded) {
            return MultipartMekanism.hasConnectionWith(this, side);
        }*/
        return true;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) throws Exception {
        if (world.isRemote) {
            currentTransmitterConnections = dataStream.readByte();
            currentAcceptorConnections = dataStream.readByte();
            for (int i = 0; i < 6; i++) {
                connectionTypes[i] = dataStream.readEnumValue(ConnectionType.class);
            }
            markDirty();
            MekanismUtils.updateBlock(world, pos);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        //TODO: Multipart
        /*if (Mekanism.hooks.MCMPLoaded) {
            MultipartTileNetworkJoiner.addMultipartHeader(this, data, null);
        }*/
        data.add(currentTransmitterConnections);
        data.add(currentAcceptorConnections);
        for (int i = 0; i < 6; i++) {
            data.add(connectionTypes[i]);
        }
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        redstoneReactive = nbtTags.getBoolean("redstoneReactive");
        for (int i = 0; i < 6; i++) {
            connectionTypes[i] = ConnectionType.values()[nbtTags.getInt("connection" + i)];
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean("redstoneReactive", redstoneReactive);
        for (int i = 0; i < 6; i++) {
            nbtTags.putInt("connection" + i, connectionTypes[i].ordinal());
        }
        return nbtTags;
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbtTags = super.getUpdateTag();
        nbtTags.putInt("tier", getBaseTier().ordinal());
        return nbtTags;
    }

    protected void onRefresh() {
    }

    public void refreshConnections() {
        if (handlesRedstone()) {
            boolean previouslyPowered = redstonePowered;
            if (redstoneReactive) {
                redstonePowered = MekanismUtils.isGettingPowered(getWorld(), new Coord4D(getPos(), getWorld()));
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

        if (!getWorld().isRemote) {
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
        if (!getWorld().isRemote) {
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
        for (Direction side : Direction.values()) {
            if (connectionMapContainsSide(newlyEnabledTransmitters, side)) {
                TileEntity tileEntity = MekanismUtils.getTileEntity(world, getPos().offset(side));
                if (tileEntity instanceof TileEntitySidedPipe) {
                    ((TileEntitySidedPipe) tileEntity).refreshConnections();
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
        if (getPossibleTransmitterConnections() != currentTransmitterConnections) {
            //Mark the transmitters as invalidated if they do not match what we have stored/calculated
            refreshConnections();
        }
        super.onLoad();
    }

    public void onNeighborTileChange(Direction side) {
        refreshConnections(side);
    }

    public void onNeighborBlockChange(Direction side) {
        refreshConnections();
    }

    //TODO: Multipart
    /*public void onPartChanged(IMultipart part) {
        byte transmittersBefore = currentTransmitterConnections;
        refreshConnections();
        if (transmittersBefore != currentTransmitterConnections) {
            markDirtyTransmitters();
        }
    }*/

    public ConnectionType getConnectionType(Direction side) {
        return getConnectionType(side, getAllCurrentConnections(), currentTransmitterConnections, connectionTypes);
    }

    public List<Direction> getConnections(ConnectionType type) {
        List<Direction> sides = new ArrayList<>();
        for (Direction side : Direction.values()) {
            if (getConnectionType(side) == type) {
                sides.add(side);
            }
        }
        return sides;
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        if (!getWorld().isRemote) {
            RayTraceResult hit = reTrace(getWorld(), getPos(), player);
            if (hit == null) {
                return ActionResultType.PASS;
            } else {
                Direction hitSide = sideHit(hit.subHit + 1);
                if (hitSide == null) {
                    if (connectionTypes[side.ordinal()] != ConnectionType.NONE && onConfigure(player, 6, side) == ActionResultType.SUCCESS) {
                        return ActionResultType.SUCCESS;
                    }
                    hitSide = side;
                }
                connectionTypes[hitSide.ordinal()] = connectionTypes[hitSide.ordinal()].next();
                sendDesc = true;
                onModeChange(Direction.byIndex(hitSide.ordinal()));

                refreshConnections();
                notifyTileChange();
                player.sendMessage(TextComponentUtil.build(Translation.of("tooltip.configurator.modeChange"), " ", connectionTypes[hitSide.ordinal()]));
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.SUCCESS;
    }

    private RayTraceResult reTrace(World world, BlockPos pos, PlayerEntity player) {
        Pair<Vec3d, Vec3d> vecs = MultipartUtils.getRayTraceVectors(player);
        AdvancedRayTraceResult result = MultipartUtils.collisionRayTrace(getPos(), vecs.getLeft(), vecs.getRight(), getCollisionBoxes());
        return result == null ? null : result.hit;
    }

    protected Direction sideHit(int boxIndex) {
        List<Direction> list = new ArrayList<>();
        for (Direction side : Direction.values()) {
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
        if (!getWorld().isRemote && handlesRedstone()) {
            redstoneReactive ^= true;
            refreshConnections();
            notifyTileChange();
            player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GRAY,
                  Translation.of("tooltip.configurator.redstoneSensitivity"), " ", EnumColor.INDIGO, OnOff.of(redstoneReactive), "."));
        }
        return ActionResultType.SUCCESS;
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

    public enum ConnectionType implements IStringSerializable, IHasTranslationKey {
        NORMAL,
        PUSH,
        PULL,
        NONE;

        public ConnectionType next() {
            if (ordinal() == values().length - 1) {
                return NORMAL;
            }
            return values()[ordinal() + 1];
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String getTranslationKey() {
            return "mekanism.pipe.connectiontype." + getName();
        }
    }
}