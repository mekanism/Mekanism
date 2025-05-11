package mekanism.common.tile.transmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.IAlloyInteraction;
import mekanism.api.IConfigurable;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.IAlloyTier;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.states.TransmitterType.Size;
import mekanism.common.block.transmitter.BlockLargeTransmitter;
import mekanism.common.block.transmitter.BlockSmallTransmitter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.proxy.ProxyConfigurable;
import mekanism.common.capabilities.proxy.ProxyConfigurable.ISidedConfigurable;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityTransmitter extends CapabilityTileEntity implements ISidedConfigurable, IAlloyInteraction {

    public static final ICapabilityProvider<TileEntityTransmitter, @Nullable Direction, IConfigurable> CONFIGURABLE_PROVIDER =
          capabilityProvider(Capabilities.CONFIGURABLE, (tile, cap) -> new BasicSidedCapabilityResolver<>(tile, cap, ProxyConfigurable::new));

    public static final ModelProperty<TransmitterModelData> TRANSMITTER_PROPERTY = new ModelProperty<>();

    private final Transmitter<?, ?, ?> transmitter;
    private boolean forceUpdate = true;
    private boolean loaded = false;
    private boolean markJoined = false;

    public TileEntityTransmitter(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(((IHasTileEntity<? extends TileEntityTransmitter>) blockProvider.value()).getTileType(), pos, state);
        this.transmitter = createTransmitter(blockProvider);
        cacheCoord();
    }

    protected abstract Transmitter<?, ?, ?> createTransmitter(Holder<Block> blockProvider);

    public Transmitter<?, ?, ?> getTransmitter() {
        return transmitter;
    }

    @Override
    public void setLevel(@NotNull Level level) {
        super.setLevel(level);
        if (level instanceof ServerLevel serverLevel) {
            getTransmitter().getAcceptorCache().initializeCache(serverLevel);
        }
    }

    public void setForceUpdate() {
        forceUpdate = true;
    }

    public abstract TransmitterType getTransmitterType();

    protected void onUpdateServer() {
        if (markJoined) {
            onWorldJoin(false);
            markJoined = false;
        }
        if (forceUpdate) {
            getTransmitter().refreshConnections();
            forceUpdate = false;
        }
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, TileEntityTransmitter transmitter) {
        transmitter.onUpdateServer();
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        return getTransmitter().getReducedUpdateTag(provider, super.getReducedUpdateTag(provider));
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        if (getTransmitter().handleUpdateTag(tag, provider)) {
            //Only update the model data if something got updated that caused the model data to change
            updateModelData();
        }
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        getTransmitter().read(provider, nbt);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        getTransmitter().write(provider, nbtTags);
    }

    public void onNeighborBlockChange(Direction side) {
        getTransmitter().onNeighborBlockChange(side);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (isRemote()) {
            onWorldJoin(false);
        } else {
            markJoined = true;
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (!isRemote()) {
            //Only take the transmitter's share if it was unloaded and not if we are being removed
            getTransmitter().validateAndTakeShare();
        }
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        onWorldSeparate(false);
        getTransmitter().remove();
    }

    @Override
    public void onAdded() {
        super.onAdded();
        onWorldJoin(false);
        getTransmitter().refreshConnections();
    }

    private void onWorldJoin(boolean wasPresent) {
        if (!isRemote() && !wasPresent) {
            //If we weren't already present, and we are on the server, track this transmitter
            TransmitterNetworkRegistry.trackTransmitter(getTransmitter());
        }
        if (!loaded) {
            //Only load it if it wasn't already loaded
            loaded = true;
            if (!isRemote()) {
                TransmitterNetworkRegistry.registerOrphanTransmitter(getTransmitter());
            }
        }
    }

    private void onWorldSeparate(boolean stillPresent) {
        if (!isRemote() && !stillPresent) {
            //If we aren't still present, and we are on the server, stop tracking this transmitter
            TransmitterNetworkRegistry.untrackTransmitter(getTransmitter());
        }
        if (loaded) {
            //Only unload it if it was actually loaded
            loaded = false;
            if (isRemote()) {
                getTransmitter().setTransmitterNetwork(null);
            } else {
                TransmitterNetworkRegistry.invalidateTransmitter(getTransmitter());
            }
        }
        getTransmitter().onWorldSeparate(stillPresent);
    }

    public void chunkAccessibilityChange(boolean loaded) {
        if (loaded) {
            //Chunk went from "unloaded" to loaded
            onWorldJoin(true);
        } else {
            //Chunk went from loaded to "unloaded", need to take the share first like normally happens when it unloads
            getTransmitter().validateAndTakeShare();
            onWorldSeparate(true);
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

    @NotNull
    @Override
    public InteractionResult onSneakRightClick(@NotNull Player player, @NotNull Direction side) {
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
            player.displayClientMessage(MekanismLang.CONNECTION_TYPE.translateColored(EnumColor.GRAY, transmitter.getConnectionTypeRaw(hitSide)), true);
            sendUpdatePacket();
        }
        return InteractionResult.SUCCESS;
    }

    protected InteractionResult onConfigure(Player player, Direction side) {
        //TODO: Move some of this stuff back into the tiles?
        return getTransmitter().onConfigure(player, side);
    }

    @NotNull
    @Override
    public InteractionResult onRightClick(@NotNull Player player, @NotNull Direction side) {
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

    @NotNull
    @Override
    public ModelData getModelData() {
        TransmitterModelData data = initModelData();
        updateModelData(data);
        return ModelData.of(TRANSMITTER_PROPERTY, data);
    }

    protected void updateModelData(TransmitterModelData modelData) {
        //Update the data, using information about if there is actually a connection on a given side
        for (Direction side : EnumUtils.DIRECTIONS) {
            modelData.setConnectionData(side, getTransmitter().getConnectionType(side));
        }
    }

    @NotNull
    protected TransmitterModelData initModelData() {
        return new TransmitterModelData();
    }

    @Override
    public void onAlloyInteraction(Player player, ItemStack stack, @NotNull IAlloyTier tier) {
        if (getLevel() != null && getTransmitter().hasTransmitterNetwork()) {
            DynamicNetwork<?, ?, ?> transmitterNetwork = getTransmitter().getTransmitterNetwork();
            List<Transmitter<?, ?, ?>> list = new ArrayList<>(transmitterNetwork.getTransmitters());
            list.sort((o1, o2) -> {
                if (o1 != null && o2 != null) {
                    return Double.compare(o1.getBlockPos().distSqr(worldPosition), o2.getBlockPos().distSqr(worldPosition));
                }
                return 0;
            });
            boolean sharesSet = false;
            int upgraded = 0;
            for (Transmitter<?, ?, ?> transmitter : list) {
                if (transmitter instanceof IUpgradeableTransmitter<?> upgradeableTransmitter && upgradeableTransmitter.canUpgrade(tier)) {
                    TileEntityTransmitter transmitterTile = transmitter.getTransmitterTile();
                    BlockState state = transmitterTile.getBlockState();
                    BlockState upgradeState = transmitterTile.upgradeResult(state, tier.getBaseTierLevel());
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
                    BlockPos transmitterPos = transmitter.getBlockPos();
                    Level transmitterWorld = transmitter.getLevel();
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
                if (player instanceof ServerPlayer serverPlayer) {
                    MekanismCriteriaTriggers.ALLOY_UPGRADE.value().trigger(serverPlayer);
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

    @NotNull
    protected BlockState upgradeResult(@NotNull BlockState current, int tierLevel) {
        BaseTier tier = BaseTier.getTier(tierLevel);
        if (tier == null) {
            return current;
        }
        return upgradeResult(current, tier);
    }

    @NotNull
    protected BlockState upgradeResult(@NotNull BlockState current, @NotNull BaseTier tier) {
        return current;
    }

    public void sideChanged(@NotNull Direction side, @NotNull ConnectionType old, @NotNull ConnectionType type) {
    }

    /**
     * Called if the transmitter handles redstone and the redstone activity state has changed.
     */
    public void redstoneChanged(boolean powered) {
    }

    protected Predicate<@Nullable Direction> getExtractPredicate() {
        return side -> {
            if (side == null) {
                //Note: We return true here, but extraction isn't actually allowed and gets blocked by the read only handler
                return true;
            }
            //If we have a side only allow extracting if our connection allows it
            return getTransmitter().getConnectionType(side).canSendTo();
        };
    }

    protected Predicate<@Nullable Direction> getInsertPredicate() {
        return side -> {
            if (side == null) {
                //Note: We return true here, but insertion isn't actually allowed and gets blocked by the read only handler
                return true;
            }
            //If we have a side only allow inserting if our connection allows it
            return getTransmitter().getConnectionType(side).canAccept();
        };
    }
}