package mekanism.common.content.transporter;

import it.unimi.dsi.fastutil.ints.AbstractInt2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import mekanism.common.content.transporter.Finder.FirstFinder;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

public class TransitRequest {

    /**
     * Complicated map- associates item types with both total available item count and slot IDs and available item amounts for each slot.
     */
    private Map<HashedItem, Pair<Integer, AbstractInt2IntMap>> itemMap = new Object2ObjectOpenHashMap<>();

    public static TransitRequest getFromTransport(TransporterStack stack) {
        return getFromStack(stack.itemStack);
    }

    public static TransitRequest getFromStack(ItemStack stack) {
        TransitRequest ret = new TransitRequest();
        ret.addItem(stack, -1);
        return ret;
    }

    public static TransitRequest buildInventoryMap(TileEntity tile, Direction side, int amount) {
        return buildInventoryMap(tile, side, amount, new FirstFinder());
    }

    /**
     * Creates a TransitRequest based on a full inspection of an entire specified inventory from a given side. The algorithm will use the specified Finder to ensure the
     * resulting map will only capture desired items. The amount of each item type present in the resulting item type will cap at the given 'amount' parameter.
     *
     * @param side - the side from an adjacent connected inventory, *not* the inventory itself.
     */
    public static TransitRequest buildInventoryMap(TileEntity tile, Direction side, int amount, Finder finder) {
        TransitRequest ret = new TransitRequest();
        // so we can keep track of how many of each item type we have in this inventory mapping
        Object2IntOpenHashMap<HashedItem> itemCountMap = new Object2IntOpenHashMap<>();

        if (!InventoryUtils.assertItemHandler("TransitRequest", tile, side.getOpposite())) {
            return ret;
        }

        IItemHandler inventory = InventoryUtils.getItemHandler(tile, side.getOpposite());

        // count backwards- we start from the bottom of the inventory and go back for consistency
        for (int i = inventory.getSlots() - 1; i >= 0; i--) {
            ItemStack stack = inventory.extractItem(i, amount, true);

            if (!stack.isEmpty() && finder.modifies(stack)) {
                HashedItem hashed = new HashedItem(stack);
                int currentCount = itemCountMap.getOrDefault(hashed, -1);
                int toUse = currentCount != -1 ? Math.min(stack.getCount(), amount - currentCount) : stack.getCount();
                if (toUse == 0) {
                    continue; // continue if we don't need anymore of this item type
                }
                ret.addItem(StackUtils.size(stack, toUse), i);

                if (currentCount != -1) {
                    itemCountMap.put(hashed, currentCount + toUse);
                } else {
                    itemCountMap.put(hashed, toUse);
                }
            }
        }

        return ret;
    }

    public Map<HashedItem, Pair<Integer, AbstractInt2IntMap>> getItemMap() {
        return itemMap;
    }

    public boolean isEmpty() {
        return itemMap.isEmpty();
    }

    public void addItem(ItemStack stack, int slot) {
        HashedItem hashed = new HashedItem(stack);
        if (!itemMap.containsKey(hashed)) {
            AbstractInt2IntMap slotMap = new Int2IntOpenHashMap();
            slotMap.put(slot, stack.getCount());
            itemMap.put(hashed, Pair.of(stack.getCount(), slotMap));
        } else {
            Pair<Integer, AbstractInt2IntMap> itemInfo = itemMap.get(hashed);
            int count = itemInfo.getLeft() + stack.getCount();
            AbstractInt2IntMap slotMap = itemInfo.getRight();
            slotMap.put(slot, stack.getCount());
            itemMap.put(hashed, Pair.of(count, slotMap));
        }
    }

    public ItemStack getSingleStack() {
        return itemMap.keySet().iterator().next().getStack();
    }

    public boolean hasType(ItemStack stack) {
        return itemMap.keySet().stream().anyMatch(item -> InventoryUtils.areItemsStackable(stack, item.getStack()));
    }

    /**
     * A TransitResponse contains information regarding the partial ItemStacks which were allowed entry into a destination inventory. Note that a TransitResponse should
     * only contain a single item type, although it may be spread out across multiple slots.
     *
     * @author aidancbrady
     */
    public static class TransitResponse {

        public static final TransitResponse EMPTY = new TransitResponse();

        /** slot ID to item count map - this details how many items we will be pulling from each slot */
        private AbstractInt2IntMap idMap = new Int2IntOpenHashMap();
        private ItemStack toSend = ItemStack.EMPTY;

        private TransitResponse() {
        }

        public TransitResponse(ItemStack i, AbstractInt2IntMap slots) {
            toSend = i;

            // generate our ID/ItemStack map based on the amount of items we're sending
            int amount = getSendingAmount();
            for (Int2IntMap.Entry entry : slots.int2IntEntrySet()) {
                int toUse = Math.min(amount, entry.getIntValue());
                idMap.put(entry.getIntKey(), toUse);
                amount -= toUse;
                if (amount == 0) {
                    break;
                }
            }
        }

        public ItemStack getStack() {
            return toSend;
        }

        public int getSendingAmount() {
            return toSend.getCount();
        }

        public boolean isEmpty() {
            return this == EMPTY || idMap.isEmpty() || (toSend != null && toSend.isEmpty());
        }

        public ItemStack getRejected(ItemStack orig) {
            return StackUtils.size(orig, orig.getCount() - getSendingAmount());
        }

        public InvStack getInvStack(TileEntity tile, Direction side) {
            return new InvStack(tile, toSend, idMap, side);
        }
    }
}