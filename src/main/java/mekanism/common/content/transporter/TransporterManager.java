package mekanism.common.content.transporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.content.transporter.TransitRequest.HashedItem;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;

public class TransporterManager {

    public static Map<Coord4D, Set<TransporterStack>> flowingStacks = new HashMap<>();

    public static void reset() {
        flowingStacks.clear();
    }

    public static void add(TransporterStack stack) {
        Set<TransporterStack> set = new HashSet<>();
        set.add(stack);

        if (flowingStacks.get(stack.getDest()) == null) {
            flowingStacks.put(stack.getDest(), set);
        } else {
            flowingStacks.get(stack.getDest()).addAll(set);
        }
    }

    public static void remove(TransporterStack stack) {
        if (stack.hasPath() && stack.pathType != Path.NONE) {
            flowingStacks.get(stack.getDest()).remove(stack);
        }
    }

    public static List<TransporterStack> getStacksToDest(Coord4D dest) {
        List<TransporterStack> ret = new ArrayList<>();

        if (flowingStacks.containsKey(dest)) {
            for (TransporterStack stack : flowingStacks.get(dest)) {
                if (stack != null && stack.pathType != Path.NONE && stack.hasPath()) {
                    if (stack.getDest().equals(dest)) {
                        ret.add(stack);
                    }
                }
            }
        }

        return ret;
    }


    private static ItemStack simulateInsert(IItemHandler handler, InventoryCopy copy, EnumFacing side, ItemStack stack) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (stack.isEmpty()) {
                // Nothing more to insert
                break;
            }

            // Make sure that the item is valid for the handler
            if (!handler.isItemValid(i, stack)) {
                continue;
            }

            // Simulate the insert; note that we can't depend solely on the "normal" simulate, since it would only tell us about
            // _this_ stack, not the cumulative set of stacks. Use our best guess about stacking/maxes to figure out
            // how the inventory would look after the insertion

            // Get the item stack for the slot in question
            ItemStack destStack = copy.inventory.get(i);

            // If the destination isn't empty and not stackable, move along
            if (!destStack.isEmpty() && !InventoryUtils.areItemsStackable(destStack, stack)) {
                continue;
            }

            // If the item stack is empty, we need to do a simulated insert since we can't tell if the stack
            // in question would be allowed in this slot. Otherwise, we depend on areItemsStackable to keep us
            // out of trouble
            if (destStack.isEmpty()) {
                // Simulate an insert;
                if (ItemStack.areItemStacksEqual(handler.insertItem(i, stack, true), stack)) {
                    // Insert will fail; bail
                    continue;
                }

                // Set the destStack to match ours
                destStack = stack.copy();
                destStack.setCount(0);
                copy.inventory.set(i, destStack);
            }

            int max = handler.getSlotLimit(i);
            if (max == 0) {
                continue;
            }

            int mergedCount = stack.getCount() + destStack.getCount();
            if (mergedCount > max) {
                // Not all the items will fit; put max in and save leftovers
                destStack.setCount(max);
                stack.setCount(mergedCount - max);
            } else {
                // All items will fit!
                destStack.grow(stack.getCount());
                stack.setCount(0);
            }
        }

        return stack;
    }

    public static boolean didEmit(ItemStack stack, ItemStack returned) {
        return returned.isEmpty() || returned.getCount() < stack.getCount();
    }

    public static ItemStack getToUse(ItemStack stack, ItemStack returned) {
        if (returned.isEmpty() || returned.getCount() == 0) {
            return stack;
        }

        return StackUtils.size(stack, stack.getCount() - returned.getCount());
    }

    /**
     * @return TransitResponse of expected items to use
     */
    public static TransitResponse getPredictedInsert(TileEntity tileEntity, EnumColor color, TransitRequest request,
          EnumFacing side) {

        // If the TE in question implements the mekanism interface, check that the color matches and bail
        // fast if it doesn't
        if (tileEntity instanceof ISideConfiguration) {
            ISideConfiguration config = (ISideConfiguration) tileEntity;
            EnumFacing tileSide = config.getOrientation();
            EnumColor configColor = config.getEjector()
                  .getInputColor(MekanismUtils.getBaseOrientation(side, tileSide).getOpposite());

            if (config.getEjector().hasStrictInput() && configColor != null && configColor != color) {
                return TransitResponse.EMPTY;
            }
        }

        // Before we see if this item can fit in the destination, we must first check the stacks that are
        // en-route. Note that we also have to simulate the current inventory after each stack; we'll make an
        // initial copy of the inventory and then simulate each in-flight addition. If any in-flight stack
        // can't be inserted, that we can fail fast.

        // Get the item handler for the TE; fail if it's not an item handler (and log for good measure --
        // there shouldn't be anything that's not an IItemHandler anymore)
        IItemHandler handler = InventoryUtils.getItemHandler(tileEntity, side.getOpposite());
        if (handler == null) {
            Mekanism.logger.error("Failed to predict insert; not an IItemHandler: {}", tileEntity);
            return TransitResponse.EMPTY;
        }

        InventoryCopy invCopy = new InventoryCopy(handler);

        // For each of the in-flight stacks, simulate their insert into the tile entity. Note that the invCopy
        // is updated each time
        for (TransporterStack s : getStacksToDest(Coord4D.get(tileEntity))) {
            ItemStack leftovers = simulateInsert(handler, invCopy, side, s.itemStack.copy());
            if (!leftovers.isEmpty()) {
                // Failed to successfully insert this in-flight item; there's no room for anyone else
                return TransitResponse.EMPTY;
            }
        }

        // Now for each of the items in the request, simulate the insert, using the state from all the in-flight
        // items to ensure we have an accurate model of what will happen in future. We try each stack in the
        // request; it might be possible to not send the first item, but the second could work, etc.
        for (Map.Entry<HashedItem, Pair<Integer, Map<Integer, Integer>>> requestEntry : request.getItemMap().entrySet()) {
            // Create a sending ItemStack with the hashed item type and total item count within the request
            ItemStack toSend = StackUtils.size(requestEntry.getKey().getStack(), requestEntry.getValue().getLeft());
            ItemStack leftovers = simulateInsert(handler, invCopy, side, toSend.copy());

            // If leftovers is unchanged from the simulation, there's no room at all; move on to the next stack
            if (ItemStack.areItemStacksEqual(leftovers, toSend)) {
                continue;
            }

            // Otherwise, construct the appropriately size stack to send and return that
            toSend.setCount(toSend.getCount() - leftovers.getCount());
            return new TransitResponse(toSend, requestEntry.getValue().getRight());
        }

        return TransitResponse.EMPTY;
    }

    private static class InventoryCopy {

        public NonNullList<ItemStack> inventory;

        public InventoryCopy(IItemHandler handler) {
            inventory = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
            for (int i = 0; i < handler.getSlots(); i++) {
                inventory.set(i, handler.getStackInSlot(i).copy());
            }
        }
    }
}
