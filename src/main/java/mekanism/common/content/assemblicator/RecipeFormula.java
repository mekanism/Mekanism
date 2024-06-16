package mekanism.common.content.assemblicator;

import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.FormulaAttachment;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record RecipeFormula(CraftingInput.Positioned craftingInput, @Nullable RecipeHolder<CraftingRecipe> recipe) {

    public static final RecipeFormula EMPTY = new RecipeFormula(CraftingInput.Positioned.EMPTY, null);

    public static RecipeFormula create(Level world, FormulaAttachment attachment) {
        //Should always be a 3x3 grid for the size
        return create(world, MekanismUtils.getCraftingInput(3, 3, attachment.inventory(), true));
    }

    public static RecipeFormula create(Level world, List<IInventorySlot> craftingGridSlots) {
        //Should always be a 3x3 grid for the size
        return create(world, MekanismUtils.getCraftingInputSlots(3, 3, craftingGridSlots, true));
    }

    public static RecipeFormula create(Level world, CraftingInput.Positioned craftingInput) {
        if (craftingInput.input().isEmpty()) {
            return EMPTY;
        }
        return new RecipeFormula(craftingInput, MekanismRecipeType.getRecipeFor(RecipeType.CRAFTING, craftingInput.input(), world).orElse(null));
    }

    public RecipeFormula withStack(Level world, int index, ItemStack stack) {
        if (isEmpty() && stack.isEmpty()) {
            return this;
        }
        List<ItemStack> copy = getCopy(false);
        ItemStack old = copy.set(index, stack);
        if (old != null && ItemStack.isSameItemSameComponents(old, stack)) {
            //Nothing changed, don't bother creating new objects
            //TODO: If there is a performance problem, try to optimize this to being above copying the list
            return this;
        }
        return create(world, CraftingInput.ofPositioned(3, 3, copy));
    }

    public ItemStack getInputStack(int slot) {
        if (isEmpty()) {
            return ItemStack.EMPTY;
        }
        int row = slot / 3;
        int column = slot % 3;
        CraftingInput input = craftingInput.input();
        if (row < craftingInput.top() || row >= craftingInput.top() + input.height() ||
            column < craftingInput.left() || column >= craftingInput.left() + input.width()) {
            return ItemStack.EMPTY;
        }
        return input.getItem(column - craftingInput.left(), row - craftingInput.top());
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public boolean valid() {
        return recipe != null;
    }

    public boolean matches(Level world, List<IInventorySlot> craftingGridSlots) {
        if (recipe == null) {
            return false;
        }
        //Should always be a 3x3 grid for the size
        return recipe.value().matches(MekanismUtils.getCraftingInputSlots(3, 3, craftingGridSlots, true).input(), world);
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

        List<ItemStack> dummy = getCopy(false);
        dummy.set(i, stack);
        return recipe.value().matches(CraftingInput.of(3, 3, dummy), world);
    }

    public boolean isValidIngredient(Level world, ItemStack stack) {
        if (recipe != null) {
            for (ItemStack inputItem : craftingInput.input().items()) {
                //Short circuit if it is one of the items we already know about
                if (!inputItem.isEmpty() && ItemStack.isSameItemSameComponents(inputItem, stack)) {
                    return true;
                }
            }
            List<ItemStack> dummy = getCopy(false);
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

    public List<ItemStack> getCopy(boolean copyStacks) {
        List<ItemStack> stacks = NonNullList.withSize(9, ItemStack.EMPTY);
        if (isEmpty()) {
            return stacks;
        }
        CraftingInput input = craftingInput.input();
        for (int row = 0; row < input.height(); row++) {
            int shiftedRow = 3 * (craftingInput.top() + row);
            for (int column = 0; column < input.width(); column++) {
                int index = shiftedRow + craftingInput.left() + column;
                ItemStack stack = input.getItem(column, row);
                stacks.set(index, copyStacks ? stack.copy() : stack);
            }
        }
        return stacks;
    }
}