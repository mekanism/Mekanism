package mekanism.common.tile.transmitter;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IAlloyInteraction;
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
import mekanism.common.capabilities.proxy.ProxyConfigurable;
import mekanism.common.capabilities.proxy.ProxyConfigurable.ISidedConfigurable;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.BasicSidedCapabilityResolver;
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
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

public abstract class TileEntityTransmitter extends CapabilityTileEntity implements ISidedConfigurable, IAlloyInteraction {

    public static final ModelProperty<TransmitterModelData> TRANSMITTER_PROPERTY = new ModelProperty<>();

    private final Transmitter<?, ?, ?> transmitter;
    private boolean forceUpdate = true;
    private boolean loaded = false;

    public TileEntityTransmitter(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(((IHasTileEntity<? extends TileEntityTransmitter>) blockProvider.getBlock()).getTileType(), pos, state);
        this.transmitter = createTransmitter(blockProvider);
        cacheCoord();
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.ALLOY_INTERACTION_CAPABILITY, this));
        addCapabilityResolver(new BasicSidedCapabilityResolver<>(this, Capabilities.CONFIGURABLE_CAPABILITY, ProxyConfigurable::new));
    }

    protected abstract Transmitter<?, ?, ?> createTransmitter(IBlockProvider blockProvider);

    public Transmitter<?, ?, ?> getTransmitter() {
        return transmitter;
    }

    public void setForceUpdate() {
        forceUpdate = true;
    }

    public abstract TransmitterType getTransmitterType();

    protected void onUpdateServer() {
        if (forceUpdate) {
            getTransmitter().refreshConnections();
            forceUpdate = false;
        }
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, TileEntityTransmitter transmitter) {
        transmitter.onUpdateServer();
    }

    @Nonnull
    @Override
    public CompoundTag getReducedUpdateTag() {
        return getTransmitter().getReducedUpdateTag(super.getReducedUpdateTag());
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag) {
        super.handleUpdateTag(tag);
        getTransmitter().handleUpdateTag(tag);
    }

    @Override
    public void handleUpdatePacket(@Nonnull CompoundTag tag) {
        super.handleUpdatePacket(tag);
        //Delay requesting the model data update and actually updating the packet until we have finished parsing the update tag
        requestModelDataUpdate();
        WorldUtils.updateBlock(getLevel(), getBlockPos(), getBlockState());
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        getTransmitter().read(nbt);
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag nbtTags) {
        super.saveAdditional(nbtTags);
        getTransmitter().write(nbtTags);
    }

    public void onNeighborTileChange(Direction side) {
        getTransmitter().onNeighborTileChange(side);
    }

    public void onNeighborBlockChange(Direction side) {
        getTransmitter().onNeighborBlockChange(side);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        onWorldJoin();
    }

    @Override
    public void onChunkUnloaded() {
        if (!isRemote()) {
            //Only take the transmitter's share if it was unloaded and not if we are being removed
            getTransmitter().takeShare();
        }
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
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

    public Direction getSideLookingAt(Player player, Direction fallback) {
        Direction side = getSideLookingAt(player);
        return side == null ? fallback : side;
    }

    @Nullable
    public Direction getSideLookingAt(Player player) {
        AdvancedRayTraceResult result = MultipartUtils.collisionRayTrace(player, getBlockPos(), getCollisionBoxes());
        if (result != null && result.valid()) {
            List<Direction> list = new ArrayList<>(EnumUtils.DIRECTIONS.length);
            byte connections = getTransmitter().getAllCurrentConnections();
            for (Direction dir : EnumUtils.DIRECTIONS) {
                if (Transmitter.connectionMapContainsSide(connections, dir)) {
                    list.add(dir);
                }
            }
            int boxIndex = result.subHit + 1;
            if (boxIndex < list.size()) {
                return list.get(boxIndex);
            }
        }
        return null;
    }

    @Nonnull
    @Override
    public InteractionResult onSneakRightClick(@Nonnull Player player, @Nonnull Direction side) {
        if (!isRemote()) {
            Direction hitSide = getSideLookingAt(player);
            if (hitSide == null) {
                if (transmitter.getConnectionTypeRaw(side) != ConnectionType.NONE) {
                    InteractionResult result = onConfigure(player, side);
                    if (result.consumesAction()) {
                        //Refresh/notify so that we actually update the block and how it can connect given color or things might have changed
                        getTransmitter().refreshConnections();
                        getTransmitter().notifyTileChange();
                        return result;
                    }
                }
                hitSide = side;
            }
            transmitter.setConnectionTypeRaw(hitSide, transmitter.getConnectionTypeRaw(hitSide).getNext());
            //Note: This stuff happens here and not in sideChanged because we don't want it to happen on load
            // or things which also would cause sideChanged to be called
            getTransmitter().onModeChange(Direction.from3DDataValue(hitSide.ordinal()));
            getTransmitter().refreshConnections();
            getTransmitter().notifyTileChange();
            player.sendMessage(MekanismLang.CONNECTION_TYPE.translate(transmitter.getConnectionTypeRaw(hitSide)), Util.NIL_UUID);
            sendUpdatePacket();
        }
        return InteractionResult.SUCCESS;
    }

    protected InteractionResult onConfigure(Player player, Direction side) {
        //TODO: Move some of this stuff back into the tiles?
        return getTransmitter().onConfigure(player, side);
    }

    @Nonnull
    @Override
    public InteractionResult onRightClick(@Nonnull Player player, @Nonnull Direction side) {
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
        list.add(isSmall ? BlockSmallTransmitter.CENTER : BlockLargeTransmitter.CENTER);
        return list;
    }

    @Nonnull
    @Override
    public AABB getRenderBoundingBox() {
        //If any of the block is in view, then allow rendering the contents
        return new AABB(worldPosition, worldPosition.offset(1, 1, 1));
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
    public void onAlloyInteraction(Player player, ItemStack stack, @Nonnull AlloyTier tier) {
        if (getLevel() != null && getTransmitter().hasTransmitterNetwork()) {
            DynamicNetwork<?, ?, ?> transmitterNetwork = getTransmitter().getTransmitterNetwork();
            List<Transmitter<?, ?, ?>> list = new ArrayList<>(transmitterNetwork.getTransmitters());
            list.sort((o1, o2) -> {
                if (o1 != null && o2 != null) {
                    return Double.compare(o1.getTilePos().distSqr(worldPosition), o2.getTilePos().distSqr(worldPosition));
                }
                return 0;
            });
            boolean sharesSet = false;
            int upgraded = 0;
            for (Transmitter<?, ?, ?> transmitter : list) {
                if (transmitter instanceof IUpgradeableTransmitter<?> upgradeableTransmitter && upgradeableTransmitter.canUpgrade(tier)) {
                    TileEntityTransmitter transmitterTile = transmitter.getTransmitterTile();
                    BlockState state = transmitterTile.getBlockState();
                    BlockState upgradeState = transmitterTile.upgradeResult(state, tier.getBaseTier());
                    if (state == upgradeState) {
                        //Skip if it would not actually upgrade anything
                        continue;
                    }
                    if (!sharesSet) {
                        if (transmitterNetwork instanceof DynamicBufferedNetwork dynamicNetwork) {
                            //Ensure we save the shares to the tiles so that they can properly take them, and they don't get voided
                            dynamicNetwork.validateSaveShares((BufferedTransmitter<?, ?, ?, ?>) transmitter);
                        }
                        sharesSet = true;
                    }
                    transmitter.startUpgrading();
                    TransmitterUpgradeData upgradeData = upgradeableTransmitter.getUpgradeData();
                    BlockPos transmitterPos = transmitter.getTilePos();
                    Level transmitterWorld = transmitter.getTileWorld();
                    if (upgradeData == null) {
                        Mekanism.logger.warn("Got no upgrade data for transmitter at position: {} in {} but it said it would be able to provide some.",
                              transmitterPos, transmitterWorld);
                    } else {
                        transmitterWorld.setBlockAndUpdate(transmitterPos, upgradeState);
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