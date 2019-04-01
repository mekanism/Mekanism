package mekanism.common.content.transporter;

import java.util.HashMap;
import java.util.Map;
import mekanism.common.Mekanism;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

/**
 * An InvStack attaches to an inventory and tracks a specific homogenous item in a variable amount
 * of slots. An InvStack can either track the entirety of these slots or portions of their contents.
 * Utility methods in this class allow for the strategic removal of clustered items from
 * inventories.
 * 
 * @author aidancbrady
 *
 */
public final class InvStack {

    /** The TileEntity owning the container this InvStack belongs to. */
    private final TileEntity tileEntity;

    /** The side of the inventory we are accessing with this InvStack. */
    private final EnumFacing side;

    /**
     * A map associating the slot IDs in consideration to the amount of items in those slots we care
     * about. Note that the associated item counts may not be the full count of items in the actual
     * inventory slots.
     */
    private Map<Integer, Integer> itemMap = new HashMap<>();

    /** The item type of this InvStack. Will be null until we have an insertion via appendStack. */
    private HashedItem itemType;

    /** The total amount of items tracked by this InvStack. */
    private int itemCount;

    public InvStack(TileEntity inv, EnumFacing facing) {
        tileEntity = inv;
        side = facing;
    }

    public InvStack(TileEntity inv, int slotID, ItemStack stack, EnumFacing facing) {
        this(inv, stack, getMap(slotID, stack), facing);
    }

    public InvStack(TileEntity inv, ItemStack stack, Map<Integer, Integer> idMap, EnumFacing facing) {
        tileEntity = inv;
        side = facing;
        itemMap = idMap;

        for (Map.Entry<Integer, Integer> entry : idMap.entrySet()) {
            appendStack(entry.getKey(), StackUtils.size(stack, entry.getValue()));
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
     * @param id - slot ID of items to track
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
    public void use(int amount) {
        if (!InventoryUtils.assertItemHandler("InvStack", tileEntity, side)) {
            return;
        }

        IItemHandler handler = InventoryUtils.getItemHandler(tileEntity, side);

        for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
            int toUse = Math.min(amount, entry.getValue());
            ItemStack ret = handler.extractItem(entry.getKey(), toUse, false);
            boolean stackable = InventoryUtils.areItemsStackable(itemType.getStack(), ret);

            if (!stackable || ret.getCount() != toUse) { // be loud if an InvStack's prediction doesn't line up
                Mekanism.logger.warn("An inventory's returned content " + (!stackable ? "type" : "count")
                        + " does not line up with InvStack's prediction.");

                Mekanism.logger.warn("InvStack item: " + itemType.getStack() + ", ret: " + ret);
                Mekanism.logger.warn("Tile: " + tileEntity + " " + tileEntity.getPos());
            }

            amount -= toUse;

            if (amount == 0) {
                return;
            }
        }
    }

    /**
     * Removes all the items being tracked by this InvStack.
     */
    public void use() {
        use(getStack().getCount());
    }

    private static Map<Integer, Integer> getMap(int slotID, ItemStack stack) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(slotID, stack.getCount());
        return map;
    }
}
