package mekanism.common.tile.transmitter;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
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
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class TileEntityLogisticalTransporter extends TileEntityTransmitter<TileEntity, InventoryNetwork, Void> {

    private final int SYNC_PACKET = 1;
    private final int BATCH_PACKET = 2;

    public final TransporterTier tier;

    private int delay = 0;
    private int delayCount = 0;

    public TileEntityLogisticalTransporter(IBlockProvider blockProvider) {
        super(((IHasTileEntity<TileEntityLogisticalTransporter>) blockProvider.getBlock()).getTileType());
        Block block = blockProvider.getBlock();
        if (block instanceof BlockLogisticalTransporter) {
            this.tier = ((BlockLogisticalTransporter) block).getTier();
        } else {
            //Diversion and restrictive transportesr
            this.tier = TransporterTier.BASIC;
        }
        transmitterDelegate = new TransporterImpl(this);
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
    public void onWorldSeparate() {
        super.onWorldSeparate();
        if (!isRemote()) {
            PathfinderCache.onChanged(new Coord4D(getPos(), getWorld()));
        }
    }

    @Override
    public TileEntity getCachedAcceptor(Direction side) {
        return getCachedTile(side);
    }

    @Override
    public boolean isValidTransmitter(TileEntity tile) {
        return CapabilityUtils.getCapabilityHelper(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null).matches(
              transporter -> super.isValidTransmitter(tile) && (getTransmitter().getColor() == null || transporter.getColor() == null ||
                                                                getTransmitter().getColor() == transporter.getColor()));
    }

    @Override
    public boolean isValidAcceptor(TileEntity tile, Direction side) {
        return TransporterUtils.isValidAcceptorOnSide(tile, side);
    }

    @Override
    public boolean handlesRedstone() {
        return false;
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
                    TransitResponse response = TransporterUtils.insert(tile, getTransmitter(), request, getTransmitter().getColor(), true, 0);

                    // If the insert succeeded, remove the inserted count and try again for another 10 ticks
                    if (!response.isEmpty()) {
                        response.getInvStack(tile, side.getOpposite()).use(response.getSendingAmount());
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
    public void onWorldJoin() {
        super.onWorldJoin();
        PathfinderCache.onChanged(new Coord4D(getPos(), getWorld()));
    }

    @Override
    public InventoryNetwork createNewNetwork() {
        return new InventoryNetwork();
    }

    @Override
    public InventoryNetwork createNetworkByMerging(Collection<InventoryNetwork> networks) {
        return new InventoryNetwork(networks);
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) throws Exception {
        if (FMLEnvironment.dist.isClient()) {
            int type = dataStream.readInt();
            if (type == 0) {
                super.handlePacketData(dataStream);
                int c = dataStream.readInt();
                EnumColor prev = getTransmitter().getColor();
                if (c == -1) {
                    getTransmitter().setColor(null);
                } else {
                    getTransmitter().setColor(TransporterUtils.colors.get(c));
                }
                if (prev != getTransmitter().getColor()) {
                    //TODO: Only make it so it needs to request an update once instead of potentially doing it in the super as well
                    //We update the model data regardless of if it changed from one color to the next
                    // because even though it is a boolean, we also may have side/connection data that changed
                    requestModelDataUpdate();
                    MekanismUtils.updateBlock(getWorld(), pos);
                }
                getTransmitter().readFromPacket(dataStream);
            } else if (type == SYNC_PACKET) {
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

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        data.add(0);
        super.getNetworkedData(data);
        if (getTransmitter().getColor() == null) {
            data.add(-1);
        } else {
            data.add(TransporterUtils.colors.indexOf(getTransmitter().getColor()));
        }

        // Serialize all the in-flight stacks (this includes their ID)
        getTransmitter().writeToPacket(data);
        return data;
    }

    public TileNetworkList makeSyncPacket(int stackId, TransporterStack stack) {
        TileNetworkList data = new TileNetworkList();
        data.add(SYNC_PACKET);
        data.add(stackId);
        stack.write(getTransmitter(), data);
        return data;
    }

    public TileNetworkList makeBatchPacket(Map<Integer, TransporterStack> updates, Set<Integer> deletes) {
        TileNetworkList data = new TileNetworkList();
        data.add(BATCH_PACKET);
        data.add(updates.size());
        for (Entry<Integer, TransporterStack> entry : updates.entrySet()) {
            data.add(entry.getKey());
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
        if (getTransmitter().getColor() != null) {
            nbtTags.putInt("color", TransporterUtils.colors.indexOf(getTransmitter().getColor()));
        }
        ListNBT stacks = new ListNBT();
        for (TransporterStack stack : getTransmitter().getTransit()) {
            CompoundNBT tagCompound = new CompoundNBT();
            stack.write(tagCompound);
            stacks.add(tagCompound);
        }
        if (!stacks.isEmpty()) {
            nbtTags.put("stacks", stacks);
        }
        return nbtTags;
    }

    @Override
    protected ActionResultType onConfigure(PlayerEntity player, int part, Direction side) {
        TransporterUtils.incrementColor(getTransmitter());
        PathfinderCache.onChanged(new Coord4D(getPos(), getWorld()));
        Mekanism.packetHandler.sendUpdatePacket(this);
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
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
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

    @Override
    public void updateShare() {
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
    protected void updateModelData() {
        super.updateModelData();
        TransmitterModelData modelData = getModelData();
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