package mekanism.common.recipe.inputs;

import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;

public class ItemStackInput extends MachineInput<ItemStackInput> {

    public ItemStack ingredient = ItemStack.EMPTY;
    private int ingredientHash;

    public ItemStackInput(ItemStack stack) {
        ingredient = stack;
        ingredientHash = hashIngredients();
    }

    public ItemStackInput() {
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        ingredient = ItemStack.read(nbtTags.getCompound("input"));
        ingredientHash = hashIngredients();
    }

    @Override
    public ItemStackInput copy() {
        return new ItemStackInput(ingredient.copy());
    }

    @Override
    public boolean isValid() {
        return !ingredient.isEmpty();
    }

    public boolean useItemStackFromInventory(NonNullList<ItemStack> inventory, int index, boolean deplete) {
        if (inputContains(inventory.get(index), ingredient)) {
            if (deplete) {
                inventory.set(index, StackUtils.subtract(inventory.get(index), ingredient));
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashIngredients() {
        return StackUtils.hashItemStack(ingredient);
    }

    @Override
    public boolean testEquality(ItemStackInput other) {
        return MachineInput.inputItemMatches(ingredient, other.ingredient);
    }

    @Override
    public boolean isInstance(Object other) {
        return other instanceof ItemStackInput;
    }

    @Override
    public int hashCode() {
        return ingredientHash;
    }
}