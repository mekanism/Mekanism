package mekanism.common.content.transporter;

import java.util.HashSet;
import java.util.Set;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import mekanism.common.Mekanism;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandler;

/**
 * An InvStack attaches to an inventory and tracks a specific homogeneous item in a variable amount of slots. An InvStack can either track the entirety of these slots or
 * portions of their contents. Utility methods in this class allow for the strategic removal of clustered items from inventories.
 *
 * @author aidancbrady
 */
public final class InvStack {

    /** The TileEntity owning the container this InvStack belongs to. */
    private final TileEntity tile;

    /** The side of the inventory we are accessing with this InvStack. */
    private final Direction side;

    /**
     * A map associating the slot IDs in consideration to the amount of items in those slots we care about. Note that the associated item counts may not be the full count
     * of items in the actual inventory slots.
     */
    private Int2IntMap itemMap = new Int2IntOpenHashMap();

    /** The item type of this InvStack. Will be null until we have an insertion via appendStack. */
    private HashedItem itemType;

    /** The total amount of items tracked by this InvStack. */
    private int itemCount;

    public InvStack(TileEntity inv, Direction facing) {
        tile = inv;
        side = facing;
    }

    public InvStack(TileEntity inv, int slotID, ItemStack stack, Direction facing) {
        this(inv, stack, getMap(slotID, stack), facing);
    }

    public InvStack(TileEntity inv, ItemStack stack, Int2IntMap idMap, Direction facing) {
        tile = inv;
        side = facing;
        itemMap = idMap;
        for (Int2IntMap.Entry entry : idMap.int2IntEntrySet()) {
            appendStack(entry.getIntKey(), StackUtils.size(stack, entry.getIntValue()));
        }
    }

    /**
     * Gets an ItemStack of the defined type and total item count tracked by this InvStack.
     *
     * @return the total ItemStack contents of this InvStack
     */
    public ItemStack getStack() {
        if (itemType == null || itemCount == 0) {
            return ItemStack.EMPTY;
        }
        return StackUtils.size(itemType.getStack(), itemCount);
    }

    /**
     * Appends a slot ID and specified ItemStack count to the tracked map of this InvStack.
     *
     * @param id    - slot ID of items to track
     * @param stack - an ItemStack representing the count of items to track in the given slot ID
     */
    public void appendStack(int id, ItemStack stack) {
        if (itemType == null) {
            itemType = new HashedItem(stack);
        }
        itemMap.put(id, stack.getCount());
        itemCount += stack.getCount();
    }

    /**
     * Removes a specified amount of items from the parent inventory.
     *
     * @param amount - the amount of items to remove
     */
    private void use(int amount, TransitResponse response) {
        IItemHandler handler = InventoryUtils.assertItemHandler("InvStack", tile, side);
        if (handler != null) {
            Set<Integer> emptySlots = new HashSet<>();
            for (Int2IntMap.Entry entry : itemMap.int2IntEntrySet()) {
                int toUse = Math.min(amount, entry.getIntValue());
                ItemStack ret = handler.extractItem(entry.getIntKey(), toUse, false);
                boolean stackable = InventoryUtils.areItemsStackable(itemType.getStack(), ret);
                if (!stackable || ret.getCount() != toUse) { // be loud if an InvStack's prediction doesn't line up
                    Mekanism.logger.warn("An inventory's returned content " + (!stackable ? "type" : "count") + " does not line up with InvStack's prediction.");
                    Mekanism.logger.warn("InvStack item: " + itemType.getStack() + ", ret: " + ret);
                    Mekanism.logger.warn("Tile: " + tile + " " + tile.getPos());
                }
                amount -= toUse;

                // update the SlotData to reflect the new item count
                // this change will be reflected in the original TransitRequest as well
                if (response != null && response.use(entry.getIntKey(), toUse)) {
                    emptySlots.add(entry.getIntKey());
                }
                if (amount == 0) {
                    return;
                }
            }
            emptySlots.forEach(i -> response.removeSlot(i));
        }
    }

    public void use(TransitResponse response) {
        use(response.getSendingAmount(), response);
    }

    public void useAll() {
        use(getStack().getCount(), null);
    }

    private static Int2IntMap getMap(int slotID, ItemStack stack) {
        Int2IntMap map = new Int2IntOpenHashMap();
        map.put(slotID, stack.getCount());
        return map;
    }
}