package mekanism.common.inventory.slot;

import mekanism.common.block.states.BlockStateMachine.MachineType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotPersonalChest extends Slot {

    public SlotPersonalChest(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        if (inventory.getStackInSlot(getSlotIndex()).isEmpty()) {
            return false;
        }

        return MachineType.get(inventory.getStackInSlot(getSlotIndex())) != MachineType.PERSONAL_CHEST;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if (MachineType.get(stack) == MachineType.PERSONAL_CHEST) {
            return false;
        }

        return super.isItemValid(stack);
    }
}
