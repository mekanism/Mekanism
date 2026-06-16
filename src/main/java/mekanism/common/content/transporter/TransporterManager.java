package mekanism.common.content.transporter;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.ItemData;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class TransporterManager {

    private TransporterManager() {
    }

    //todo determine if a custom pos record is better storing the long
    private static final Map<GlobalPos, FlowingJournal> flowingStacks = new Object2ObjectOpenHashMap<>();

    public static void reset() {
        flowingStacks.clear();
    }

    public static void add(Level world, TransporterStack stack, @Nullable TransactionContext transaction) {
        flowingStacks.computeIfAbsent(GlobalPos.of(world.dimension(), BlockPos.of(stack.getDest())), FlowingJournal::new).add(stack, transaction);
    }

    public static void remove(Level world, TransporterStack stack, @Nullable TransactionContext transaction) {
        if (stack.hasPath() && stack.getPathType().hasTarget()) {
            GlobalPos pos = GlobalPos.of(world.dimension(), BlockPos.of(stack.getDest()));
            FlowingJournal flowing = flowingStacks.get(pos);
            if (flowing != null && flowing.remove(stack, transaction)) {
                flowingStacks.remove(pos);
            }
        }
    }

    /// Gets the [TransitResponse] of what items we expect to be able to get used/inserted into the item handler at a given position, taking into account any already
    /// "in-flight" items that are being transferred to the handler.
    ///
    /// @param position Position of the target
    /// @param side     Side of the target we are connecting to
    /// @param handler  The item handler the target has
    /// @param request  Transit request
    ///
    /// @return [TransitResponse] of expected items to use
    public static TransitResponse getPredictedInsert(GlobalPos position, Direction side, ResourceHandler<ItemResource> handler, TransitRequest request,
          @Nullable TransactionContext transaction) {
        try (Transaction simulation = Transaction.open(transaction)) {
            //Before we see if this item can fit in the destination, we must first check the stacks that are
            // en-route. Note that we also have to simulate the current inventory after each stack; we'll keep
            // track of the initial size of the inventory and then simulate each in-flight addition. If any
            // in-flight stack can't be inserted, then we can fail fast.
            FlowingJournal flowing = flowingStacks.get(position);
            if (flowing == null || predictFlowing(flowing, side, handler, simulation)) {
                //Now for each of the items in the request, simulate the insert, using the state from all the in-flight
                // items to ensure we have an accurate model of what will happen in the future.
                //For each of the items in the request, simulate the insert. We try each stack in the
                // request; it might be possible to not send the first item, but the second could work, etc.
                for (ItemData data : request) {
                    int count = data.getTotalCount();
                    if (count > 0) {
                        ItemResource itemType = data.getItemType();
                        int inserted = handler.insert(itemType, count, simulation);
                        if (inserted > 0) {
                            //If we managed to insert any of the item, return a response for it
                            return request.createResponse(itemType, inserted, data);
                        }
                    }
                }
            }
            return TransitResponse.EMPTY;
        }
    }

    private static boolean predictFlowing(FlowingJournal flowing, Direction side, ResourceHandler<ItemResource> handler, TransactionContext simulation) {
        for (TransporterStack stack : flowing.stacks) {
            if (!stack.isEmpty() && stack.getPathType().hasTarget()) {
                //We start by simulating inserting the stack into the handler, regardless of if we
                // are interacting with the same side of the target as the stack's path is taking.
                // This is so that in cases where the item handler is shared (chests) or some of
                // the slots of the item handler may be shared (our machines with multiple sides
                // set to the same side config are "different" because of the side proxies) then
                // we want to make sure we try to insert the in-flight stacks anyway so that if
                // the slot is the same we fill it.
                int toInsert = stack.size();
                int inserted = handler.insert(stack.getItemType(), toInsert, simulation);
                if (inserted < toInsert) {
                    //TODO - 26.2: Should we be failing here? Because what if we can't insert the apples that are en route, but can insert the carrots
                    if (inserted == 0) {
                        //If none of the stack could be inserted, check if we are attempting to insert it
                        // into the same side as the side we are predicting that we can insert into.
                        if (side != stack.getSideOfDest()) {
                            //If we are not, then assume that the destination does not contain the slot that the in-flight
                            // stack is en-route to at all, so don't exit early just because of failing to insert it into
                            // the destination, and instead continue checking and then simulate/check our TransitRequest
                            continue;
                        }
                    }
                    // Failed to successfully insert this in-flight item; there's no room for anyone else
                    return false;
                }
            }
        }
        return true;
    }

    private static class FlowingJournal extends SnapshotJournal<Set<TransporterStack>> {

        private final GlobalPos pos;
        //TODO - 26.2: Should this be some sort of queue, or should we do reverting similar to how it works for dropped items?
        private Set<TransporterStack> stacks = new HashSet<>();

        public FlowingJournal(GlobalPos pos) {
            this.pos = pos;
        }

        private void add(TransporterStack stack, @Nullable TransactionContext transaction) {
            //TODO - 26.2: If we are in a transaction, but aren't passed one, then this might get rolled back by revert?
            // Is this a worry we might have? Same goes for removal
            if (transaction != null) {
                updateSnapshots(transaction);
            }
            stacks.add(stack);
        }

        private boolean remove(TransporterStack stack, @Nullable TransactionContext transaction) {
            if (transaction != null) {
                updateSnapshots(transaction);
            }
            return stacks.remove(stack) && stacks.isEmpty();
        }

        @Override
        protected Set<TransporterStack> createSnapshot() {
            return new HashSet<>(stacks);
        }

        @Override
        protected void revertToSnapshot(Set<TransporterStack> snapshot) {
            this.stacks = snapshot;
        }

        @Override
        protected void onRootCommit(Set<TransporterStack> originalState) {
            super.onRootCommit(originalState);
            //If there are no elements stored when we are done with this transaction, then remove the flowing journal for this position from the overall flowing map
            if (stacks.isEmpty()) {
                flowingStacks.remove(pos);
            }
        }
    }
}