package mekanism.common.content.assemblicator;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class RecipeFormula {

    public NonNullList<ItemStack> input = NonNullList.withSize(9, ItemStack.EMPTY);
    public ICraftingRecipe recipe;
    private CraftingInventory dummy = MekanismUtils.getDummyCraftingInv();

    public RecipeFormula(World world, NonNullList<ItemStack> inv) {
        for (int i = 0; i < 9; i++) {
            input.set(i, StackUtils.size(inv.get(i), 1));
        }
        resetToRecipe();
        recipe = getRecipeFromGrid(dummy, world);
    }

    public RecipeFormula(World world, List<IInventorySlot> craftingGridSlots) {
        //Should always be 9 for the size
        for (int i = 0; i < craftingGridSlots.size(); i++) {
            IInventorySlot craftingSlot = craftingGridSlots.get(i);
            if (!craftingSlot.isEmpty()) {
                input.set(i, StackUtils.size(craftingSlot.getStack(), 1));
            }
        }
        resetToRecipe();
        recipe = getRecipeFromGrid(dummy, world);
    }

    private void resetToRecipe() {
        for (int i = 0; i < 9; i++) {
            dummy.setInventorySlotContents(i, input.get(i));
        }
    }

    public boolean matches(World world, List<IInventorySlot> craftingGridSlots) {
        //Should always be 9 for the size
        for (int i = 0; i < craftingGridSlots.size(); i++) {
            //TODO: Do we really need to be copying it here
            dummy.setInventorySlotContents(i, craftingGridSlots.get(i).getStack().copy());
        }
        return recipe.matches(dummy, world);
    }

    public boolean isIngredientInPos(World world, ItemStack stack, int i) {
        resetToRecipe();
        dummy.setInventorySlotContents(i, stack);
        return recipe.matches(dummy, world);
    }

    public boolean isIngredient(World world, ItemStack stack) {
        for (int i = 0; i < 9; i++) {
            dummy.setInventorySlotContents(i, stack);
            if (recipe.matches(dummy, world)) {
                return true;
            }
            dummy.setInventorySlotContents(i, input.get(i));
        }
        return false;
    }

    public List<Integer> getIngredientIndices(World world, ItemStack stack) {
        List<Integer> ret = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            dummy.setInventorySlotContents(i, stack);
            if (recipe.matches(dummy, world)) {
                ret.add(i);
            }
            dummy.setInventorySlotContents(i, input.get(i));
        }
        return ret;
    }

    public boolean isValidFormula(World world) {
        return getRecipe(world) != null;
    }

    public ICraftingRecipe getRecipe(World world) {
        return recipe;
    }

    public boolean isFormulaEqual(World world, RecipeFormula formula) {
        return formula.getRecipe(world) == getRecipe(world);
    }

    private static ICraftingRecipe getRecipeFromGrid(CraftingInventory inv, World world) {
        return world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, inv, world).get();
    }
}