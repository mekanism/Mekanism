package mekanism.common.content.assemblicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.FormulaAttachment;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RecipeFormula {

    public final CraftingInput craftingInput;
    @Nullable
    public RecipeHolder<CraftingRecipe> recipe;

    public RecipeFormula() {
        //TODO - 1.21: Re-evaluate this
        craftingInput = CraftingInput.EMPTY;
    }

    public RecipeFormula(Level world, FormulaAttachment attachment) {
        //Should always be a 3x3 grid for the size
        craftingInput = MekanismUtils.getCraftingInput(3, 3, attachment.inventory(), true);
        recipe = getRecipeFromGrid(craftingInput, world);
    }

    public RecipeFormula(Level world, CraftingInput craftingInput) {
        this.craftingInput = craftingInput;
        recipe = getRecipeFromGrid(this.craftingInput, world);
    }

    public RecipeFormula(Level world, List<IInventorySlot> craftingGridSlots) {
        //Should always be a 3x3 grid for the size
        craftingInput = MekanismUtils.getCraftingInputSlots(3, 3, craftingGridSlots, true);
        recipe = getRecipeFromGrid(craftingInput, world);
    }

    public ItemStack getInputStack(int slot) {
        return craftingInput.getItem(slot);
    }

    public boolean matches(Level world, List<IInventorySlot> craftingGridSlots) {
        if (recipe == null) {
            return false;
        }
        //Should always be a 3x3 grid for the size
        return recipe.value().matches(MekanismUtils.getCraftingInputSlots(3, 3, craftingGridSlots, true), world);
    }

    //Must have matches be called before this and be true as it assumes that the dummy inventory was set by it
    public ItemStack assemble(RegistryAccess registryAccess) {
        return recipe == null ? ItemStack.EMPTY : recipe.value().assemble(craftingInput, registryAccess);
    }

    //Must have matches be called before this and be true as it assumes that the dummy inventory was set by it
    public NonNullList<ItemStack> getRemainingItems() {
        //Should never be null given the assumption matches is called first
        return recipe == null ? NonNullList.create() : recipe.value().getRemainingItems(craftingInput);
    }

    public boolean isIngredientInPos(Level world, ItemStack stack, int i) {
        if (recipe == null) {
            return false;
        } else if (stack.isEmpty()) {
            //If the stack being checked is empty but the input isn't expected to be empty,
            // mark it as not being correct for the position, but if it is expected to be empty,
            // mark it as being correct for the position
            return getInputStack(i).isEmpty();
        }
        ItemStack lastItem = getInputStack(i);
        if (lastItem.isEmpty()) {
            //We expect it to be empty, fail because it isn't
            return false;
        } else if (ItemStack.isSameItemSameComponents(stack, lastItem)) {
            //We are the same as the last item and the one we expect for that slot of the recipe
            return true;
        }

        List<ItemStack> dummy = new ArrayList<>(craftingInput.items());
        dummy.set(i, stack);
        return recipe.value().matches(CraftingInput.of(3, 3, dummy), world);
    }

    public boolean isValidIngredient(Level world, ItemStack stack) {
        if (recipe != null) {
            for (ItemStack inputItem : craftingInput.items()) {
                //Short circuit if it is one of the items we already know about
                if (!inputItem.isEmpty() && ItemStack.isSameItemSameComponents(inputItem, stack)) {
                    return true;
                }
            }
            List<ItemStack> dummy = new ArrayList<>(craftingInput.items());
            for (int i = 0; i < 9; i++) {
                ItemStack inputItem = dummy.get(i);
                //Skip slots that aren't expected to be empty
                if (!inputItem.isEmpty()) {
                    dummy.set(i, stack);
                    if (recipe.value().matches(CraftingInput.of(3, 3, dummy), world)) {
                        return true;
                    }
                    dummy.set(i, inputItem);
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

    //TODO - 1.21: Re-evaluate this entire class, in particular this with stack stuff. Should we be copying stacks when we do dummy based stuff?
    public RecipeFormula withStack(Level world, int index, ItemStack stack) {
        List<ItemStack> copy = new ArrayList<>(craftingInput.items());
        copy.set(index, stack);
        return new RecipeFormula(world, CraftingInput.of(3, 3, copy));
    }

    @Nullable
    private static RecipeHolder<CraftingRecipe> getRecipeFromGrid(CraftingInput input, Level world) {
        return MekanismRecipeType.getRecipeFor(RecipeType.CRAFTING, input, world).orElse(null);
    }
}