package mekanism.common.inventory.container.slot;

import mekanism.common.item.block.machine.ItemBlockPersonalChest;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

//TODO: Re-evaluate
public class SlotPersonalChest extends Slot {

    public SlotPersonalChest(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canTakeStack(PlayerEntity player) {
        ItemStack stackInSlot = inventory.getStackInSlot(getSlotIndex());
        return !stackInSlot.isEmpty() && !(stackInSlot.getItem() instanceof ItemBlockPersonalChest);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return !(stack.getItem() instanceof ItemBlockPersonalChest) && super.isItemValid(stack);
    }
}