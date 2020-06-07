package mekanism.common.tile.transmitter;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.IntConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.transmitter.BlockLogisticalTransporter;
import mekanism.common.content.transmitter.InventoryNetwork;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.IGridTransmitter;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.PacketTransporterUpdate;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.interfaces.ILogisticalTransporter;
import mekanism.common.upgrade.transmitter.LogisticalTransporterUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;

//TODO: Validate that the sub classes of this still work properly
public class TileEntityLogisticalTransporter extends TileEntityTransmitter<TileEntity, InventoryNetwork, Void> implements ILogisticalTransporter {

    private final Int2ObjectMap<TransporterStack> transit = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<TransporterStack> needsSync = new Int2ObjectOpenHashMap<>();
    public final TransporterTier tier;
    private EnumColor color;
    private int nextId = 0;
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
    }

    @Override
    public boolean handlesRedstone() {
        return false;
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
    public EnumColor getColor() {
        return color;
    }

    @Override
    public void setColor(EnumColor c) {
        color = c;
    }

    @Override
    public boolean canEmitTo(TileEntity tile, Direction side) {
        if (!canConnect(side)) {
            return false;
        }
        ConnectionType connectionType = getConnectionType(side);
        return connectionType == ConnectionType.NORMAL || connectionType == ConnectionType.PUSH;
    }

    @Override
    public boolean canReceiveFrom(TileEntity tile, Direction side) {
        if (!canConnect(side)) {
            return false;
        }
        return getConnectionType(side) == ConnectionType.NORMAL;
    }

    @Override
    public TileEntity getAcceptor(Direction side) {
        return getCachedTile(side);
    }

    @Override
    public boolean isValidTransmitter(TileEntity tile) {
        if (tile instanceof ILogisticalTransporter) {
            ILogisticalTransporter transporter = (ILogisticalTransporter) tile;
            if (getColor() == null || transporter.getColor() == null || getColor() == transporter.getColor()) {
                return super.isValidTransmitter(tile);
            }
        }
        return false;
    }

    @Override
    public boolean isValidAcceptor(TileEntity tile, Direction side) {
        //TODO: Maybe merge this back with TransporterUtils.isValidAcceptorOnSide
        //return TransporterUtils.isValidAcceptorOnSide(tile, side);
        if (tile instanceof IGridTransmitter && TransmissionType.ITEM.checkTransmissionType(((IGridTransmitter<?, ?, ?>) tile))) {
            return false;
        }
        return isAcceptorAndListen(tile, side, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    @Override
    public void tick() {
        super.tick();
        if (isRemote()) {
            for (TransporterStack stack : transit.values()) {
                stack.progress = Math.min(100, stack.progress + tier.getSpeed());
            }
        } else if (getTransmitterNetwork() != null) {
            IntSet deletes = new IntOpenHashSet();
            pullItems();
            Coord4D coord = coord();
            //Note: Our calls to getTileEntity are not done with a chunkMap as we don't tend to have that many tiles we
            // are checking at once from here and given this gets called each tick, it would cause unnecessary garbage
            // collection to occur actually causing the tick time to go up slightly.
            for (Int2ObjectMap.Entry<TransporterStack> entry : transit.int2ObjectEntrySet()) {
                int stackId = entry.getIntKey();
                TransporterStack stack = entry.getValue();
                if (!stack.initiatedPath) {
                    if (stack.itemStack.isEmpty() || !recalculate(stackId, stack, null)) {
                        deletes.add(stackId);
                        continue;
                    }
                }

                stack.progress += tier.getSpeed();
                if (stack.progress >= 100) {
                    Coord4D prevSet = null;
                    if (stack.hasPath()) {
                        int currentIndex = stack.getPath().indexOf(coord);
                        if (currentIndex == 0) { //Necessary for transition reasons, not sure why
                            deletes.add(stackId);
                            continue;
                        }

                        Coord4D next = stack.getPath().get(currentIndex - 1);
                        if (next != null) {
                            if (!stack.isFinal(this)) {
                                TileEntity tile = MekanismUtils.getTileEntity(world, next.getPos());
                                if (stack.canInsertToTransporter(tile, stack.getSide(this), this)) {
                                    if (tile instanceof ILogisticalTransporter) {
                                        ((ILogisticalTransporter) tile).entityEntering(stack, stack.progress % 100);
                                    }
                                    deletes.add(stackId);
                                    continue;
                                }
                                prevSet = next;
                            } else if (stack.getPathType() != Path.NONE) {
                                TileEntity tile = MekanismUtils.getTileEntity(world, next.getPos());
                                if (tile != null) {
                                    TransitResponse response = TransitRequest.simple(stack.itemStack).addToInventory(tile, stack.getSide(this),
                                          stack.getPathType() == Path.HOME);
                                    // Nothing was rejected; remove the stack from the prediction tracker and
                                    // schedule this stack for deletion. Continue the loop thereafter
                                    ItemStack rejected = response.getRejected();
                                    if (rejected.isEmpty()) {
                                        TransporterManager.remove(stack);
                                        deletes.add(stackId);
                                        continue;
                                    }
                                    // Some portion of the stack got rejected; save the remainder and
                                    // let the recalculate below sort out what to do next
                                    stack.itemStack = rejected;
                                    prevSet = next;
                                }
                            }
                        }
                    }
                    if (!recalculate(stackId, stack, prevSet)) {
                        deletes.add(stackId);
                    } else if (prevSet != null) {
                        stack.progress = 0;
                    } else {
                        stack.progress = 50;
                    }
                } else if (stack.progress == 50) {
                    boolean tryRecalculate;
                    if (stack.isFinal(this)) {
                        tryRecalculate = checkPath(stack, Path.DEST, false) || checkPath(stack, Path.HOME, true) || stack.getPathType() == Path.NONE;
                    } else {
                        tryRecalculate = !stack.canInsertToTransporter(MekanismUtils.getTileEntity(world, stack.getNext(this).getPos()), stack.getSide(this), this);
                    }
                    if (tryRecalculate && !recalculate(stackId, stack, null)) {
                        deletes.add(stackId);
                    }
                }
            }

            if (!deletes.isEmpty() || !needsSync.isEmpty()) {
                //Notify clients, so that we send the information before we start clearing our lists
                Mekanism.packetHandler.sendToAllTracking(new PacketTransporterUpdate(this, needsSync, deletes), world, coord.getPos());
                // Now remove any entries from transit that have been deleted
                deletes.forEach((IntConsumer) (this::deleteStack));

                // Clear the pending sync packets
                needsSync.clear();

                // Finally, mark chunk for save
                MekanismUtils.saveChunk(this);
            }
        }
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
                TransitRequest request = TransitRequest.anyItem(tile, side, tier.getPullAmount());

                // There's a stack available to insert into the network...
                if (!request.isEmpty()) {
                    TransitResponse response = insert(tile, request, getColor(), true, 0);

                    // If the insert succeeded, remove the inserted count and try again for another 10 ticks
                    if (!response.isEmpty()) {
                        response.useAll();
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
    public InventoryNetwork createEmptyNetwork() {
        return new InventoryNetwork();
    }

    @Override
    public InventoryNetwork createEmptyNetworkWithID(UUID networkID) {
        return new InventoryNetwork(networkID);
    }

    @Override
    public InventoryNetwork createNetworkByMerging(Collection<InventoryNetwork> networks) {
        return new InventoryNetwork(networks);
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.putInt(NBTConstants.COLOR, TransporterUtils.getColorIndex(getColor()));
        ListNBT stacks = new ListNBT();
        for (Int2ObjectMap.Entry<TransporterStack> entry : transit.int2ObjectEntrySet()) {
            CompoundNBT tagCompound = new CompoundNBT();
            tagCompound.putInt(NBTConstants.INDEX, entry.getIntKey());
            entry.getValue().writeToUpdateTag(this, tagCompound);
            stacks.add(tagCompound);
        }
        if (!stacks.isEmpty()) {
            updateTag.put(NBTConstants.ITEMS, stacks);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setEnumIfPresent(tag, NBTConstants.COLOR, TransporterUtils::readColor, this::setColor);
        transit.clear();
        if (tag.contains(NBTConstants.ITEMS, NBT.TAG_LIST)) {
            ListNBT tagList = tag.getList(NBTConstants.ITEMS, NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                CompoundNBT compound = tagList.getCompound(i);
                TransporterStack stack = TransporterStack.readFromUpdate(compound);
                addStack(compound.getInt(NBTConstants.INDEX), stack);
            }
        }
    }

    @Override
    public void read(@Nonnull CompoundNBT nbtTags) {
        super.read(nbtTags);
        readFromNBT(nbtTags);
    }

    public void readFromNBT(CompoundNBT nbtTags) {
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.COLOR, TransporterUtils::readColor, this::setColor);
        if (nbtTags.contains(NBTConstants.ITEMS, NBT.TAG_LIST)) {
            ListNBT tagList = nbtTags.getList(NBTConstants.ITEMS, NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                TransporterStack stack = TransporterStack.readFromNBT(tagList.getCompound(i));
                addStack(nextId++, stack);
            }
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        writeToNBT(nbtTags);
        return nbtTags;
    }

    public void writeToNBT(CompoundNBT nbtTags) {
        nbtTags.putInt(NBTConstants.COLOR, TransporterUtils.getColorIndex(getColor()));
        ListNBT stacks = new ListNBT();
        for (TransporterStack stack : getTransit()) {
            CompoundNBT tagCompound = new CompoundNBT();
            stack.write(tagCompound);
            stacks.add(tagCompound);
        }
        if (!stacks.isEmpty()) {
            nbtTags.put(NBTConstants.ITEMS, stacks);
        }
    }

    @Override
    protected ActionResultType onConfigure(PlayerEntity player, int part, Direction side) {
        TransporterUtils.incrementColor(this);
        PathfinderCache.onChanged(getTransmitterNetwork());
        sendUpdatePacket();
        EnumColor color = getColor();
        player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
              MekanismLang.TOGGLE_COLOR.translateColored(EnumColor.GRAY, color != null ? color.getColoredName() : MekanismLang.NONE)));
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        super.onRightClick(player, side);
        EnumColor color = getColor();
        player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
              MekanismLang.CURRENT_COLOR.translateColored(EnumColor.GRAY, color != null ? color.getColoredName() : MekanismLang.NONE)));
        return ActionResultType.SUCCESS;
    }

    @Override
    public void remove() {
        super.remove();
        if (!isRemote()) {
            for (TransporterStack stack : getTransit()) {
                TransporterUtils.drop(this, stack);
            }
        }
    }

    @Override
    public long getCapacity() {
        return 0;
    }

    @Override
    public Void releaseShare() {
        return null;
    }

    @Override
    public Void getShare() {
        return null;
    }

    @Override
    public void takeShare() {
    }

    @Override
    public double getCost() {
        return TransporterTier.ULTIMATE.getSpeed() / (double) tier.getSpeed();
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
        return new LogisticalTransporterUpgradeData(redstoneReactive, connectionTypes, this);
    }

    @Override
    protected void parseUpgradeData(@Nonnull TransmitterUpgradeData upgradeData) {
        if (upgradeData instanceof LogisticalTransporterUpgradeData) {
            LogisticalTransporterUpgradeData data = (LogisticalTransporterUpgradeData) upgradeData;
            redstoneReactive = data.redstoneReactive;
            connectionTypes = data.connectionTypes;
            readFromNBT(data.nbt);
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    @Override
    protected void updateModelData(TransmitterModelData modelData) {
        super.updateModelData(modelData);
        modelData.setHasColor(getColor() != null);
    }

    public Collection<TransporterStack> getTransit() {
        return Collections.unmodifiableCollection(transit.values());
    }

    public void deleteStack(int id) {
        transit.remove(id);
    }

    public void addStack(int id, TransporterStack s) {
        transit.put(id, s);
    }

    private boolean checkPath(TransporterStack stack, Path dest, boolean home) {
        return stack.getPathType() == dest && (!checkSideForInsert(stack) || !TransporterUtils.canInsert(MekanismUtils.getTileEntity(world, stack.getDest().getPos()),
              stack.color, stack.itemStack, stack.getSide(this), home));
    }

    private boolean checkSideForInsert(TransporterStack stack) {
        Direction side = stack.getSide(this);
        ConnectionType connectionType = getConnectionType(side);
        return connectionType == ConnectionType.NORMAL || connectionType == ConnectionType.PUSH;
    }

    private boolean recalculate(int stackId, TransporterStack stack, Coord4D from) {
        boolean noPath = stack.getPathType() == Path.NONE;
        if (!noPath) {
            noPath = stack.recalculatePath(TransitRequest.simple(stack.itemStack), this, 0).isEmpty();
        }
        if (noPath && !stack.calculateIdle(this)) {
            TransporterUtils.drop(this, stack);
            return false;
        }

        //Only add to needsSync if true is being returned; otherwise it gets added to deletes
        needsSync.put(stackId, stack);
        if (from != null) {
            stack.originalLocation = from;
        }
        return true;
    }

    @Override
    public TransitResponse insert(TileEntity outputter, TransitRequest request, EnumColor color, boolean doEmit, int min) {
        Coord4D original = Coord4D.get(outputter);
        Direction from = coord().sideDifference(original).getOpposite();
        TransporterStack stack = new TransporterStack();
        stack.originalLocation = original;
        stack.homeLocation = original;
        stack.color = color;
        if (!stack.canInsertToTransporter(this, from, outputter)) {
            return request.getEmptyResponse();
        }
        TransitResponse response = stack.recalculatePath(request, this, min);
        return updateTransit(doEmit, stack, response);
    }

    @Nonnull
    private TransitResponse updateTransit(boolean doEmit, TransporterStack stack, TransitResponse response) {
        if (!response.isEmpty()) {
            stack.itemStack = response.getStack();
            if (doEmit) {
                int stackId = nextId++;
                addStack(stackId, stack);
                Mekanism.packetHandler.sendToAllTracking(new PacketTransporterUpdate(this, stackId, stack), this);
                MekanismUtils.saveChunk(this);
            }
        }
        return response;
    }

    @Override
    public TransitResponse insertRR(TileEntityLogisticalSorter outputter, TransitRequest request, EnumColor color, boolean doEmit, int min) {
        Direction from = coord().sideDifference(Coord4D.get(outputter)).getOpposite();
        TransporterStack stack = new TransporterStack();
        stack.originalLocation = Coord4D.get(outputter);
        stack.homeLocation = Coord4D.get(outputter);
        stack.color = color;
        if (!canReceiveFrom(outputter, from) || !stack.canInsertToTransporter(this, from, outputter)) {
            return request.getEmptyResponse();
        }
        TransitResponse response = stack.recalculateRRPath(request, outputter, this, min);
        return updateTransit(doEmit, stack, response);
    }

    @Override
    public void entityEntering(TransporterStack stack, int progress) {
        // Update the progress of the stack and add it as something that's both
        // in transit and needs sync down to the client.
        //
        // This code used to generate a sync message at this point, but that was a LOT
        // of bandwidth in a busy server, so by adding to needsSync, the sync will happen
        // in a batch on a per-tick basis.
        int stackId = nextId++;
        stack.progress = progress;
        addStack(stackId, stack);
        needsSync.put(stackId, stack);

        // N.B. We are not marking the chunk as dirty here! I don't believe it's needed, since
        // the next tick will generate the necessary save and if we crash before the next tick,
        // it's unlikely the data will be save anyways (since chunks aren't saved until the end of
        // a tick).
    }
}