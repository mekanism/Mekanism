package mekanism.common.inventory.slot;

import mekanism.common.item.block.machine.ItemBlockPersonalChest;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotPersonalChest extends Slot {

    public SlotPersonalChest(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canTakeStack(PlayerEntity player) {
        if (inventory.getStackInSlot(getSlotIndex()).isEmpty()) {
            return false;
        }
        return !(inventory.getStackInSlot(getSlotIndex()).getItem() instanceof ItemBlockPersonalChest);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return !(stack.getItem() instanceof ItemBlockPersonalChest) && super.isItemValid(stack);
    }
}