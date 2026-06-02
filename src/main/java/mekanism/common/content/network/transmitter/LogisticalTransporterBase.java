package mekanism.common.content.network.transmitter;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import mekanism.api.SerializationConstants;
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
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.transmitter.PacketTransporterBatch;
import mekanism.common.network.to_client.transmitter.PacketTransporterSync;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.InventoryUtils.ItemDropper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public abstract class LogisticalTransporterBase extends Transmitter<ResourceHandler<ItemResource>, InventoryNetwork, LogisticalTransporterBase> {

    protected final Int2ObjectMap<TransporterStack> transit = new Int2ObjectOpenHashMap<>();
    protected final Int2ObjectMap<TransporterStack> needsSync = new Int2ObjectOpenHashMap<>();
    private final DroppedItems droppedItems = new DroppedItems();
    public final TransporterTier tier;
    protected int nextId = 0;
    protected int delay = 0;
    protected int delayCount = 0;
    private final Map<Direction, BlockCapabilityCache<ResourceHandler<ItemResource>, Direction>> capabilityCache = new EnumMap<>(Direction.class);
    private final Long2ReferenceMap<EnumMap<Direction, BlockCapabilityCache<ResourceHandler<ItemResource>, Direction>>> fallbackHandlerCache = new Long2ReferenceRBTreeMap<>();

    protected LogisticalTransporterBase(TileEntityTransmitter tile, TransporterTier tier) {
        super(tile, TransmissionType.ITEM);
        this.tier = tier;
    }

    @Nullable
    private ResourceHandler<ItemResource> getCapForSide(Direction logisticalSide) {
        BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> cache = capabilityCache.get(logisticalSide);
        if (cache == null) {
            cache = Capabilities.ITEM.createCache((ServerLevel) getLevel(), getBlockPos().relative(logisticalSide), logisticalSide.getOpposite(), this::isValid);
            capabilityCache.put(logisticalSide, cache);
        }
        return cache.getCapability();
    }

    @Nullable
    private ResourceHandler<ItemResource> getFallbackCapForSide(long pos, Direction handlerSide) {
        EnumMap<Direction, BlockCapabilityCache<ResourceHandler<ItemResource>, Direction>> sideCache = fallbackHandlerCache.computeIfAbsent(pos, _ -> new EnumMap<>(Direction.class));
        BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> cache = sideCache.get(handlerSide);
        if (cache == null) {
            cache = Capabilities.ITEM.createCache((ServerLevel) getLevel(), BlockPos.of(pos), handlerSide, this::isValid);
            sideCache.put(handlerSide, cache);
        }
        return cache.getCapability();
    }


    @Override
    protected AcceptorCache<ResourceHandler<ItemResource>> createAcceptorCache() {
        return new AcceptorCache<>(getTransmitterTile(), Capabilities.ITEM.block());
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
        InventoryNetwork network = getTransmitterNetwork();
        if (network != null) {
            //Pull items into the transporter
            if (delay > 0) {
                //If a delay has been imposed, wait a bit
                delay--;
            } else {
                //Reset delay to 3 ticks; if nothing is available to insert OR inserted, we'll try again in 3 ticks
                delay = 3;
                //Attempt to pull
                tryPull();
            }
            if (!transit.isEmpty()) {
                tickTransit(getLevel(), network);
            }
        }
    }

    private void tryPull() {
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (!isConnectionType(side, ConnectionType.PULL)) {
                continue;
            }
            ResourceHandler<ItemResource> inventory = getCapForSide(side);
            if (inventory != null) {
                try (Transaction transaction = Transaction.openRoot()) {
                    //TODO - 26.1: Ensure we aren't pulling more than max stack size at once(?)
                    TransitRequest request = TransitRequest.anyItem(inventory, tier.getPullAmount(), transaction);
                    //There's a stack available to insert into the network...
                    if (!request.isEmpty()) {
                        TransitResponse response = insert(null, getBlockPos().relative(side), request, getColor(), 0, transaction);
                        if (response.useAll(transaction)) {
                            //If the insert succeeded, remove the inserted count and try again in another 10 ticks
                            transaction.commit();
                            delay = MekanismUtils.TICKS_PER_HALF_SECOND;
                        } else {
                            //Either the insertion or extraction failed; increment the backoff and calculate delay. Note that we cap retries
                            // at a max of 40 ticks (2 seconds), which would be 4 consecutive retries
                            delayCount++;
                            delay = Math.min(2 * SharedConstants.TICKS_PER_SECOND, (int) Math.exp(delayCount));
                        }
                    }
                }
            }
        }
    }

    private void tickTransit(Level level, InventoryNetwork network) {
        long pos = getWorldPositionLong();
        //Update stack positions
        IntSet deletes = new IntOpenHashSet();
        //TODO - 26.1: Evaluate handling of this transaction
        try (Transaction transaction = Transaction.openRoot()) {
            //Note: Our calls to getTileEntity are not done with a chunkMap as we don't tend to have that many tiles we
            // are checking at once from here and given this gets called each tick, it would cause unnecessary garbage
            // collection to occur actually causing the tick time to go up slightly.
            for (ObjectIterator<Int2ObjectMap.Entry<TransporterStack>> iterator = Int2ObjectMaps.fastIterator(transit); iterator.hasNext(); ) {
                Int2ObjectMap.Entry<TransporterStack> entry = iterator.next();//don't store it anywhere
                int stackId = entry.getIntKey();
                TransporterStack stack = entry.getValue();
                if (!stack.initiatedPath) {//Initiate any paths and remove things that can't go places
                    if (stack.isEmpty() || !recalculate(stackId, stack, Long.MAX_VALUE, transaction)) {
                        deletes.add(stackId);
                        continue;
                    }
                }

                int prevProgress = stack.progress;
                stack.progress += tier.getSpeed();
                if (stack.progress >= 100) {
                    long prevSet = Long.MAX_VALUE;
                    if (stack.hasPath()) {
                        if (stack.getPath().getLong(0) == pos) { //Necessary for transition reasons, not sure why
                            deletes.add(stackId);
                            continue;
                        }
                        int currentIndex = stack.getPath().indexOf(pos);
                        long next = stack.getPath().getLong(currentIndex - 1);
                        if (next != Long.MAX_VALUE) {
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
                                //Get the handler we are trying to insert into from the network's acceptor cache
                                Direction side = stack.getSide(this).getOpposite();
                                ResourceHandler<ItemResource> acceptor = network.getCachedAcceptor(next, side);
                                if (acceptor == null && stack.getPathType().isHome()) {
                                    acceptor = getFallbackCapForSide(next, side);
                                }
                                TransitResponse response = TransitRequest.simple(stack).addToInventory(level, BlockPos.of(next), acceptor, 0,
                                      stack.getPathType().isHome(), transaction);
                                if (!response.isEmpty()) {
                                    //We were able to add at least part of the stack to the inventory
                                    int rejected = response.getRejected();
                                    if (rejected == 0) {
                                        //Nothing was rejected (it was all accepted); remove the stack from the prediction
                                        // tracker and schedule this stack for deletion. Continue the loop thereafter
                                        TransporterManager.remove(level, stack, transaction);
                                        deletes.add(stackId);
                                        continue;
                                    }
                                    //Some portion of the stack got rejected; save the remainder and
                                    // recalculate below to sort out what to do next
                                    stack.setStack(response.slotData().getItemType(), rejected);
                                }//else the entire stack got rejected (Note: we don't need to update the stack to point to itself)
                                prevSet = next;
                            }
                        }
                    }
                    if (!recalculate(stackId, stack, prevSet, transaction)) {
                        deletes.add(stackId);
                    } else if (prevSet == Long.MAX_VALUE) {
                        stack.progress = 50;
                    } else {
                        stack.progress = 0;
                    }
                } else if (prevProgress < 50 && stack.progress >= 50) {
                    boolean tryRecalculate;
                    if (stack.isFinal(this)) {
                        Path pathType = stack.getPathType();
                        if (pathType.hasTarget()) {
                            Direction side = stack.getSide(this);
                            ConnectionType connectionType = getConnectionType(side);
                            tryRecalculate = !connectionType.canSendTo() || !TransporterUtils.canInsert(level, BlockPos.of(stack.getDest()), stack.color,
                                  stack.getItemType(), stack.size(), side, pathType.isHome(), transaction);
                        } else {
                            //Try to recalculate idles once they reach their destination
                            tryRecalculate = true;
                        }
                    } else {
                        long nextPos = stack.getNext(this);
                        if (nextPos == Long.MAX_VALUE) {
                            tryRecalculate = true;
                        } else {
                            Direction nextSide = stack.getSide(getWorldPositionLong(), nextPos);
                            LogisticalTransporterBase nextTransmitter = network.getTransmitter(nextPos);
                            if (nextTransmitter == null && stack.getPathType().noTarget() && stack.getPath().size() == 2) {
                                //If there is no next transmitter, and it was an idle path, assume that we are idling
                                // in a single length transmitter, in which case we only recalculate it at 50 if it won't
                                // be able to go into that connection type
                                tryRecalculate = !getConnectionType(nextSide).canSendTo();
                            } else {
                                tryRecalculate = !stack.canInsertToTransporter(nextTransmitter, nextSide, this);
                            }
                        }
                    }
                    if (tryRecalculate && !recalculate(stackId, stack, Long.MAX_VALUE, transaction)) {
                        deletes.add(stackId);
                    }
                }
            }
            transaction.commit();
        }

        if (!deletes.isEmpty() || !needsSync.isEmpty()) {
            //Notify clients, so that we send the information before we start clearing our lists
            //Note: We have to copy needsSync so that it still has values when we clear the pending sync packets
            PacketUtils.sendToAllTracking(PacketTransporterBatch.create(pos, deletes, new Int2ObjectOpenHashMap<>(needsSync)), getTransmitterTile());
            // Now remove any entries from transit that have been deleted
            IntIterator deleteIterator = deletes.iterator();
            while (deleteIterator.hasNext()) {
                deleteStack(deleteIterator.nextInt());
            }

            // Clear the pending sync packets
            needsSync.clear();

            // Finally, mark chunk for save
            getTransmitterTile().markForSave();
        }
    }

    @Override
    public void remove() {
        super.remove();
        clearCapabilityCaches();
        if (!isRemote()) {
            for (TransporterStack stack : getTransit()) {
                TransporterManager.remove(getLevel(), stack, null);
            }
        }
    }

    @Override
    public void onWorldSeparate(boolean stillPresent) {
        super.onWorldSeparate(stillPresent);
        clearCapabilityCaches();//may still be "loaded" but caches will likely be invalidated
    }

    private void clearCapabilityCaches() {
        capabilityCache.clear();
        fallbackHandlerCache.clear();
    }

    @Override
    public void refreshConnections() {
        clearCapabilityCaches();
        super.refreshConnections();
    }

    @Override
    public InventoryNetwork createEmptyNetworkWithID(UUID networkID) {
        return new InventoryNetwork(networkID);
    }

    @Override
    public InventoryNetwork createNetworkByMerging(Collection<InventoryNetwork> networks) {
        return new InventoryNetwork(networks);
    }

    @Override
    public void writeReducedUpdatedTag(@NotNull ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        if (!transit.isEmpty()) {
            ValueOutputList itemOutputs = output.childrenList(SerializationConstants.ITEMS);
            for (ObjectIterator<Int2ObjectMap.Entry<TransporterStack>> iterator = Int2ObjectMaps.fastIterator(transit); iterator.hasNext(); ) {
                Int2ObjectMap.Entry<TransporterStack> entry = iterator.next();
                ValueOutput itemOutput = itemOutputs.addChild();
                itemOutput.putInt(SerializationConstants.INDEX, entry.getIntKey());
                entry.getValue().writeToUpdateTag(this, itemOutput);
            }
        }
    }

    @Override
    public boolean handleUpdateTag(@NotNull ValueInput input) {
        boolean refreshModelData = super.handleUpdateTag(input);
        transit.clear();
        ValueInputList itemInputs = input.childrenListOrEmpty(SerializationConstants.ITEMS);
        for (ValueInput itemInput : itemInputs) {
            //TODO - 26.1: How do we want to handle if the item input doesn't contain an index?
            addStack(itemInput.getIntOr(SerializationConstants.INDEX, 0), TransporterStack.readFromUpdate(itemInput));
        }
        return refreshModelData;
    }

    @Override
    public void read(@NotNull ValueInput input) {
        super.read(input);
        ValueInputList itemInputs = input.childrenListOrEmpty(SerializationConstants.ITEMS);
        for (ValueInput itemInput : itemInputs) {
            addStack(nextId++, TransporterStack.read(itemInput));
        }
    }

    @Override
    public void write(@NotNull ValueOutput output) {
        super.write(output);
        Collection<TransporterStack> transit = getTransit();
        if (!transit.isEmpty()) {
            ValueOutputList itemOutputs = output.childrenList(SerializationConstants.ITEMS);
            for (TransporterStack stack : transit) {
                stack.write(itemOutputs.addChild());
            }
        }
    }

    @Override
    public void takeShare(@Nullable TransactionContext transaction) {
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

    public void drop(TransporterStack stack, @Nullable TransactionContext transaction) {
        if (stack.isEmpty()) {
            //Skip any stacks that for some reason get passed to this with an empty method
            return;
        }
        int xOffset = 0, yOffset = 0, zOffset = 0;
        if (stack.hasPath()) {
            Vector3f pos = TransporterUtils.getStackPosition(this, stack, 0);
            xOffset = Mth.floor(pos.x());
            yOffset = Mth.floor(pos.y());
            zOffset = Mth.floor(pos.z());
        }
        try (Transaction subTransaction = Transaction.open(transaction)) {
            TransporterManager.remove(getLevel(), stack, subTransaction);
            droppedItems.addDrop(stack.getItemType(), stack.size(), xOffset, yOffset, zOffset, subTransaction);
            subTransaction.commit();
        }
    }

    private boolean recalculate(int stackId, TransporterStack stack, long from, @Nullable TransactionContext transaction) {
        //TODO: Why do we skip recalculating the path if it is idle. Is it possible for idle paths to eventually stop being idle or are they just idle forever??
        boolean noPath = stack.getPathType().noTarget() || stack.recalculatePath(TransitRequest.simple(stack), this, 0, transaction).isEmpty();
        if (noPath && !stack.calculateIdle(this, transaction)) {
            drop(stack, transaction);
            return false;
        }

        //Only add to needsSync if true is being returned; otherwise it gets added to deletes
        needsSync.put(stackId, stack);
        if (from != Long.MAX_VALUE) {
            stack.originalLocation = from;
        }
        return true;
    }

    public TransitResponse insert(@Nullable BlockEntity outputter, BlockPos outputterPos, TransitRequest request, @Nullable EnumColor color, int min, @Nullable TransactionContext transaction) {
        Direction from = WorldUtils.sideDifference(getBlockPos(), outputterPos);
        if (from != null && canReceiveFrom(from.getOpposite())) {
            TransporterStack stack = createInsertStack(outputterPos.asLong(), color);
            if (stack.canInsertToTransporter(this, from, outputter)) {
                if (outputter instanceof IAdvancedTransportEjector ejector && ejector.getRoundRobin()) {
                    return insert((BlockEntity & IAdvancedTransportEjector) outputter, request, stack, min, transaction, TransporterStack::recalculateRRPath);
                }
                return insert(outputter, request, stack, min, transaction, TransporterStack::recalculatePath);
            }
        }
        return TransitResponse.EMPTY;
    }

    public <BE extends BlockEntity> TransitResponse insertUnchecked(BE outputter, TransitRequest request, @Nullable EnumColor color, int min,
          @Nullable TransactionContext transaction, PathCalculator<BE> pathCalculator) {
        TransporterStack stack = createInsertStack(outputter.getBlockPos().asLong(), color);
        return insert(outputter, request, stack, min, transaction, pathCalculator);
    }

    public TransporterStack createInsertStack(long outputterCoord, @Nullable EnumColor color) {
        TransporterStack stack = new TransporterStack();
        stack.originalLocation = outputterCoord;
        stack.homeLocation = outputterCoord;
        stack.color = color;
        return stack;
    }

    public <BE extends BlockEntity> TransitResponse insert(BE outputter, TransitRequest request, TransporterStack stack, int min,
          @Nullable TransactionContext transaction, PathCalculator<BE> pathCalculator) {
        TransitResponse response = pathCalculator.calculate(stack, request, outputter, this, min, transaction);
        if (!response.isEmpty()) {
            stack.setStack(response.itemType(), response.sendingAmount());
            if (transaction == null) {
                //If there is no current transaction context, just directly emit the stack
                emitStack(stack);
            } else {
                //Otherwise, queue it to emit when the transaction chain finishes
                //TODO - 26.1: Test this
                RootCommitJournal onRootCommit = new RootCommitJournal(() -> emitStack(stack));
                onRootCommit.updateSnapshots(transaction);
            }
        }
        return response;
    }

    private void emitStack(TransporterStack stack) {
        int stackId = nextId++;
        addStack(stackId, stack);
        PacketUtils.sendToAllTracking(PacketTransporterSync.create(getWorldPositionLong(), stackId, stack), getTransmitterTile());
        getTransmitterTile().markForSave();
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

        TransitResponse calculate(TransporterStack stack, TransitRequest request, BE outputter, LogisticalTransporterBase transporter, int min, @Nullable TransactionContext transaction);
    }

    /// Based on [PlayerInventoryWrapper.DroppedItems]
    private class DroppedItems extends SnapshotJournal<Integer> {

        private static final ItemDropper<BlockPos> DROPPER = (lvl, pos, ignored, item) -> Block.popResource(lvl, pos, item);

        final Deque<DroppedItems.DropInfo> entries = new ArrayDeque<>();

        void addDrop(ItemResource resource, int amount, int xOffset, int yOffset, int zOffset, TransactionContext transaction) {
            updateSnapshots(transaction);
            entries.add(new DroppedItems.DropInfo(resource, amount, xOffset, yOffset, zOffset));
        }

        @Override
        protected Integer createSnapshot() {
            return entries.size();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            // effectively cancel dropping the stacks
            int previousSize = snapshot;
            while (entries.size() > previousSize) {
                entries.removeLast();
            }
        }

        @Override
        protected void onRootCommit(Integer originalState) {
            // actually drop the stacks
            // process elements of the queue one by one to avoid a CME if dropping the entity triggers more additions to the queue
            BlockPos blockPos = getBlockPos();
            Level level = getLevel();
            while (!entries.isEmpty()) {
                DroppedItems.DropInfo dropInfo = entries.removeFirst();
                BlockPos adjustedPos = blockPos.offset(dropInfo.xOffset(), dropInfo.yOffset(), dropInfo.zOffset());
                InventoryUtils.dropStack(level, adjustedPos, null, dropInfo.resource(), dropInfo.amount(), DROPPER);
            }
        }

        private record DropInfo(ItemResource resource, int amount, int xOffset, int yOffset, int zOffset) {
        }
    }
}