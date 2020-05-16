package mekanism.common.content.transporter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import mekanism.common.Mekanism;
import mekanism.common.content.transporter.Finder.FirstFinder;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandler;

public class TransitRequest {

    private final TransitResponse EMPTY_RESPONSE = new TransitResponse();

    /**
     * Maps item types to respective inventory slot data.
     */
    private Map<HashedItem, SlotData> itemMap = new LinkedHashMap<>();

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

    public static TransitRequest buildInventoryMap(TileEntity tile, Direction side, int amount, Finder finder) {
        return buildInventoryMap(tile, side, 1, amount, finder);
    }

    /**
     * Creates a TransitRequest based on a full inspection of an entire specified inventory from a given side. The algorithm will use the specified Finder to ensure the
     * resulting map will only capture desired items. The amount of each item type present in the resulting item type will cap at the given 'amount' parameter.
     *
     * @param side - the side from an adjacent connected inventory, *not* the inventory itself.
     */
    public static TransitRequest buildInventoryMap(TileEntity tile, Direction side, int min, int max, Finder finder) {
        TransitRequest ret = new TransitRequest();
        IItemHandler inventory = InventoryUtils.assertItemHandler("TransitRequest", tile, side.getOpposite());
        if (inventory == null) {
            return ret;
        }

        // count backwards- we start from the bottom of the inventory and go back for consistency
        for (int i = inventory.getSlots() - 1; i >= 0; i--) {
            ItemStack stack = inventory.extractItem(i, max, true);

            if (!stack.isEmpty() && finder.modifies(stack)) {
                HashedItem hashed = new HashedItem(stack);
                int toUse = Math.min(stack.getCount(), max - ret.getCount(hashed));
                if (toUse == 0) {
                    continue; // continue if we don't need anymore of this item type
                }
                ret.addItem(StackUtils.size(stack, toUse), i);
            }
        }
        // remove items that we don't have enough of
        for (Iterator<Map.Entry<HashedItem, SlotData>> iter = ret.getItemMap().entrySet().iterator(); iter.hasNext();) {
            Map.Entry<HashedItem, SlotData> entry = iter.next();
            if (entry.getValue().getTotalCount() < min) {
                iter.remove();
            }
        }

        return ret;
    }

    public Map<HashedItem, SlotData> getItemMap() {
        return itemMap;
    }

    public boolean isEmpty() {
        return itemMap.isEmpty();
    }

    public void addItem(ItemStack stack, int slot) {
        HashedItem hashed = new HashedItem(stack);
        itemMap.computeIfAbsent(hashed, SlotData::new).addSlot(slot, stack);
    }

    public ItemStack getSingleStack() {
        return itemMap.keySet().iterator().next().getStack();
    }

    public boolean hasType(ItemStack stack) {
        return itemMap.keySet().stream().anyMatch(item -> InventoryUtils.areItemsStackable(stack, item.getStack()));
    }

    public int getCount(HashedItem type) {
        SlotData data = itemMap.get(type);
        return data != null ? data.getTotalCount() : 0;
    }

    public TransitResponse createResponse(ItemStack toSend, SlotData slotData) {
        return new TransitResponse(toSend, slotData);
    }

    public TransitResponse createSimpleResponse() {
        SlotData data = itemMap.values().stream().findFirst().orElse(null);
        return data != null ? createResponse(data.itemType.createStack(data.totalCount), data) : null;
    }

    public TransitResponse getEmptyResponse() {
        return EMPTY_RESPONSE;
    }

    /**
     * A TransitResponse contains information regarding the partial ItemStacks which were allowed entry into a destination inventory. Note that a TransitResponse should
     * only contain a single item type, although it may be spread out across multiple slots.
     *
     * @author aidancbrady
     */
    public class TransitResponse {

        private ItemStack toSend = ItemStack.EMPTY;

        private SlotData slotData;

        private TransitResponse() {
        }

        public TransitResponse(ItemStack toSend, SlotData slotData) {
            this.toSend = toSend;
            this.slotData = slotData;
        }

        public ItemStack getStack() {
            return toSend;
        }

        public int getSendingAmount() {
            return toSend.getCount();
        }

        public boolean use(int slot, int amount) {
            boolean empty = slotData.use(slot, amount);
            if (slotData.getTotalCount() == 0) {
                itemMap.remove(slotData.itemType);
            }
            return empty;
        }

        public boolean isEmpty() {
            return toSend.isEmpty() || slotData.getTotalCount() == 0;
        }

        public ItemStack getRejected(ItemStack orig) {
            return StackUtils.size(orig, orig.getCount() - getSendingAmount());
        }

        public void use(TileEntity tile, Direction side) {
            // fail fast if the response is empty
            if (isEmpty()) {
                return;
            }

            InvStack stack = new InvStack(tile, toSend, slotData.slotCountMap, side);
            stack.use(this);
        }

        public void removeSlot(int slot) {
            slotData.slotCountMap.remove(slot);
        }
    }

    /**
     * SlotData reflects slot count information for a unique item type within an inventory.
     *
     * @author aidancbrady
     */
    public static class SlotData {

        private HashedItem itemType;
        private int totalCount = 0;
        private Int2IntMap slotCountMap = new Int2IntOpenHashMap();

        public SlotData(HashedItem itemType) {
            this.itemType = itemType;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public int getSlotCount(int slot) {
            return slotCountMap.get(slot);
        }

        public void addSlot(int slot, ItemStack stack) {
            if (slotCountMap.containsKey(slot)) {
                Mekanism.logger.error("Attempted to track an already-tracked slot in a new TransitRequest.");
                Mekanism.logger.error("Item: " + stack.getDisplayName());
                return;
            }
            slotCountMap.put(slot, stack.getCount());
            totalCount += stack.getCount();
        }

        public boolean use(int slot, int amount) {
            int stored = getSlotCount(slot);
            totalCount -= amount;
            slotCountMap.put(slot, stored - amount);
            return stored == amount;
        }
    }
}