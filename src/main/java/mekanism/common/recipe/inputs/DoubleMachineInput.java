package mekanism.common.recipe.inputs;

import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public class DoubleMachineInput extends MachineInput<DoubleMachineInput> {

    public ItemStack itemStack = ItemStack.EMPTY;
    public ItemStack extraStack = ItemStack.EMPTY;

    public DoubleMachineInput(ItemStack item, ItemStack extra) {
        itemStack = item;
        extraStack = extra;
    }

    public DoubleMachineInput() {
    }

    @Override
    public void load(NBTTagCompound nbtTags) {
        itemStack = new ItemStack(nbtTags.getCompoundTag("input"));
        extraStack = new ItemStack(nbtTags.getCompoundTag("extra"));
    }

    @Override
    public DoubleMachineInput copy() {
        return new DoubleMachineInput(itemStack.copy(), extraStack.copy());
    }

    @Override
    public boolean isValid() {
        return !itemStack.isEmpty() && !extraStack.isEmpty();
    }

    protected boolean useItemInternal(ItemStack stack, NonNullList<ItemStack> inventory, int index, boolean deplete) {
        if (inputContains(inventory.get(index), stack)) {
            if (deplete) {
                inventory.set(index, StackUtils.subtract(inventory.get(index), stack));
            }

            return true;
        }

        return false;
    }

    public boolean useItem(NonNullList<ItemStack> inventory, int index, boolean deplete) {
        return useItemInternal(itemStack, inventory, index, deplete);
    }

    public boolean useExtra(NonNullList<ItemStack> inventory, int index, boolean deplete) {
        return useItemInternal(extraStack, inventory, index, deplete);
    }

    public boolean matches(DoubleMachineInput input) {
        return StackUtils.equalsWildcard(itemStack, input.itemStack) && input.itemStack.getCount() >= itemStack
              .getCount()
              && StackUtils.equalsWildcard(extraStack, input.extraStack) && input.extraStack.getCount() >= extraStack
              .getCount();
    }

    @Override
    public int hashIngredients() {
        return StackUtils.hashItemStack(itemStack) ^ Integer.reverse(StackUtils.hashItemStack(extraStack));
    }

    @Override
    public boolean testEquality(DoubleMachineInput other) {
        if (!isValid()) {
            return !other.isValid();
        }

        return StackUtils.equalsWildcardWithNBT(itemStack, other.itemStack) && StackUtils
              .equalsWildcardWithNBT(extraStack, other.extraStack);
    }

    @Override
    public boolean isInstance(Object other) {
        return other instanceof DoubleMachineInput;
    }

    public DoubleMachineInput wildCopy() {
        return new DoubleMachineInput(
              new ItemStack(itemStack.getItem(), itemStack.getCount(), OreDictionary.WILDCARD_VALUE),
              new ItemStack(extraStack.getItem(), extraStack.getCount(), OreDictionary.WILDCARD_VALUE));
    }
}
