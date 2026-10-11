package mekanism.common.inventory.slot;

import mekanism.api.inventory.IInventorySlot;
import mekanism.api.resource.LargeResourceStack;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record VanillaSlotAccess(IInventorySlot slot, boolean readOnly) implements SlotAccess {

    @Override
    public ItemStack get() {
        LargeResourceStack<ItemResource> stack = slot.asStack();
        return stack.resource().toStack(stack.amountAsInt());
    }

    @Override
    public boolean set(ItemStack stack) {
        if (readOnly) {
            return false;
        }
        slot.setContents(ItemResource.of(stack), stack.count(), null);
        return true;
    }
}