package mekanism.common.inventory.container.slot;

import mekanism.api.Action;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InsertableSlot extends Slot implements IInsertableSlot {

    public InsertableSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @NotNull
    @Override
    public ItemStack insertItem(@NotNull ItemStack stack, Action action) {
        if (stack.isEmpty() || !mayPlace(stack)) {
            //TODO: Should we even be checking isItemValid
            //"Fail quick" if the given stack is empty or we are not valid for the slot
            return stack;
        }
        ItemStack current = getItem();
        int needed = getMaxStackSize(stack) - current.getCount();
        if (needed <= 0) {
            //Fail if we are a full slot
            return stack;
        }
        if (current.isEmpty() || ItemStack.isSameItemSameComponents(current, stack)) {
            int toAdd = Math.min(stack.getCount(), needed);
            if (action.execute()) {
                //If we want to actually insert the item, then update the current item
                //Set the stack to our new stack (we have no simple way to increment the stack size) so we have to set it instead of being able to just grow it
                set(stack.copyWithCount(current.getCount() + toAdd));
            }
            return stack.copyWithCount(stack.getCount() - toAdd);
        }
        //If we didn't accept this item, then just return the given stack
        return stack;
    }
}