package mekanism.common.inventory.container.slot;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.common.util.StackUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class InsertableSlot extends Slot implements IInsertableSlot {

    public InsertableSlot(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return Math.min(getSlotStackLimit(), stack.getMaxStackSize());
    }

    @Nonnull
    @Override
    public ItemStack insertItem(@Nonnull ItemStack stack, Action action) {
        if (stack.isEmpty() || !isItemValid(stack)) {
            //TODO: Should we even be checking isItemValid
            //"Fail quick" if the given stack is empty or we are not valid for the slot
            return stack;
        }
        ItemStack current = getStack();
        int needed = getItemStackLimit(stack) - current.getCount();
        if (needed <= 0) {
            //Fail if we are a full slot
            return stack;
        }
        if (current.isEmpty() || ItemHandlerHelper.canItemStacksStack(current, stack)) {
            int toAdd = Math.min(stack.getCount(), needed);
            if (action.execute()) {
                //If we want to actually insert the item, then update the current item
                //Set the stack to our new stack (we have no simple way to increment the stack size) so we have to set it instead of being able to just grow it
                putStack(StackUtils.size(stack, current.getCount() + toAdd));
            }
            return StackUtils.size(stack, stack.getCount() - toAdd);
        }
        //If we didn't accept this item, then just return the given stack
        return stack;
    }
}