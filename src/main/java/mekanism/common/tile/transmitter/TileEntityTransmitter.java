package mekanism.common.tile.transmitter;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IAlloyInteraction;
import mekanism.api.IConfigurable;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.states.TransmitterType.Size;
import mekanism.common.block.transmitter.BlockLargeTransmitter;
import mekanism.common.block.transmitter.BlockSmallTransmitter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.content.network.transmitter.BufferedTransmitter;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.MultipartUtils.AdvancedRayTraceResult;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.apache.commons.lang3.tuple.Pair;

public abstract class TileEntityTransmitter extends CapabilityTileEntity implements IConfigurable, ITickableTileEntity, IAlloyInteraction {

    public static final ModelProperty<TransmitterModelData> TRANSMITTER_PROPERTY = new ModelProperty<>();

    private final Transmitter<?, ?, ?> transmitter;
    private boolean forceUpdate = true;
    private boolean loaded = false;

    public TileEntityTransmitter(IBlockProvider blockProvider) {
        super(((IHasTileEntity<? extends TileEntityTransmitter>) blockProvider.getBlock()).getTileType());
        this.transmitter = createTransmitter(blockProvider);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.ALLOY_INTERACTION_CAPABILITY, this));
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
    }

    protected abstract Transmitter<?, ?, ?> createTransmitter(IBlockProvider blockProvider);

    public Transmitter<?, ?, ?> getTransmitter() {
        return transmitter;
    }

    public void setForceUpdate() {
        forceUpdate = true;
    }

    public abstract TransmitterType getTransmitterType();

    @Override
    public void tick() {
        if (!isRemote() && forceUpdate) {
            getTransmitter().refreshConnections();
            forceUpdate = false;
        }
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        return getTransmitter().getReducedUpdateTag(super.getReducedUpdateTag());
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        getTransmitter().handleUpdateTag(tag);
    }

    @Override
    public void handleUpdatePacket(@Nonnull CompoundNBT tag) {
        super.handleUpdatePacket(tag);
        //Delay requesting the model data update and actually updating the packet until we have finished parsing the update tag
        requestModelDataUpdate();
        MekanismUtils.updateBlock(getWorld(), getPos());
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        getTransmitter().read(nbtTags);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        return getTransmitter().write(super.write(nbtTags));
    }

    public void onNeighborTileChange(Direction side) {
        getTransmitter().onNeighborTileChange(side);
    }

    public void onNeighborBlockChange(Direction side) {
        getTransmitter().onNeighborBlockChange(side);
    }

    @Override
    public void validate() {
        super.validate();
        onWorldJoin();
    }

    @Override
    public void onChunkUnloaded() {
        if (!isRemote()) {
            getTransmitter().takeShare();
        }
        onWorldSeparate();
        super.onChunkUnloaded();
    }

    @Override
    public void remove() {
        super.remove();
        onWorldSeparate();
        getTransmitter().remove();
    }

    public void onAdded() {
        onWorldJoin();
        getTransmitter().refreshConnections();
    }

    private void onWorldJoin() {
        loaded = true;
        if (!isRemote()) {
            TransmitterNetworkRegistry.registerOrphanTransmitter(getTransmitter());
        }
    }

    private void onWorldSeparate() {
        loaded = false;
        if (isRemote()) {
            getTransmitter().setTransmitterNetwork(null);
        } else {
            TransmitterNetworkRegistry.invalidateTransmitter(getTransmitter());
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        if (!isRemote()) {
            Pair<Vector3d, Vector3d> vecs = MultipartUtils.getRayTraceVectors(player);
            AdvancedRayTraceResult result = MultipartUtils.collisionRayTrace(getPos(), vecs.getLeft(), vecs.getRight(), getCollisionBoxes());
            if (result == null) {
                return ActionResultType.PASS;
            }
            List<Direction> list = new ArrayList<>();
            byte connections = getTransmitter().getAllCurrentConnections();
            for (Direction dir : EnumUtils.DIRECTIONS) {
                if (Transmitter.connectionMapContainsSide(connections, dir)) {
                    list.add(dir);
                }
            }
            Direction hitSide;
            int boxIndex = result.hit.subHit + 1;
            if (boxIndex < list.size()) {
                hitSide = list.get(boxIndex);
            } else {
                if (transmitter.connectionTypes[side.ordinal()] != ConnectionType.NONE && onConfigure(player, side) == ActionResultType.SUCCESS) {
                    //Refresh/notify so that we actually update the block and how it can connect given color or things might have changed
                    getTransmitter().refreshConnections();
                    getTransmitter().notifyTileChange();
                    return ActionResultType.SUCCESS;
                }
                hitSide = side;
            }
            transmitter.connectionTypes[hitSide.ordinal()] = transmitter.connectionTypes[hitSide.ordinal()].getNext();
            getTransmitter().onModeChange(Direction.byIndex(hitSide.ordinal()));
            getTransmitter().refreshConnections();
            getTransmitter().notifyTileChange();
            player.sendMessage(MekanismLang.CONNECTION_TYPE.translate(transmitter.connectionTypes[hitSide.ordinal()]), Util.DUMMY_UUID);
            sendUpdatePacket();
        }
        return ActionResultType.SUCCESS;
    }

    protected ActionResultType onConfigure(PlayerEntity player, Direction side) {
        //TODO: Move some of this stuff back into the tiles?
        return getTransmitter().onConfigure(player, side);
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return getTransmitter().onRightClick(player, side);
    }

    public List<VoxelShape> getCollisionBoxes() {
        List<VoxelShape> list = new ArrayList<>();
        byte connections = getTransmitter().getAllCurrentConnections();
        boolean isSmall = getTransmitterType().getSize() == Size.SMALL;
        for (Direction side : EnumUtils.DIRECTIONS) {
            ConnectionType connectionType = Transmitter.getConnectionType(side, connections, transmitter.currentTransmitterConnections, transmitter.connectionTypes);
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
            modelData.setConnectionData(side, getTransmitter().getConnectionType(side));
        }
    }

    @Nonnull
    protected TransmitterModelData initModelData() {
        return new TransmitterModelData();
    }

    protected boolean canUpgrade(AlloyTier tier) {
        return false;
    }

    @Override
    public void onAlloyInteraction(PlayerEntity player, Hand hand, ItemStack stack, @Nonnull AlloyTier tier) {
        if (getWorld() != null && getTransmitter().hasTransmitterNetwork()) {
            DynamicNetwork<?, ?, ?> transmitterNetwork = getTransmitter().getTransmitterNetwork();
            List<Transmitter<?, ?, ?>> list = new ArrayList<>(transmitterNetwork.getTransmitters());
            list.sort((o1, o2) -> {
                if (o1 != null && o2 != null) {
                    return Double.compare(o1.getTilePos().distanceSq(pos), o2.getTilePos().distanceSq(pos));
                }
                return 0;
            });
            boolean sharesSet = false;
            int upgraded = 0;
            for (Transmitter<?, ?, ?> transmitter : list) {
                TileEntityTransmitter transmitterTile = transmitter.getTransmitterTile();
                if (transmitterTile.canUpgrade(tier)) {
                    BlockState state = transmitterTile.getBlockState();
                    BlockState upgradeState = transmitterTile.upgradeResult(state, tier.getBaseTier());
                    if (state == upgradeState) {
                        //Skip if it would not actually upgrade anything
                        continue;
                    }
                    if (!sharesSet) {
                        if (transmitterNetwork instanceof DynamicBufferedNetwork) {
                            //Ensure we save the shares to the tiles so that they can properly take them and they don't get voided
                            ((DynamicBufferedNetwork) transmitterNetwork).validateSaveShares((BufferedTransmitter<?, ?, ?, ?>) transmitter);
                        }
                        sharesSet = true;
                    }
                    transmitter.startUpgrading();
                    TransmitterUpgradeData upgradeData = transmitterTile.getUpgradeData();
                    if (upgradeData == null) {
                        Mekanism.logger.warn("Got no upgrade data for transmitter at position: {} in {} but it said it would be able to provide some.",
                              transmitter.getTilePos(), transmitter.getTileWorld());
                    } else {
                        transmitter.getTileWorld().setBlockState(transmitter.getTilePos(), upgradeState);
                        TileEntityTransmitter upgradedTile = MekanismUtils.getTileEntity(TileEntityTransmitter.class, transmitter.getTileWorld(), transmitter.getTilePos());
                        if (upgradedTile == null) {
                            Mekanism.logger.warn("Error upgrading transmitter at position: {} in {}.", transmitter.getTilePos(), transmitter.getTileWorld());
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
}