package mekanism.common.inventory;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.item.ItemStack;

public class InventoryPersonalChest extends ItemStackMekanismInventory {

    public InventoryPersonalChest(ItemStack stack) {
        super(stack);
    }

    @Override
    protected List<IInventorySlot> getInitialInventory() {
        List<IInventorySlot> slots = new ArrayList<>();
        for (int slotY = 0; slotY < 6; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                //Note: we allow access to the slots from all sides as long as it is public, unlike in 1.12 where we always denied the bottom face
                // We did that to ensure that things like hoppers that could check IInventory did not bypass any restrictions
                slots.add(BasicInventorySlot.at(this, 8 + slotX * 18, 26 + slotY * 18));
            }
        }
        return slots;
    }
}