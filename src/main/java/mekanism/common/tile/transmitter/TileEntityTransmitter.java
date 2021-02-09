package mekanism.common.tile.transmitter;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
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
import mekanism.common.capabilities.DynamicHandler.InteractPredicate;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.content.network.transmitter.BufferedTransmitter;
import mekanism.common.content.network.transmitter.IUpgradeableTransmitter;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.MultipartUtils.AdvancedRayTraceResult;
import mekanism.common.util.WorldUtils;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
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
        WorldUtils.updateBlock(getWorld(), getPos(), this);
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
        getTransmitter().onChunkUnload();
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
                if (transmitter.getConnectionTypeRaw(side) != ConnectionType.NONE && onConfigure(player, side) == ActionResultType.SUCCESS) {
                    //Refresh/notify so that we actually update the block and how it can connect given color or things might have changed
                    getTransmitter().refreshConnections();
                    getTransmitter().notifyTileChange();
                    return ActionResultType.SUCCESS;
                }
                hitSide = side;
            }
            transmitter.setConnectionTypeRaw(hitSide, transmitter.getConnectionTypeRaw(hitSide).getNext());
            //TODO - 10.1: Re-evaluate how much of this is needed because in theory we could try and get most
            // of it to be handled in the sideChanged method
            getTransmitter().onModeChange(Direction.byIndex(hitSide.ordinal()));
            getTransmitter().refreshConnections();
            getTransmitter().notifyTileChange();
            player.sendMessage(MekanismLang.CONNECTION_TYPE.translate(transmitter.getConnectionTypeRaw(hitSide)), Util.DUMMY_UUID);
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
        boolean isSmall = getTransmitterType().getSize() == Size.SMALL;
        for (Direction side : EnumUtils.DIRECTIONS) {
            ConnectionType connectionType = getTransmitter().getConnectionType(side);
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
                if (transmitter instanceof IUpgradeableTransmitter) {
                    IUpgradeableTransmitter<?> upgradeableTransmitter = (IUpgradeableTransmitter<?>) transmitter;
                    if (upgradeableTransmitter.canUpgrade(tier)) {
                        TileEntityTransmitter transmitterTile = transmitter.getTransmitterTile();
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
                        TransmitterUpgradeData upgradeData = upgradeableTransmitter.getUpgradeData();
                        BlockPos transmitterPos = transmitter.getTilePos();
                        World transmitterWorld = transmitter.getTileWorld();
                        if (upgradeData == null) {
                            Mekanism.logger.warn("Got no upgrade data for transmitter at position: {} in {} but it said it would be able to provide some.",
                                  transmitterPos, transmitterWorld);
                        } else {
                            transmitterWorld.setBlockState(transmitterPos, upgradeState);
                            TileEntityTransmitter upgradedTile = WorldUtils.getTileEntity(TileEntityTransmitter.class, transmitterWorld, transmitterPos);
                            if (upgradedTile == null) {
                                Mekanism.logger.warn("Error upgrading transmitter at position: {} in {}.", transmitterPos, transmitterWorld);
                            } else {
                                Transmitter<?, ?, ?> upgradedTransmitter = upgradedTile.getTransmitter();
                                if (upgradedTransmitter instanceof IUpgradeableTransmitter) {
                                    transferUpgradeData((IUpgradeableTransmitter<?>) upgradedTransmitter, upgradeData);
                                } else {
                                    Mekanism.logger.warn("Unhandled upgrade data.", new IllegalStateException());
                                }
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
                }
            }
        }
    }

    private <DATA extends TransmitterUpgradeData> void transferUpgradeData(IUpgradeableTransmitter<DATA> upgradeableTransmitter, TransmitterUpgradeData data) {
        if (upgradeableTransmitter.dataTypeMatches(data)) {
            upgradeableTransmitter.parseUpgradeData((DATA) data);
        } else {
            Mekanism.logger.warn("Unhandled upgrade data.", new IllegalStateException());
        }
    }

    @Nonnull
    protected BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
        return current;
    }

    public void sideChanged(@Nonnull Direction side, @Nonnull ConnectionType old, @Nonnull ConnectionType type) {
    }

    protected InteractPredicate getExtractPredicate() {
        return (tank, side) -> {
            if (side == null) {
                //Note: We return true here, but extraction isn't actually allowed and gets blocked by the read only handler
                return true;
            }
            //If we have a side only allow extracting if our connection allows it
            ConnectionType connectionType = getTransmitter().getConnectionType(side);
            return connectionType == ConnectionType.NORMAL || connectionType == ConnectionType.PUSH;
        };
    }

    protected InteractPredicate getInsertPredicate() {
        return (tank, side) -> {
            if (side == null) {
                //Note: We return true here, but insertion isn't actually allowed and gets blocked by the read only handler
                return true;
            }
            //If we have a side only allow inserting if our connection allows it
            ConnectionType connectionType = getTransmitter().getConnectionType(side);
            return connectionType == ConnectionType.NORMAL || connectionType == ConnectionType.PULL;
        };
    }
}