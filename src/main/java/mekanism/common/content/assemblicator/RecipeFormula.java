package mekanism.common.content.assemblicator;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class RecipeFormula {

    public final NonNullList<ItemStack> input = NonNullList.withSize(9, ItemStack.EMPTY);
    @Nullable
    public CraftingRecipe recipe;
    private final CraftingContainer dummy = MekanismUtils.getDummyCraftingInv();

    public RecipeFormula(Level world, NonNullList<ItemStack> inv) {
        for (int i = 0; i < 9; i++) {
            input.set(i, StackUtils.size(inv.get(i), 1));
        }
        resetToRecipe();
        recipe = getRecipeFromGrid(dummy, world);
    }

    public RecipeFormula(Level world, List<IInventorySlot> craftingGridSlots) {
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

    public ItemStack getInputStack(int slot) {
        return input.get(slot);
    }

    private void resetToRecipe() {
        for (int i = 0; i < 9; i++) {
            dummy.setItem(i, input.get(i));
        }
    }

    public boolean matches(Level world, List<IInventorySlot> craftingGridSlots) {
        if (recipe == null) {
            return false;
        }
        //Should always be 9 for the size
        for (int i = 0; i < craftingGridSlots.size(); i++) {
            dummy.setItem(i, StackUtils.size(craftingGridSlots.get(i).getStack(), 1));
        }
        return recipe.matches(dummy, world);
    }

    //Must have matches be called before this and be true as it assumes that the dummy inventory was set by it
    public ItemStack assemble() {
        return recipe == null ? ItemStack.EMPTY : recipe.assemble(dummy);
    }

    //Must have matches be called before this and be true as it assumes that the dummy inventory was set by it
    public NonNullList<ItemStack> getRemainingItems() {
        //Should never be null given the assumption matches is called first
        return recipe == null ? NonNullList.create() : recipe.getRemainingItems(dummy);
    }

    public boolean isIngredientInPos(Level world, ItemStack stack, int i) {
        if (recipe == null) {
            return false;
        } else if (stack.isEmpty() && !input.get(i).isEmpty()) {
            //If the stack being checked is empty but the input isn't expected to be empty,
            // mark it as not being correct for the position
            return false;
        }
        resetToRecipe();
        dummy.setItem(i, stack);
        return recipe.matches(dummy, world);
    }

    public IntList getIngredientIndices(Level world, ItemStack stack) {
        IntList ret = new IntArrayList();
        if (recipe != null) {
            for (int i = 0; i < 9; i++) {
                dummy.setItem(i, stack);
                if (recipe.matches(dummy, world)) {
                    ret.add(i);
                }
                dummy.setItem(i, input.get(i));
            }
        }
        return ret;
    }

    public boolean isValidFormula() {
        return getRecipe() != null;
    }

    @Nullable
    public CraftingRecipe getRecipe() {
        return recipe;
    }

    public boolean isFormulaEqual(RecipeFormula formula) {
        return formula.getRecipe() == getRecipe();
    }

    public void setStack(Level world, int index, ItemStack stack) {
        input.set(index, stack);
        resetToRecipe();
        recipe = getRecipeFromGrid(dummy, world);
    }

    @Nullable
    private static CraftingRecipe getRecipeFromGrid(CraftingContainer inv, Level world) {
        return MekanismRecipeType.getRecipeFor(RecipeType.CRAFTING, inv, world).orElse(null);
    }
}