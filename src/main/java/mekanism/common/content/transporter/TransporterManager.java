package mekanism.common.content.transporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

public class TransporterManager {

    private static Map<Coord4D, Set<TransporterStack>> flowingStacks = new HashMap<>();

    public static void reset() {
        flowingStacks.clear();
    }

    public static void add(TransporterStack stack) {
        flowingStacks.computeIfAbsent(stack.getDest(), k -> new HashSet<>()).add(stack);
    }

    public static void remove(TransporterStack stack) {
        if (stack.hasPath() && stack.getPathType() != Path.NONE) {
            flowingStacks.get(stack.getDest()).remove(stack);
        }
    }

    private static int simulateInsert(IItemHandler handler, InventoryInfo inventoryInfo, ItemStack stack, int count) {
        //IMPL NOTE: Make sure to set stack size back to the originalCount when returning from this method
        int originalCount = stack.getCount();
        if (count != originalCount) {
            //If we have a different count than actual count, set the count to the proper amount (allows for slightly reduced copying stacks about)
            stack.setCount(count);
        }
        for (int i = 0; i < handler.getSlots(); i++) {
            if (stack.isEmpty()) {
                // Nothing more to insert
                break;
            }

            int max = handler.getSlotLimit(i);
            //If no items are allowed in the slot, pass it up before checking anything about the items
            if (max == 0) {
                continue;
            }

            // Make sure that the item is valid for the handler
            if (!handler.isItemValid(i, stack)) {
                continue;
            }

            // Simulate the insert; note that we can't depend solely on the "normal" simulate, since it would only tell us about
            // _this_ stack, not the cumulative set of stacks. Use our best guess about stacking/maxes to figure out
            // how the inventory would look after the insertion

            // Number of items in the destination
            int destCount = inventoryInfo.stackSizes.get(i);

            // If the destination isn't empty and not stackable, move along
            if (destCount > 0 && !InventoryUtils.areItemsStackable(inventoryInfo.inventory.get(i), stack)) {
                continue;
            }

            // If the item stack is empty, we need to do a simulated insert since we can't tell if the stack
            // in question would be allowed in this slot. Otherwise, we depend on areItemsStackable to keep us
            // out of trouble
            else if (destCount == 0 && ItemStack.areItemStacksEqual(handler.insertItem(i, stack, true), stack)) {
                // Insert will fail; bail
                continue;
            }

            int mergedCount = count + destCount;
            if (mergedCount > max) {
                // Not all the items will fit; put max in and save leftovers
                inventoryInfo.stackSizes.set(i, max);
                count = mergedCount - max;
                stack.setCount(count);
            } else {
                // All items will fit; set the destination count as the new combined amount
                inventoryInfo.stackSizes.set(i, mergedCount);
                if (count != originalCount) {
                    //Set the stack size back to what it was when we got it
                    stack.setCount(originalCount);
                }
                return 0;
            }
        }
        if (count != originalCount) {
            //Set the stack size back to what it was when we got it
            stack.setCount(originalCount);
        }
        return count;
    }

    public static boolean didEmit(ItemStack stack, ItemStack returned) {
        return returned.isEmpty() || returned.getCount() < stack.getCount();
    }

    public static ItemStack getToUse(ItemStack stack, ItemStack returned) {
        return returned.isEmpty() ? stack : StackUtils.size(stack, stack.getCount() - returned.getCount());
    }

    /**
     * @return TransitResponse of expected items to use
     */
    public static TransitResponse getPredictedInsert(TileEntity tileEntity, EnumColor color, TransitRequest request, Direction side) {
        // If the TE in question implements the mekanism interface, check that the color matches and bail
        // fast if it doesn't
        if (tileEntity instanceof ISideConfiguration) {
            ISideConfiguration config = (ISideConfiguration) tileEntity;
            if (config.getEjector().hasStrictInput()) {
                Direction tileSide = config.getOrientation();
                EnumColor configColor = config.getEjector().getInputColor(MekanismUtils.getBaseOrientation(side, tileSide).getOpposite());
                if (configColor != null && configColor != color) {
                    return TransitResponse.EMPTY;
                }
            }
        }

        // Get the item handler for the TE; fail if it's not an item handler (and log for good measure --
        // there shouldn't be anything that's not an IItemHandler anymore)
        IItemHandler handler = InventoryUtils.getItemHandler(tileEntity, side.getOpposite());
        if (handler == null) {
            Mekanism.logger.error("Failed to predict insert; not an IItemHandler: {}", tileEntity);
            return TransitResponse.EMPTY;
        }

        // Before we see if this item can fit in the destination, we must first check the stacks that are
        // en-route. Note that we also have to simulate the current inventory after each stack; we'll keep
        // track of the initial size of the inventory and then simulate each in-flight addition. If any
        // in-flight stack can't be inserted, that we can fail fast.

        //Information about the inventory, keeps track of the size of a stack a slot will have, and
        // a cache of what getStackInSlot returns (as it has to call it anyways to get the stack size).
        // This cache allows potentially expensive getStackInSlot implementations to only have to be called
        // once instead of potentially many times.
        InventoryInfo inventoryInfo = new InventoryInfo(handler);

        //For each of the in-flight stacks, simulate their insert into the tile entity. Note that stackSizes
        // for inventoryInfo is updated each time
        Set<TransporterStack> transporterStacks = flowingStacks.get(Coord4D.get(tileEntity));
        if (transporterStacks != null) {
            for (TransporterStack stack : transporterStacks) {
                if (stack != null && stack.getPathType() != Path.NONE) {
                    if (simulateInsert(handler, inventoryInfo, stack.itemStack, stack.itemStack.getCount()) > 0) {
                        // Failed to successfully insert this in-flight item; there's no room for anyone else
                        return TransitResponse.EMPTY;
                    }
                }
            }
        }

        // Now for each of the items in the request, simulate the insert, using the state from all the in-flight
        // items to ensure we have an accurate model of what will happen in future. We try each stack in the
        // request; it might be possible to not send the first item, but the second could work, etc.
        for (Entry<HashedItem, Pair<Integer, Map<Integer, Integer>>> requestEntry : request.getItemMap().entrySet()) {
            // Create a sending ItemStack with the hashed item type and total item count within the request
            ItemStack stack = requestEntry.getKey().getStack();
            int numToSend = requestEntry.getValue().getLeft();
            //Directly pass the stack AND the actual amount we want, so that it does not need to copy the stack if there is no room
            int numLeftOver = simulateInsert(handler, inventoryInfo, stack, numToSend);

            // If leftovers is unchanged from the simulation, there's no room at all; move on to the next stack
            if (numLeftOver == numToSend) {
                continue;
            }

            // Otherwise, construct the appropriately size stack to send and return that
            return new TransitResponse(StackUtils.size(stack, numToSend - numLeftOver), requestEntry.getValue().getRight());
        }
        return TransitResponse.EMPTY;
    }

    private static class InventoryInfo {

        public NonNullList<ItemStack> inventory;
        public List<Integer> stackSizes = new ArrayList<>();

        public InventoryInfo(IItemHandler handler) {
            inventory = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                inventory.set(i, stack);
                stackSizes.add(stack.getCount());
            }
        }
    }
}