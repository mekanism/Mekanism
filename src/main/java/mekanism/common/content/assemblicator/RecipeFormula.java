package mekanism.common.content.assemblicator;

import java.util.List;
import java.util.Objects;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.FormulaAttachment;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RecipeFormula {

    public final NonNullList<ItemStack> input = NonNullList.withSize(9, ItemStack.EMPTY);
    @Nullable
    public RecipeHolder<CraftingRecipe> recipe;
    private final CraftingContainer dummy = MekanismUtils.getDummyCraftingInv();

    public RecipeFormula() {
    }

    public RecipeFormula(Level world, FormulaAttachment attachment) {
        //Should always be 9 for the size
        for (int i = 0; i < 9; i++) {
            //Note: copyWithCount returns EMPTY if the stack is empty, so we can skip checking
            input.set(i, attachment.inventory().get(i).copyWithCount(1));
        }
        resetToRecipe();
        recipe = getRecipeFromGrid(dummy, world);
    }

    public RecipeFormula(Level world, List<IInventorySlot> craftingGridSlots) {
        //Should always be 9 for the size
        for (int i = 0; i < craftingGridSlots.size(); i++) {
            IInventorySlot craftingSlot = craftingGridSlots.get(i);
            //Note: copyWithCount returns EMPTY if the stack is empty, so we can skip checking
            input.set(i, craftingSlot.getStack().copyWithCount(1));
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
            dummy.setItem(i, craftingGridSlots.get(i).getStack().copyWithCount(1));
        }
        return recipe.value().matches(dummy, world);
    }

    //Must have matches be called before this and be true as it assumes that the dummy inventory was set by it
    public ItemStack assemble(RegistryAccess registryAccess) {
        return recipe == null ? ItemStack.EMPTY : recipe.value().assemble(dummy, registryAccess);
    }

    //Must have matches be called before this and be true as it assumes that the dummy inventory was set by it
    public NonNullList<ItemStack> getRemainingItems() {
        //Should never be null given the assumption matches is called first
        return recipe == null ? NonNullList.create() : recipe.value().getRemainingItems(dummy);
    }

    public boolean isIngredientInPos(Level world, ItemStack stack, int i) {
        if (recipe == null) {
            return false;
        } else if (stack.isEmpty()) {
            //If the stack being checked is empty but the input isn't expected to be empty,
            // mark it as not being correct for the position, but if it is expected to be empty,
            // mark it as being correct for the position
            return input.get(i).isEmpty();
        }
        ItemStack lastItem = input.get(i);
        if (lastItem.isEmpty()) {
            //We expect it to be empty, fail because it isn't
            return false;
        } else if (ItemStack.isSameItemSameComponents(stack, lastItem)) {
            //We are the same as the last item and the one we expect for that slot of the recipe
            return true;
        }

        resetToRecipe();
        dummy.setItem(i, stack);
        return recipe.value().matches(dummy, world);
    }

    public boolean isValidIngredient(Level world, ItemStack stack) {
        if (recipe != null) {
            for (ItemStack inputItem : input) {
                //Short circuit if it is one of the items we already know about
                if (!inputItem.isEmpty() && ItemStack.isSameItemSameComponents(inputItem, stack)) {
                    return true;
                }
            }
            resetToRecipe();
            for (int i = 0; i < 9; i++) {
                ItemStack inputItem = input.get(i);
                //Skip slots that aren't expected to be empty
                if (!inputItem.isEmpty()) {
                    dummy.setItem(i, stack);
                    if (recipe.value().matches(dummy, world)) {
                        return true;
                    }
                    dummy.setItem(i, inputItem);
                }
            }
        }
        return false;
    }

    public boolean isValidFormula() {
        return getRecipe() != null;
    }

    @Nullable
    public RecipeHolder<CraftingRecipe> getRecipe() {
        return recipe;
    }

    public boolean isFormulaEqual(RecipeFormula formula) {
        return Objects.equals(formula.getRecipe(), getRecipe());
    }

    public void setStack(Level world, int index, ItemStack stack) {
        input.set(index, stack);
        resetToRecipe();
        recipe = getRecipeFromGrid(dummy, world);
    }

    @Nullable
    private static RecipeHolder<CraftingRecipe> getRecipeFromGrid(CraftingContainer inv, Level world) {
        return MekanismRecipeType.getRecipeFor(RecipeType.CRAFTING, inv, world).orElse(null);
    }
}