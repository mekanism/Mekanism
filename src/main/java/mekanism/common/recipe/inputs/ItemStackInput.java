package mekanism.common.recipe.inputs;

import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public class ItemStackInput extends MachineInput<ItemStackInput> {

    public ItemStack ingredient = ItemStack.EMPTY;
    private ItemStackInput wildVersion = null;
    private int ingredientHash;

    public ItemStackInput(ItemStack stack) {
        ingredient = stack;
        ingredientHash = hashIngredients();
    }

    public ItemStackInput() {
    }

    @Override
    public void load(NBTTagCompound nbtTags) {
        ingredient = new ItemStack(nbtTags.getCompoundTag("input"));
        ingredientHash = hashIngredients();
        wildVersion = null;
    }

    @Override
    public ItemStackInput copy() {
        return new ItemStackInput(ingredient.copy());
    }

    @Override
    public boolean isValid() {
        return !ingredient.isEmpty();
    }

    public ItemStackInput wildCopy() {
        if (wildVersion == null) {
            if (ingredient.getMetadata() != OreDictionary.WILDCARD_VALUE) {
                this.wildVersion = new ItemStackInput(
                      new ItemStack(ingredient.getItem(), ingredient.getCount(), OreDictionary.WILDCARD_VALUE));
            } else {
                this.wildVersion = this;
            }
        }
        return this.wildVersion;
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
        return StackUtils.equalsWildcardWithNBT(ingredient, other.ingredient);
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
