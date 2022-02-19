package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.api.recipes.IRecipeHandler;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import net.minecraft.item.crafting.IRecipe;

@IRecipeHandler.For(ItemStackToItemStackRecipe.class)
public class ItemStackToItemStackRecipeHandler extends MekanismRecipeHandler<ItemStackToItemStackRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, ItemStackToItemStackRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends IRecipe<?>> boolean doesConflict(IRecipeManager manager, ItemStackToItemStackRecipe recipe, U other) {
        //Only support if the other is an itemstack to itemstack recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return other instanceof ItemStackToItemStackRecipe && ingredientConflicts(recipe.getInput(), ((ItemStackToItemStackRecipe) other).getInput());
    }
}