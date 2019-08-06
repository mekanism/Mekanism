package mekanism.common.recipe.outputs;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemStackOutput extends MachineOutput<ItemStackOutput> {

    public ItemStack output = ItemStack.EMPTY;

    public ItemStackOutput(ItemStack stack) {
        output = stack;
    }

    public ItemStackOutput() {
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        output = new ItemStack(nbtTags.getCompoundTag("output"));
    }

    public boolean applyOutputs(NonNullList<ItemStack> inventory, int index, boolean doEmit) {
        ItemStack stack = inventory.get(index);
        if (stack.isEmpty()) {
            if (doEmit) {
                inventory.set(index, output.copy());
            }
            return true;
        } else if (ItemHandlerHelper.canItemStacksStack(stack, output) && stack.getCount() + output.getCount() <= stack.getMaxStackSize()) {
            if (doEmit) {
                stack.grow(output.getCount());
            }
            return true;
        }
        return false;
    }

    @Override
    public ItemStackOutput copy() {
        return new ItemStackOutput(output.copy());
    }
}