package mekanism.common.content.network.transmitter;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collection;
import java.util.Collections;
import java.util.PrimitiveIterator.OfInt;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.network.InventoryNetwork;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.lib.inventory.IAdvancedTransportEjector;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AbstractAcceptorCache;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.transmitter.PacketTransporterBatch;
import mekanism.common.network.to_client.transmitter.PacketTransporterSync;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LogisticalTransporterBase extends Transmitter<IItemHandler, InventoryNetwork, LogisticalTransporterBase> {

    protected final Int2ObjectMap<TransporterStack> transit = new Int2ObjectOpenHashMap<>();
    protected final Int2ObjectMap<TransporterStack> needsSync = new Int2ObjectOpenHashMap<>();
    public final TransporterTier tier;
    protected int nextId = 0;
    protected int delay = 0;
    protected int delayCount = 0;

    protected LogisticalTransporterBase(TileEntityTransmitter tile, TransporterTier tier) {
        super(tile, TransmissionType.ITEM);
        this.tier = tier;
    }

    @Override
    protected AbstractAcceptorCache<IItemHandler, ?> createAcceptorCache() {
        return new AcceptorCache<>(getTransmitterTile(), Capabilities.ITEM.block());
    }

    @Override
    @SuppressWarnings("unchecked")
    public AcceptorCache<IItemHandler> getAcceptorCache() {
        return (AcceptorCache<IItemHandler>) super.getAcceptorCache();
    }

    @Override
    public boolean handlesRedstone() {
        return false;
    }

    public boolean exposesInsertCap(@NotNull Direction side) {
        return getConnectionTypeRaw(side).canAccept();
    }

    @Nullable
    public EnumColor getColor() {
        return null;
    }

    public boolean canEmitTo(Direction side) {
        return canConnect(side) && getConnectionType(side).canSendTo();
    }

    public boolean canReceiveFrom(Direction side) {
        return canConnect(side) && getConnectionType(side).canAccept();
    }

    @Override
    public boolean isValidTransmitterBasic(TileEntityTransmitter transmitter, Direction side) {
        if (transmitter.getTransmitter() instanceof LogisticalTransporterBase transporter) {
            if (getColor() == null || transporter.getColor() == null || getColor() == transporter.getColor()) {
                return super.isValidTransmitterBasic(transmitter, side);
            }
        }
        return false;
    }

    public void onUpdateClient() {
        for (TransporterStack stack : transit.values()) {
            stack.progress = Math.min(100, stack.progress + tier.getSpeed());
        }
    }

    public void onUpdateServer() {
        if (getTransmitterNetwork() != null) {
            //Pull items into the transporter
            if (delay > 0) {
                //If a delay has been imposed, wait a bit
                delay--;
            } else {
                //Reset delay to 3 ticks; if nothing is available to insert OR inserted, we'll try again in 3 ticks
                delay = 3;
                //Attempt to pull
                BlockPos.MutableBlockPos inventoryPos = new BlockPos.MutableBlockPos();
                BlockPos pos = getBlockPos();
                for (Direction side : getConnections(ConnectionType.PULL)) {
                    inventoryPos.setWithOffset(pos, side);
                    IItemHandler inventory = Capabilities.ITEM.getCapabilityIfLoaded(getLevel(), inventoryPos, side.getOpposite());
                    if (inventory != null) {
                        TransitRequest request = TransitRequest.anyItem(inventory, tier.getPullAmount());
                        //There's a stack available to insert into the network...
                        if (!request.isEmpty()) {
                            TransitResponse response = insert(null, inventoryPos, request, getColor(), true, 0);
                            if (response.isEmpty()) {
                                //Insert failed; increment the backoff and calculate delay. Note that we cap retries
                                // at a max of 40 ticks (2 seconds), which would be 4 consecutive retries
                                delayCount++;
                                delay = Math.min(40, (int) Math.exp(delayCount));
                            } else {
                                //If the insert succeeded, remove the inserted count and try again for another 10 ticks
                                response.useAll();
                                delay = MekanismUtils.TICKS_PER_HALF_SECOND;
                            }
                        }
                    }
                }
            }
            if (!transit.isEmpty()) {
                InventoryNetwork network = getTransmitterNetwork();
                //Update stack positions
                IntSet deletes = new IntOpenHashSet();
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

                    int prevProgress = stack.progress;
                    stack.progress += tier.getSpeed();
                    if (stack.progress >= 100) {
                        BlockPos prevSet = null;
                        if (stack.hasPath()) {
                            int currentIndex = stack.getPath().indexOf(getBlockPos());
                            if (currentIndex == 0) { //Necessary for transition reasons, not sure why
                                deletes.add(stackId);
                                continue;
                            }
                            BlockPos next = stack.getPath().get(currentIndex - 1);
                            if (next != null) {
                                if (!stack.isFinal(this)) {
                                    LogisticalTransporterBase transmitter = network.getTransmitter(next);
                                    if (stack.canInsertToTransporter(transmitter, stack.getSide(this), this)) {
                                        transmitter.entityEntering(stack, stack.progress % 100);
                                        deletes.add(stackId);
                                        continue;
                                    }
                                    prevSet = next;
                                } else if (stack.getPathType() != Path.NONE) {
                                    //Get the handler we are trying to insert into from the network's acceptor cache
                                    IItemHandler acceptor = network.getCachedAcceptor(next, stack.getSide(this).getOpposite());
                                    TransitResponse response = TransitRequest.simple(stack.itemStack).addToInventory(getLevel(), next, acceptor, 0,
                                          stack.getPathType() == Path.HOME);
                                    if (!response.isEmpty()) {
                                        //We were able to add at least part of the stack to the inventory
                                        ItemStack rejected = response.getRejected();
                                        if (rejected.isEmpty()) {
                                            //Nothing was rejected (it was all accepted); remove the stack from the prediction
                                            // tracker and schedule this stack for deletion. Continue the loop thereafter
                                            TransporterManager.remove(getLevel(), stack);
                                            deletes.add(stackId);
                                            continue;
                                        }
                                        //Some portion of the stack got rejected; save the remainder and
                                        // let the recalculate below sort out what to do next
                                        stack.itemStack = rejected;
                                    }//else the entire stack got rejected (Note: we don't need to update the stack to point to itself)
                                    prevSet = next;
                                }
                            }
                        }
                        if (!recalculate(stackId, stack, prevSet)) {
                            deletes.add(stackId);
                        } else if (prevSet == null) {
                            stack.progress = 50;
                        } else {
                            stack.progress = 0;
                        }
                    } else if (prevProgress < 50 && stack.progress >= 50) {
                        boolean tryRecalculate;
                        if (stack.isFinal(this)) {
                            Path pathType = stack.getPathType();
                            if (pathType == Path.DEST || pathType == Path.HOME) {
                                Direction side = stack.getSide(this);
                                ConnectionType connectionType = getConnectionType(side);
                                tryRecalculate = !connectionType.canSendTo() ||
                                                 !TransporterUtils.canInsert(getLevel(), stack.getDest(), stack.color, stack.itemStack, side, pathType == Path.HOME);
                            } else {
                                tryRecalculate = pathType == Path.NONE;
                            }
                        } else {
                            LogisticalTransporterBase nextTransmitter = network.getTransmitter(stack.getNext(this));
                            if (nextTransmitter == null && stack.getPathType() == Path.NONE && stack.getPath().size() == 2) {
                                //If there is no next transmitter, and it was an idle path, assume that we are idling
                                // in a single length transmitter, in which case we only recalculate it at 50 if it won't
                                // be able to go into that connection type
                                ConnectionType connectionType = getConnectionType(stack.getSide(this));
                                tryRecalculate = !connectionType.canSendTo();
                            } else {
                                tryRecalculate = !stack.canInsertToTransporter(nextTransmitter, stack.getSide(this), this);
                            }
                        }
                        if (tryRecalculate && !recalculate(stackId, stack, null)) {
                            deletes.add(stackId);
                        }
                    }
                }

                if (!deletes.isEmpty() || !needsSync.isEmpty()) {
                    //Notify clients, so that we send the information before we start clearing our lists
                    PacketUtils.sendToAllTracking(new PacketTransporterBatch(getBlockPos(), deletes, needsSync), getTransmitterTile());
                    // Now remove any entries from transit that have been deleted
                    OfInt ofInt = deletes.iterator();
                    while (ofInt.hasNext()) {
                        deleteStack(ofInt.nextInt());
                    }

                    // Clear the pending sync packets
                    needsSync.clear();

                    // Finally, mark chunk for save
                    getTransmitterTile().markForSave();
                }
            }
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (!isRemote()) {
            for (TransporterStack stack : getTransit()) {
                TransporterManager.remove(getLevel(), stack);
            }
        }
    }

    @Override
    public InventoryNetwork createEmptyNetworkWithID(UUID networkID) {
        return new InventoryNetwork(networkID);
    }

    @Override
    public InventoryNetwork createNetworkByMerging(Collection<InventoryNetwork> networks) {
        return new InventoryNetwork(networks);
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(CompoundTag updateTag) {
        updateTag = super.getReducedUpdateTag(updateTag);
        ListTag stacks = new ListTag();
        for (Int2ObjectMap.Entry<TransporterStack> entry : transit.int2ObjectEntrySet()) {
            CompoundTag tagCompound = new CompoundTag();
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
    public void handleUpdateTag(@NotNull CompoundTag tag) {
        super.handleUpdateTag(tag);
        transit.clear();
        if (tag.contains(NBTConstants.ITEMS, Tag.TAG_LIST)) {
            ListTag tagList = tag.getList(NBTConstants.ITEMS, Tag.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                CompoundTag compound = tagList.getCompound(i);
                TransporterStack stack = TransporterStack.readFromUpdate(compound);
                addStack(compound.getInt(NBTConstants.INDEX), stack);
            }
        }
    }

    @Override
    public void read(@NotNull CompoundTag nbtTags) {
        super.read(nbtTags);
        readFromNBT(nbtTags);
    }

    protected void readFromNBT(CompoundTag nbtTags) {
        if (nbtTags.contains(NBTConstants.ITEMS, Tag.TAG_LIST)) {
            ListTag tagList = nbtTags.getList(NBTConstants.ITEMS, Tag.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                addStack(nextId++, TransporterStack.readFromNBT(tagList.getCompound(i)));
            }
        }
    }

    @NotNull
    @Override
    public CompoundTag write(@NotNull CompoundTag nbtTags) {
        super.write(nbtTags);
        writeToNBT(nbtTags);
        return nbtTags;
    }

    public void writeToNBT(CompoundTag nbtTags) {
        Collection<TransporterStack> transit = getTransit();
        if (!transit.isEmpty()) {
            ListTag stacks = new ListTag();
            for (TransporterStack stack : transit) {
                CompoundTag tagCompound = new CompoundTag();
                stack.write(tagCompound);
                stacks.add(tagCompound);
            }
            nbtTags.put(NBTConstants.ITEMS, stacks);
        }
    }

    @Override
    public void takeShare() {
    }

    public double getCost() {
        return TransporterTier.ULTIMATE.getSpeed() / (double) tier.getSpeed();
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

    private boolean recalculate(int stackId, TransporterStack stack, BlockPos from) {
        //TODO: Why do we skip recalculating the path if it is idle. Is it possible for idle paths to eventually stop being idle or are they just idle forever??
        boolean noPath = stack.getPathType() == Path.NONE || stack.recalculatePath(TransitRequest.simple(stack.itemStack), this, 0).isEmpty();
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

    public <BE extends BlockEntity & IAdvancedTransportEjector> TransitResponse insertMaybeRR(@Nullable BE outputter, BlockPos outputterPos, TransitRequest request, @Nullable EnumColor color, boolean doEmit, int min) {
        if (outputter != null && outputter.getRoundRobin()) {
            return insert(outputter, outputterPos, request, color, min, doEmit, TransporterStack::recalculateRRPath);
        }
        return insert(outputter, outputterPos, request, color, doEmit, min);
    }

    public TransitResponse insert(@Nullable BlockEntity outputter, BlockPos outputterPos, TransitRequest request, @Nullable EnumColor color, boolean doEmit, int min) {
        return insert(outputter, outputterPos, request, color, min, doEmit, TransporterStack::recalculatePath);
    }

    private <BE extends BlockEntity> TransitResponse insert(@Nullable BE outputter, BlockPos outputterPos, TransitRequest request, @Nullable EnumColor color,
          int min, boolean doEmit, PathCalculator<BE> pathCalculator) {
        Direction from = WorldUtils.sideDifference(getBlockPos(), outputterPos);
        if (from != null && canReceiveFrom(from.getOpposite())) {
            TransporterStack stack = createInsertStack(outputterPos.immutable(), color);
            if (stack.canInsertToTransporterNN(this, from, outputter)) {
                return updateTransit(doEmit, stack, pathCalculator.calculate(stack, request, outputter, this, min, doEmit));
            }
        }
        return request.getEmptyResponse();
    }

    public TransitResponse insertUnchecked(BlockPos outputterPos, TransitRequest request, @Nullable EnumColor color, boolean doEmit, int min) {
        TransporterStack stack = createInsertStack(outputterPos, color);
        return updateTransit(doEmit, stack, stack.recalculatePath(request, this, min, doEmit));
    }

    public <BE extends BlockEntity> TransitResponse insertUnchecked(BE outputter, TransitRequest request, @Nullable EnumColor color, boolean doEmit, int min, PathCalculator<BE> pathCalculator) {
        TransporterStack stack = createInsertStack(outputter.getBlockPos(), color);
        return updateTransit(doEmit, stack, pathCalculator.calculate(stack, request, outputter, this, min, doEmit));
    }

    public TransporterStack createInsertStack(BlockPos outputterCoord, @Nullable EnumColor color) {
        TransporterStack stack = new TransporterStack();
        stack.originalLocation = outputterCoord;
        stack.homeLocation = outputterCoord;
        stack.color = color;
        return stack;
    }

    @NotNull
    private TransitResponse updateTransit(boolean doEmit, TransporterStack stack, TransitResponse response) {
        if (!response.isEmpty()) {
            stack.itemStack = response.getStack();
            if (doEmit) {
                int stackId = nextId++;
                addStack(stackId, stack);
                PacketUtils.sendToAllTracking(new PacketTransporterSync(getBlockPos(), stackId, stack), getTransmitterTile());
                getTransmitterTile().markForSave();
            }
        }
        return response;
    }

    private void entityEntering(TransporterStack stack, int progress) {
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
        // it's unlikely the data will be saved anyway (since chunks aren't saved until the end of
        // a tick).
    }

    @FunctionalInterface
    public interface PathCalculator<BE extends BlockEntity> {

        TransitResponse calculate(TransporterStack stack, TransitRequest request, BE outputter, LogisticalTransporterBase transporter, int min, boolean doEmit);
    }
}