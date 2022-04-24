package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(ItemStackToItemStackRecipe.class)
public class ItemStackToItemStackRecipeHandler extends MekanismRecipeHandler<ItemStackToItemStackRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, ItemStackToItemStackRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager manager, ItemStackToItemStackRecipe recipe, U o) {
        //Only support if the other is an itemstack to itemstack recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return o instanceof ItemStackToItemStackRecipe other && ingredientConflicts(recipe.getInput(), other.getInput());
    }
}