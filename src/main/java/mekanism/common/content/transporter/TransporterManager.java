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
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class TransporterManager {

    private TransporterManager() {
    }

    //todo determine if a custom pos record is better storing the long
    private static final Map<GlobalPos, Set<TransporterStack>> flowingStacks = new Object2ObjectOpenHashMap<>();

    public static void reset() {
        flowingStacks.clear();
    }

    public static void add(Level world, TransporterStack stack) {
        flowingStacks.computeIfAbsent(GlobalPos.of(world.dimension(), BlockPos.of(stack.getDest())), k -> new HashSet<>()).add(stack);
    }

    public static void remove(Level world, TransporterStack stack) {
        if (stack.hasPath() && stack.getPathType().hasTarget()) {
            GlobalPos pos = GlobalPos.of(world.dimension(), BlockPos.of(stack.getDest()));
            Set<TransporterStack> transporterStacks = flowingStacks.get(pos);
            if (transporterStacks != null && transporterStacks.remove(stack) && transporterStacks.isEmpty()) {
                flowingStacks.remove(pos);
            }
        }
    }

    /**
     * Gets the {@link TransitResponse} of what items we expect to be able to get used/inserted into the item handler at a given position, taking into account any already
     * "in-flight" items that are being transferred to the handler.
     *
     * @param position                Position of the target
     * @param side                    Side of the target we are connecting to
     * @param handler                 The item handler the target has
     * @param request                 Transit request
     * @param additionalFlowingStacks Any additional stacks to treat as flowing and in transit for simulation uses.
     *
     * @return {@link TransitResponse} of expected items to use
     */
    public static TransitResponse getPredictedInsert(GlobalPos position, Direction side, ResourceHandler<ItemResource> handler, TransitRequest request,
          Map<GlobalPos, Set<TransporterStack>> additionalFlowingStacks) {
        //TODO - 26.1: Can this method be called from a transactional context?
        try (Transaction simulation = Transaction.openRoot()) {
            //Before we see if this item can fit in the destination, we must first check the stacks that are
            // en-route. Note that we also have to simulate the current inventory after each stack; we'll keep
            // track of the initial size of the inventory and then simulate each in-flight addition. If any
            // in-flight stack can't be inserted, then we can fail fast.
            //Note: that stackSizes for inventoryInfo is updated each time
            if (!predictFlowing(position, side, handler, flowingStacks, simulation) || !predictFlowing(position, side, handler, additionalFlowingStacks, simulation)) {
                return request.getEmptyResponse();
            }

            //Now for each of the items in the request, simulate the insert, using the state from all the in-flight
            // items to ensure we have an accurate model of what will happen in the future.
            //For each of the items in the request, simulate the insert. We try each stack in the
            // request; it might be possible to not send the first item, but the second could work, etc.
            for (ItemData data : request) {
                ItemResource itemType = data.getItemType();
                int inserted = handler.insert(itemType, data.getTotalCount(), simulation);
                if (inserted > 0) {
                    //If we managed to insert any of the item, return a response for it
                    return request.createResponse(itemType, inserted, data);
                }
            }
            return request.getEmptyResponse();
        }
    }

    private static boolean predictFlowing(GlobalPos position, Direction side, ResourceHandler<ItemResource> handler, Map<GlobalPos, Set<TransporterStack>> flowingStacks,
          TransactionContext simulation) {
        Set<TransporterStack> transporterStacks = flowingStacks.get(position);
        if (transporterStacks != null) {
            for (TransporterStack stack : transporterStacks) {
                if (stack != null && stack.getPathType().hasTarget() && !stack.isEmpty()) {
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
                        //TODO - 26.1: Should we be failing here? Because what if we can't insert the apples that are en route, but can insert the carrots
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
        }
        return true;
    }
}