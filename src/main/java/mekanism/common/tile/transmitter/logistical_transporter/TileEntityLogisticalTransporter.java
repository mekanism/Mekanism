package mekanism.common.tile.transmitter.logistical_transporter;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.base.IBlockProvider;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.transmitter.BlockLogisticalTransporter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.transmitters.TransporterImpl;
import mekanism.common.transmitters.grid.InventoryNetwork;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.block.Block;
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

public abstract class TileEntityLogisticalTransporter extends TileEntityTransmitter<TileEntity, InventoryNetwork, Void> {

    private final int SYNC_PACKET = 1;
    private final int BATCH_PACKET = 2;

    public TransporterTier tier;

    private int delay = 0;
    private int delayCount = 0;

    public TileEntityLogisticalTransporter(IBlockProvider blockProvider) {
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
    public BaseTier getBaseTier() {
        return tier.getBaseTier();
    }

    @Override
    public void setBaseTier(BaseTier baseTier) {
        tier = TransporterTier.get(baseTier);
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
        if (!getWorld().isRemote) {
            PathfinderCache.onChanged(new Coord4D(getPos(), getWorld()));
        }
    }

    @Override
    public TileEntity getCachedAcceptor(Direction side) {
        return getCachedTile(side);
    }

    @Override
    public boolean isValidTransmitter(TileEntity tileEntity) {
        return CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null).matches(
              transporter -> super.isValidTransmitter(tileEntity) && (getTransmitter().getColor() == null || transporter.getColor() == null ||
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
            final TileEntity tile = MekanismUtils.getTileEntity(world, getPos().offset(side));
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
                tier = TransporterTier.values()[dataStream.readInt()];
                int c = dataStream.readInt();
                EnumColor prev = getTransmitter().getColor();
                if (c != -1) {
                    getTransmitter().setColor(TransporterUtils.colors.get(c));
                } else {
                    getTransmitter().setColor(null);
                }
                if (prev != getTransmitter().getColor()) {
                    MekanismUtils.updateBlock(world, pos);
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
        data.add(tier.ordinal());
        if (getTransmitter().getColor() != null) {
            data.add(TransporterUtils.colors.indexOf(getTransmitter().getColor()));
        } else {
            data.add(-1);
        }

        // Serialize all the in-flight stacks (this includes their ID)
        getTransmitter().writeToPacket(data);
        return data;
    }

    public TileNetworkList makeSyncPacket(int stackId, TransporterStack stack) {
        TileNetworkList data = new TileNetworkList();
        //TODO: Multipart
        /*if (Mekanism.hooks.MCMPLoaded) {
            MultipartTileNetworkJoiner.addMultipartHeader(this, data, null);
        }*/
        data.add(SYNC_PACKET);
        data.add(stackId);
        stack.write(getTransmitter(), data);
        return data;
    }

    public TileNetworkList makeBatchPacket(Map<Integer, TransporterStack> updates, Set<Integer> deletes) {
        TileNetworkList data = new TileNetworkList();
        //TODO: Multipart
        /*if (Mekanism.hooks.MCMPLoaded) {
            MultipartTileNetworkJoiner.addMultipartHeader(this, data, null);
        }*/
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
        if (nbtTags.contains("tier")) {
            tier = TransporterTier.values()[nbtTags.getInt("tier")];
        }
        getTransmitter().readFromNBT(nbtTags);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("tier", tier.ordinal());
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
        onPartChanged(null);
        PathfinderCache.onChanged(new Coord4D(getPos(), getWorld()));
        Mekanism.packetHandler.sendUpdatePacket(this);
        EnumColor color = getTransmitter().getColor();
        player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GRAY, Translation.of("tooltip.configurator.toggleColor"), ": ",
              (color != null ? color.getColoredName() : Translation.of("mekanism.gui.none"))));
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        super.onRightClick(player, side);
        EnumColor color = getTransmitter().getColor();
        player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GRAY, Translation.of("tooltip.configurator.viewColor"), ": ",
              (color != null ? color.getColoredName() : Translation.of("mekanism.gui.none"))));
        return ActionResultType.SUCCESS;
    }

    @Override
    public EnumColor getRenderColor() {
        return getTransmitter().getColor();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (!getWorld().isRemote) {
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

    @Override
    public TransporterImpl getTransmitter() {
        return (TransporterImpl) transmitterDelegate;
    }

    public double getCost() {
        return (double) TransporterTier.ULTIMATE.getSpeed() / (double) tier.getSpeed();
    }

    @Override
    public boolean upgrade(int tierOrdinal) {
        if (tier.ordinal() < BaseTier.ULTIMATE.ordinal() && tierOrdinal == tier.ordinal() + 1) {
            tier = TransporterTier.values()[tier.ordinal() + 1];
            markDirtyTransmitters();
            sendDesc = true;
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY) {
            //TODO: Check annotations/nullability
            return Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY.orEmpty(capability, LazyOptional.of(this::getTransmitter));
        }
        return super.getCapability(capability, side);
    }
}