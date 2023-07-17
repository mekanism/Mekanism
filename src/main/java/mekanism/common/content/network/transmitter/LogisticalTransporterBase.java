package mekanism.common.content.network.transmitter;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntConsumer;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.content.network.InventoryNetwork;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.network.to_client.PacketTransporterUpdate;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LogisticalTransporterBase extends Transmitter<IItemHandler, InventoryNetwork, LogisticalTransporterBase> {

    protected final Int2ObjectMap<TransporterStack> transit = new Int2ObjectOpenHashMap<>();
    protected final Int2ObjectMap<TransporterStack> needsSync = new Int2ObjectOpenHashMap<>();
    public final TransporterTier tier;
    private long lastRenderUpdate;
    protected int nextId = 0;
    protected int delay = 0;
    protected int delayCount = 0;

    protected LogisticalTransporterBase(TileEntityTransmitter tile, TransporterTier tier) {
        super(tile, TransmissionType.ITEM);
        this.tier = tier;
    }

    @Override
    public AcceptorCache<IItemHandler> getAcceptorCache() {
        //Cast it here to make things a bit easier, as we know createAcceptorCache by default returns an object of type AcceptorCache
        return (AcceptorCache<IItemHandler>) super.getAcceptorCache();
    }

    @Override
    public boolean handlesRedstone() {
        return false;
    }

    public boolean exposesInsertCap(@NotNull Direction side) {
        ConnectionType connectionType = getConnectionTypeRaw(side);
        return connectionType == ConnectionType.NORMAL || connectionType == ConnectionType.PULL;
    }

    public EnumColor getColor() {
        return null;
    }

    public boolean canEmitTo(Direction side) {
        return canConnect(side) && getConnectionType(side).canEmit();
    }

    public boolean canReceiveFrom(Direction side) {
        return canConnect(side) && getConnectionType(side).canReceive();
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

    @Override
    public boolean isValidAcceptor(BlockEntity tile, Direction side) {
        return super.isValidAcceptor(tile, side) && getAcceptorCache().isAcceptorAndListen(tile, side, ForgeCapabilities.ITEM_HANDLER);
    }

    public void onUpdateClient() {
        //TODO: For this to actually appear smooth, this assumes that packets with updates won't be delayed
        // even though they may be by at least one tick. Which then can cause it to jitter especially at high speeds
        long time = getTileWorld().getGameTime();
        //TODO: Compare against game time and if game time hasn't progressed then don't actually increment this. TEST THIS
        if (lastRenderUpdate != time) {
            //TODO: If it has been more than one tick do we want to progress multiple pieces at once?
            lastRenderUpdate = time;
            for (TransporterStack stack : transit.values()) {
                stack.progress = Math.min(100, stack.progress + tier.getSpeed());
            }
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
                for (Direction side : getConnections(ConnectionType.PULL)) {
                    BlockEntity tile = WorldUtils.getTileEntity(getTileWorld(), getTilePos().relative(side));
                    if (tile != null) {
                        TransitRequest request = TransitRequest.anyItem(tile, side.getOpposite(), tier.getPullAmount());
                        //There's a stack available to insert into the network...
                        if (!request.isEmpty()) {
                            TransitResponse response = insert(tile, request, getColor(), true, 0);
                            if (response.isEmpty()) {
                                //Insert failed; increment the backoff and calculate delay. Note that we cap retries
                                // at a max of 40 ticks (2 seconds), which would be 4 consecutive retries
                                delayCount++;
                                delay = Math.min(40, (int) Math.exp(delayCount));
                            } else {
                                //If the insert succeeded, remove the inserted count and try again for another 10 ticks
                                response.useAll();
                                delay = 10;
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
                    if (!stack.initiatedPath) {//Initiate any paths and remove things that can't go places
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
                            //TODO: In theory we could and potentially replace this with stack#getNext, but we could also
                            // instead take advantage of having the index to skip a lookup check for calculating stack#isFinal
                            int currentIndex = stack.getPath().indexOf(getTilePos());
                            if (currentIndex == 0) { //Necessary for transition reasons, not sure why
                                //TODO: Potential reason for why is so it removes it when inserting it into a block?
                                // as it seems to only be index 0 when it is length zero OR actually also potentially
                                // when it is just the first transmitter it was in.
                                //TODO: Which realistically means that this is only really needed to add explicitly to deletes
                                // if there is no next
                                //TODO: Wait actually current index == 0 means that this tile is the destination, but can transporters
                                // be the destination of a path? Maybe especially for when it is idling
                                deletes.add(stackId);
                                continue;
                            }
                            //TODO: If speed is configured in such a way that progress might be >= 200 we should potentially try to make it
                            // skip over the intermediary transporters
                            BlockPos next = stack.getPath().get(currentIndex - 1);
                            if (next != null) {
                                if (!stack.isFinal(this)) {
                                    //If this is not the final transporter try transferring it to the next one
                                    LogisticalTransporterBase transmitter = network.getTransmitter(next);
                                    if (stack.canInsertToTransporter(transmitter, stack.getSide(this), this)) {
                                        transmitter.entityEntering(stack, stack.progress % 100);
                                        deletes.add(stackId);
                                        continue;
                                    }
                                    prevSet = next;
                                } else if (stack.getPathType().hasTarget()) {
                                    //Otherwise, try to insert it into the destination inventory
                                    BlockEntity tile = WorldUtils.getTileEntity(getTileWorld(), next);
                                    if (tile != null) {
                                        TransitResponse response = TransitRequest.simple(stack.itemStack).addToInventory(tile, stack.getSide(this), 0,
                                              stack.getPathType().isHome());
                                        if (!response.isEmpty()) {
                                            //We were able to add at least part of the stack to the inventory
                                            ItemStack rejected = response.getRejected();
                                            if (rejected.isEmpty()) {
                                                //Nothing was rejected (it was all accepted); remove the stack from the prediction
                                                // tracker and schedule this stack for deletion. Continue the loop thereafter
                                                TransporterManager.remove(getTileWorld(), stack);
                                                deletes.add(stackId);
                                                continue;
                                            }
                                            //Some portion of the stack got rejected; save the remainder and
                                            // recalculate below to sort out what to do next
                                            stack.itemStack = rejected;
                                        }//else the entire stack got rejected (Note: we don't need to update the stack to point to itself)
                                        prevSet = next;
                                    }
                                }
                            }
                        }
                        //TODO: Re-evaluate prevSet being set to next in places? I think it potentially should be our position rather than the next one???
                        if (!recalculate(stackId, stack, prevSet)) {
                            deletes.add(stackId);
                        } else if (prevSet == null) {
                            stack.progress = 50;
                        } else {
                            stack.progress = 0;
                        }
                    } else if (prevProgress < 50 && stack.progress >= 50) {
                        //If the stack has transitioned past the halfway point
                        //TODO: Finish above comment
                        boolean tryRecalculate;
                        if (stack.isFinal(this)) {
                            //TODO: If this is the final transporter: ??
                            Path pathType = stack.getPathType();
                            if (pathType.hasTarget()) {
                                Direction side = stack.getSide(this);
                                ConnectionType connectionType = getConnectionType(side);
                                tryRecalculate = !connectionType.canEmit() || !TransporterUtils.canInsert(WorldUtils.getTileEntity(getTileWorld(), stack.getDest()),
                                      stack.color, stack.itemStack, side, pathType.isHome());
                            } else {
                                //Try to recalculate idles once they reach their destination
                                tryRecalculate = true;
                            }
                        } else {
                            BlockPos nextPos = stack.getNext(this);
                            Direction nextSide = WorldUtils.sideDifference(nextPos, getTilePos());
                            if (nextSide == null) {//TODO: RE-EVALUATE in theory only would happen if they aren't actually sequential or if the next pos is this pos?
                                nextSide = Direction.DOWN;
                            }
                            LogisticalTransporterBase nextTransmitter = network.getTransmitter(nextPos);
                            //TODO: In theory because we already have next and know which side of progress it is on we could simplify the getSide logic
                            // which is what we now are doing a few lines above
                            if (nextTransmitter == null && !stack.getPathType().hasTarget() && stack.getPath().size() == 2) {
                                //If there is no next transmitter, and it was an idle path, assume that we are idling
                                // in a single length transmitter, in which case we only recalculate it at 50 if it won't
                                // be able to go into that connection type
                                tryRecalculate = !getConnectionType(nextSide).canEmit();
                            } else {
                                tryRecalculate = !stack.canInsertToTransporter(nextTransmitter, nextSide, this);
                            }
                        }
                        if (tryRecalculate && !recalculate(stackId, stack, null)) {
                            deletes.add(stackId);
                        }
                    }
                }

                //TODO: Needs sync gets set on neighboring ones not on this, so re-evaluate how exactly we want to handle this
                // also should we remove any entries in delete from needs sync potentially
                // (aka see if there are cases when there may be overlap)
                if (!deletes.isEmpty() || !needsSync.isEmpty()) {
                    //Notify clients, so that we send the information before we start clearing our lists
                    Mekanism.packetHandler().sendToAllTracking(new PacketTransporterUpdate(this, needsSync, deletes), getTransmitterTile());
                    // Now remove any entries from transit that have been deleted
                    deletes.forEach((IntConsumer) (this::deleteStack));

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
                TransporterManager.remove(getTileWorld(), stack);
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
        boolean noPath = !stack.getPathType().hasTarget() || stack.recalculatePath(TransitRequest.simple(stack.itemStack), this, 0).isEmpty();
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

    public TransitResponse insert(BlockEntity outputter, TransitRequest request, @Nullable EnumColor color, boolean doEmit, int min) {
        return insert(outputter, request, color, doEmit, stack -> stack.recalculatePath(request, this, min, doEmit));
    }

    public TransitResponse insertRR(TileEntityLogisticalSorter outputter, TransitRequest request, @Nullable EnumColor color, boolean doEmit, int min) {
        return insert(outputter, request, color, doEmit, stack -> stack.recalculateRRPath(request, outputter, this, min, doEmit));
    }

    private TransitResponse insert(BlockEntity outputter, TransitRequest request, @Nullable EnumColor color, boolean doEmit,
          Function<TransporterStack, TransitResponse> pathCalculator) {
        BlockPos outputterPos = outputter.getBlockPos();
        Direction from = WorldUtils.sideDifference(getTilePos(), outputterPos);
        if (from != null && canReceiveFrom(from.getOpposite())) {
            TransporterStack stack = createInsertStack(outputterPos, color);
            if (stack.canInsertToTransporterNN(this, from, outputter)) {
                return updateTransit(doEmit, stack, pathCalculator.apply(stack));
            }
        }
        return request.getEmptyResponse();
    }

    public TransitResponse insertUnchecked(BlockPos outputterPos, TransitRequest request, @Nullable EnumColor color, boolean doEmit, int min) {
        return insertUnchecked(outputterPos, color, doEmit, stack -> stack.recalculatePath(request, this, min, doEmit));
    }

    private TransitResponse insertUnchecked(BlockPos outputterPos, @Nullable EnumColor color, boolean doEmit, Function<TransporterStack, TransitResponse> pathCalculator) {
        TransporterStack stack = createInsertStack(outputterPos, color);
        return updateTransit(doEmit, stack, pathCalculator.apply(stack));
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
                Mekanism.packetHandler().sendToAllTracking(new PacketTransporterUpdate(this, stackId, stack), getTransmitterTile());
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
}