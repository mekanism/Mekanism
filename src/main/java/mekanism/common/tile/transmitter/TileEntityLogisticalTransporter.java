package mekanism.common.tile.transmitter;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.base.ITileNetwork;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.transmitter.BlockLogisticalTransporter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.TransporterTier;
import mekanism.common.transmitters.TransporterImpl;
import mekanism.common.transmitters.grid.InventoryNetwork;
import mekanism.common.upgrade.transmitter.LogisticalTransporterUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityLogisticalTransporter extends TileEntityTransmitter<TileEntity, InventoryNetwork, Void> implements ITileNetwork {

    private final byte SYNC_PACKET = 0;
    private final byte BATCH_PACKET = 1;

    public final TransporterTier tier;

    private int delay = 0;
    private int delayCount = 0;

    public TileEntityLogisticalTransporter(IBlockProvider blockProvider) {
        super(blockProvider);
        Block block = blockProvider.getBlock();
        if (block instanceof BlockLogisticalTransporter) {
            this.tier = Attribute.getTier(blockProvider.getBlock(), TransporterTier.class);
        } else {
            //Diversion and restrictive transporters
            this.tier = TransporterTier.BASIC;
        }
        transmitterDelegate = new TransporterImpl(this);
        redstoneReactive = true;
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.LOGISTICAL_TRANSPORTER;
    }

    @Override
    public TransmissionType getTransmissionType() {
        return TransmissionType.ITEM;
    }

    @Override
    public TileEntity getCachedAcceptor(Direction side) {
        return getCachedTile(side);
    }

    @Override
    public boolean isValidTransmitter(TileEntity tile) {
        Optional<ILogisticalTransporter> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null));
        if (capability.isPresent()) {
            ILogisticalTransporter transporter = capability.get();
            if (getTransmitter().getColor() == null || transporter.getColor() == null || getTransmitter().getColor() == transporter.getColor()) {
                return super.isValidTransmitter(tile);
            }
        }
        return false;
    }

    @Override
    public boolean isValidAcceptor(TileEntity tile, Direction side) {
        return TransporterUtils.isValidAcceptorOnSide(tile, side);
    }

    @Override
    public boolean handlesRedstone() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        getTransmitter().update();
    }

    public void pullItems() {
        // If a delay has been imposed, wait a bit
        if (delay > 0) {
            delay--;
            return;
        }

        // Reset delay to 3 ticks; if nothing is available to insert OR inserted, we'll try again
        // in 3 ticks
        delay = 3;

        // Attempt to pull
        for (Direction side : getConnections(ConnectionType.PULL)) {
            final TileEntity tile = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
            if (tile != null) {
                TransitRequest request = TransitRequest.buildInventoryMap(tile, side, tier.getPullAmount());

                // There's a stack available to insert into the network...
                if (!request.isEmpty()) {
                    TransitResponse response = getTransmitter().insert(tile, request, getTransmitter().getColor(), true, 0);

                    // If the insert succeeded, remove the inserted count and try again for another 10 ticks
                    if (!response.isEmpty()) {
                        response.use(tile, side.getOpposite());
                        delay = 10;
                    } else {
                        // Insert failed; increment the backoff and calculate delay. Note that we cap retries
                        // at a max of 40 ticks (2 seocnds), which would be 4 consecutive retries
                        delayCount++;
                        delay = Math.min(40, (int) Math.exp(delayCount));
                    }
                }
            }
        }
    }

    @Override
    public InventoryNetwork createNewNetwork() {
        return new InventoryNetwork();
    }

    @Override
    public InventoryNetwork createNewNetworkWithID(UUID networkID) {
        return new InventoryNetwork(networkID);
    }

    @Override
    public InventoryNetwork createNetworkByMerging(Collection<InventoryNetwork> networks) {
        return new InventoryNetwork(networks);
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT updateTag = super.getUpdateTag();
        getTransmitter().writeToUpdateTag(updateTag);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        getTransmitter().readFromUpdateTag(tag);
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        //TODO: Make a proper way for the client to retrieve sync and batch packets
        if (isRemote()) {
            byte type = dataStream.readByte();
            if (type == SYNC_PACKET) {
                readStack(dataStream);
            } else if (type == BATCH_PACKET) {
                int updates = dataStream.readInt();
                for (int i = 0; i < updates; i++) {
                    readStack(dataStream);
                }
                int deletes = dataStream.readInt();
                for (int i = 0; i < deletes; i++) {
                    getTransmitter().deleteStack(dataStream.readInt());
                }
            }
        }
    }

    public TileNetworkList makeSyncPacket(int stackId, TransporterStack stack) {
        TileNetworkList data = new TileNetworkList();
        data.add(SYNC_PACKET);
        data.add(stackId);
        stack.write(getTransmitter(), data);
        return data;
    }

    public TileNetworkList makeBatchPacket(Int2ObjectMap<TransporterStack> updates, IntSet deletes) {
        TileNetworkList data = new TileNetworkList();
        data.add(BATCH_PACKET);
        data.add(updates.size());
        for (Int2ObjectMap.Entry<TransporterStack> entry : updates.int2ObjectEntrySet()) {
            data.add(entry.getIntKey());
            entry.getValue().write(getTransmitter(), data);
        }
        data.add(deletes.size());
        data.addAll(deletes);
        return data;
    }

    private void readStack(PacketBuffer dataStream) {
        int id = dataStream.readInt();
        TransporterStack stack = TransporterStack.readFromPacket(dataStream);
        if (stack.progress == 0) {
            stack.progress = 5;
        }
        getTransmitter().addStack(id, stack);
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        getTransmitter().readFromNBT(nbtTags);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        getTransmitter().writeToNBT(nbtTags);
        return nbtTags;
    }

    @Override
    protected ActionResultType onConfigure(PlayerEntity player, int part, Direction side) {
        TransporterUtils.incrementColor(getTransmitter());
        PathfinderCache.onChanged(getTransmitter().getTransmitterNetwork());
        sendUpdatePacket();
        EnumColor color = getTransmitter().getColor();
        player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
              MekanismLang.TOGGLE_COLOR.translateColored(EnumColor.GRAY, color != null ? color.getColoredName() : MekanismLang.NONE)));
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        super.onRightClick(player, side);
        EnumColor color = getTransmitter().getColor();
        player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
              MekanismLang.CURRENT_COLOR.translateColored(EnumColor.GRAY, color != null ? color.getColoredName() : MekanismLang.NONE)));
        return ActionResultType.SUCCESS;
    }

    @Override
    public EnumColor getRenderColor() {
        return getTransmitter().getColor();
    }

    @Override
    public void remove() {
        super.remove();
        if (!isRemote()) {
            for (TransporterStack stack : getTransmitter().getTransit()) {
                TransporterUtils.drop(getTransmitter(), stack);
            }
        }
    }

    @Override
    public int getCapacity() {
        return 0;
    }

    @Override
    public Void getBuffer() {
        return null;
    }

    @Override
    public void takeShare() {
    }

    @Nonnull
    @Override
    public TransporterImpl getTransmitter() {
        return (TransporterImpl) transmitterDelegate;
    }

    public double getCost() {
        return (double) TransporterTier.ULTIMATE.getSpeed() / (double) tier.getSpeed();
    }

    @Override
    protected boolean canUpgrade(AlloyTier alloyTier) {
        return alloyTier.getBaseTier().ordinal() == tier.getBaseTier().ordinal() + 1;
    }

    @Nonnull
    @Override
    protected BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
        switch (tier) {
            case BASIC:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER.getBlock().getDefaultState());
            case ADVANCED:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER.getBlock().getDefaultState());
            case ELITE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER.getBlock().getDefaultState());
            case ULTIMATE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER.getBlock().getDefaultState());
        }
        return current;
    }

    @Nullable
    @Override
    protected LogisticalTransporterUpgradeData getUpgradeData() {
        return new LogisticalTransporterUpgradeData(redstoneReactive, connectionTypes, getTransmitter());
    }

    @Override
    protected void parseUpgradeData(@Nonnull TransmitterUpgradeData upgradeData) {
        if (upgradeData instanceof LogisticalTransporterUpgradeData) {
            LogisticalTransporterUpgradeData data = (LogisticalTransporterUpgradeData) upgradeData;
            redstoneReactive = data.redstoneReactive;
            connectionTypes = data.connectionTypes;
            getTransmitter().readFromNBT(data.nbt);
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY) {
            return Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY.orEmpty(capability, LazyOptional.of(this::getTransmitter));
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected void updateModelData(TransmitterModelData modelData) {
        super.updateModelData(modelData);
        if (modelData instanceof TransmitterModelData.Colorable) {
            TransmitterModelData.Colorable colorable = (TransmitterModelData.Colorable) modelData;
            colorable.setColor(getRenderColor() != null);
        }
    }

    @Nonnull
    @Override
    protected TransmitterModelData initModelData() {
        return new TransmitterModelData.Colorable();
    }
}