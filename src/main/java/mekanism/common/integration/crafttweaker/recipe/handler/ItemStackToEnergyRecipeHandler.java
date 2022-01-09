package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(ItemStackToEnergyRecipe.class)
public class ItemStackToEnergyRecipeHandler extends MekanismRecipeHandler<ItemStackToEnergyRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, ItemStackToEnergyRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager manager, ItemStackToEnergyRecipe recipe, U o) {
        //Only support if the other is an itemstack to energy recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return o instanceof ItemStackToEnergyRecipe other && ingredientConflicts(recipe.getInput(), other.getInput());
    }
}