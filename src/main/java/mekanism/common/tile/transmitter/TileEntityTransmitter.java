package mekanism.common.tile.transmitter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
import mekanism.common.capabilities.holder.IHolder;
import mekanism.common.capabilities.proxy.ProxyConfigurable;
import mekanism.common.capabilities.proxy.ProxyConfigurable.ISidedConfigurable;
import mekanism.common.capabilities.resolver.BasicSidedCapabilityResolver;
import mekanism.common.content.network.transmitter.BufferedTransmitter;
import mekanism.common.content.network.transmitter.IUpgradeableTransmitter;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transaction.TransactionHelper;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public abstract class TileEntityTransmitter extends CapabilityTileEntity implements ISidedConfigurable, IAlloyInteraction, IHolder {

    public static final ICapabilityProvider<TileEntityTransmitter, @Nullable Direction, IConfigurable> CONFIGURABLE_PROVIDER = capabilityProvider(Capabilities.CONFIGURABLE,
          (tile, cap) -> new BasicSidedCapabilityResolver<>(tile, cap, ProxyConfigurable::new));

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
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level instanceof ServerLevel serverLevel) {
            getTransmitter().getAcceptorCache().initializeCache(serverLevel);
        }
    }

    public void setForceUpdate() {
        forceUpdate = true;
    }

    public abstract TransmitterType getTransmitterType();

    protected void onUpdateServer(ServerLevel level) {
        if (markJoined) {
            onWorldJoin(level, false);
            markJoined = false;
        }
        if (forceUpdate) {
            getTransmitter().refreshConnections();
            forceUpdate = false;
        }
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, TileEntityTransmitter transmitter) {
        transmitter.onUpdateServer((ServerLevel) level);
    }

    @Override
    public void writeReducedUpdatedTag(ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        getTransmitter().writeReducedUpdatedTag(output);
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        super.loadAdditional(input);//we do NOT call super directly, as it will call a load and the below check never sees the changes
        if (getTransmitter().handleUpdateTag(input)) {
            //Only update the model data if something got updated that caused the model data to change
            updateModelData();
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        getTransmitter().read(input);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        getTransmitter().write(output);
    }

    public void onNeighborBlockChange(@Nullable Direction side) {
        getTransmitter().onNeighborBlockChange(side);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (level != null && level.isClientSide()) {
            onWorldJoin(level, false);
        } else {
            markJoined = true;
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (level != null && !level.isClientSide()) {
            //Only take the transmitter's share if it was unloaded and not if we are being removed
            getTransmitter().validateAndTakeShare(null);
        }
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) {//TODO - 26.2: Re-evaluate this check
            onWorldSeparate(level, false);
        }
        getTransmitter().remove();
    }

    @Override
    public void onAdded(Level level) {
        super.onAdded(level);
        onWorldJoin(level, false);
        getTransmitter().refreshConnections();
    }

    private void onWorldJoin(Level level, boolean wasPresent) {
        if (!level.isClientSide() && !wasPresent) {
            //If we weren't already present, and we are on the server, track this transmitter
            TransmitterNetworkRegistry.trackTransmitter(getTransmitter());
        }
        if (!loaded) {
            //Only load it if it wasn't already loaded
            loaded = true;
            if (!level.isClientSide()) {
                TransmitterNetworkRegistry.registerOrphanTransmitter(getTransmitter());
            }
        }
    }

    private void onWorldSeparate(Level level, boolean stillPresent) {
        if (!level.isClientSide() && !stillPresent) {
            //If we aren't still present, and we are on the server, stop tracking this transmitter
            TransmitterNetworkRegistry.untrackTransmitter(getTransmitter());
        }
        if (loaded) {
            //Only unload it if it was actually loaded
            loaded = false;
            if (level.isClientSide()) {
                getTransmitter().setTransmitterNetwork(null);
            } else {
                TransmitterNetworkRegistry.invalidateTransmitter(getTransmitter());
            }
        }
        getTransmitter().onWorldSeparate(stillPresent);
    }

    public void chunkAccessibilityChange(boolean loaded) {
        Level level = Objects.requireNonNull(getLevel(), "Level should not be null if it is in a chunk that changed ticket level");
        if (loaded) {
            //Chunk went from "unloaded" to loaded
            onWorldJoin(level, true);
        } else {
            //Chunk went from loaded to "unloaded", need to take the share first like normally happens when it unloads
            getTransmitter().validateAndTakeShare(null);
            onWorldSeparate(level, true);
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
    private Direction getSideLookingAt(Player player) {
        AdvancedRayTraceResult result = MultipartUtils.collisionRayTrace(player, getBlockPos(), getCollisionBoxes());
        if (result != null && result.valid()) {
            List<Direction> list = new ArrayList<>(EnumUtils.DIRECTIONS.length);
            byte connections = getTransmitter().getAllCurrentConnections();
            for (Direction dir : EnumUtils.DIRECTIONS) {
                if (Transmitter.connectionMapContainsSide(connections, dir)) {
                    list.add(dir);
                }
            }
            int boxIndex = result.subHit() + 1;
            if (boxIndex < list.size()) {
                return list.get(boxIndex);
            }
        }
        return null;
    }

    @Override
    public InteractionResult onSneakRightClick(Level level, Player player, Direction side) {
        if (!level.isClientSide()) {
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
            player.sendOverlayMessage(MekanismLang.CONNECTION_TYPE.translateColored(EnumColor.GRAY, transmitter.getConnectionTypeRaw(hitSide)));
            sendUpdatePacket();
        }
        return InteractionResult.SUCCESS;
    }

    protected InteractionResult onConfigure(Player player, Direction side) {
        //TODO: Move some of this stuff back into the tiles?
        return getTransmitter().onConfigure(player, side);
    }

    @Override
    public InteractionResult onRightClick(Level level, Player player, Direction side) {
        return getTransmitter().onRightClick(level, player, side);
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

    @Override
    public ModelData getModelData() {
        TransmitterModelData data = initModelData();
        updateModelData(data);
        return ModelData.of(TRANSMITTER_PROPERTY, data);
    }

    protected void updateModelData(TransmitterModelData modelData) {
        Transmitter<?, ?, ?> myTransmitter = getTransmitter();
        //Update the data, using information about if there is actually a connection on a given side
        ConnectionType[] connections = new ConnectionType[EnumUtils.DIRECTIONS.length];
        for (Direction side : EnumUtils.DIRECTIONS) {
            connections[side.ordinal()] = myTransmitter.getConnectionType(side);
        }
        modelData.setConnectionData(connections);
    }

    protected TransmitterModelData initModelData() {
        return new TransmitterModelData();
    }

    @Override
    public void onAlloyInteraction(Level level, BlockPos pos, Player player, ItemStack stack, IAlloyTier tier) {
        if (getTransmitter().hasTransmitterNetwork()) {
            DynamicNetwork<?, ?, ?> transmitterNetwork = getTransmitter().getTransmitterNetworkNN();
            List<Transmitter<?, ?, ?>> list = new ArrayList<>(transmitterNetwork.getTransmitters());
            list.sort(Comparator.comparingDouble(transmitter -> transmitter.getBlockPos().distSqr(pos)));
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
                            try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
                                dynamicNetwork.validateSaveShares((BufferedTransmitter<?, ?, ?, ?>) transmitter, transaction);
                                transaction.commit();
                            }
                        }
                        sharesSet = true;
                    }
                    transmitter.startUpgrading();
                    TransmitterUpgradeData upgradeData = upgradeableTransmitter.getUpgradeData();
                    BlockPos transmitterPos = transmitter.getBlockPos();
                    if (upgradeData == null) {
                        Mekanism.logger.warn("Got no upgrade data for transmitter at position: {} in {} but it said it would be able to provide some.",
                              transmitterPos, level);
                    } else {
                        level.setBlockAndUpdate(transmitterPos, upgradeState);
                        TileEntityTransmitter upgradedTile = WorldUtils.getTileEntity(TileEntityTransmitter.class, level, transmitterPos);
                        if (upgradedTile == null) {
                            Mekanism.logger.warn("Error upgrading transmitter at position: {} in {}.", transmitterPos, level);
                        } else {
                            Transmitter<?, ?, ?> upgradedTransmitter = upgradedTile.getTransmitter();
                            if (upgradedTransmitter instanceof IUpgradeableTransmitter) {
                                try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
                                    transferUpgradeData((IUpgradeableTransmitter<?>) upgradedTransmitter, upgradeData, transaction);
                                    transaction.commit();
                                }
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
                //TODO - 26.2: Figure out what other uses of ItemStack#shrink should be replaced with ItemStack#consume
                stack.consume(1, player);
                if (player instanceof ServerPlayer serverPlayer) {
                    MekanismCriteriaTriggers.ALLOY_UPGRADE.value().trigger(serverPlayer);
                }
            }
        }
    }

    private <DATA extends TransmitterUpgradeData> void transferUpgradeData(IUpgradeableTransmitter<DATA> upgradeableTransmitter, TransmitterUpgradeData data, TransactionContext transaction) {
        if (upgradeableTransmitter.dataTypeMatches(data)) {
            upgradeableTransmitter.parseUpgradeData((DATA) data, transaction);
        } else {
            Mekanism.logger.warn("Unhandled upgrade data.", new IllegalStateException());
        }
    }

    protected BlockState upgradeResult(BlockState current, int tierLevel) {
        BaseTier tier = BaseTier.getTier(tierLevel);
        if (tier == null) {
            return current;
        }
        return upgradeResult(current, tier);
    }

    protected BlockState upgradeResult(BlockState current, BaseTier tier) {
        return current;
    }

    public void sideChanged(Direction side, ConnectionType old, ConnectionType type) {
    }

    /// Called if the transmitter handles redstone and the redstone activity state has changed.
    public void redstoneChanged(boolean powered) {
    }

    @Override
    public boolean canExtract(@Nullable Direction side) {
        if (side == null) {
            //Note: We return true here, but extraction isn't actually allowed and gets blocked by the read only handler
            return true;
        }
        //If we have a side only allow extracting if our connection allows it
        return getTransmitter().getConnectionType(side).canSendTo();
    }

    @Override
    public boolean canInsert(@Nullable Direction side) {
        if (side == null) {
            //Note: We return true here, but insertion isn't actually allowed and gets blocked by the read only handler
            return true;
        }
        //If we have a side only allow inserting if our connection allows it
        return getTransmitter().getConnectionType(side).canAccept();
    }
}