package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.api.recipes.IRecipeHandler;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import net.minecraft.item.crafting.IRecipe;

@IRecipeHandler.For(ItemStackToEnergyRecipe.class)
public class ItemStackToEnergyRecipeHandler extends MekanismRecipeHandler<ItemStackToEnergyRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, ItemStackToEnergyRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends IRecipe<?>> boolean doesConflict(IRecipeManager manager, ItemStackToEnergyRecipe recipe, U other) {
        //Only support if the other is an itemstack to energy recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return other instanceof ItemStackToEnergyRecipe && ingredientConflicts(recipe.getInput(), ((ItemStackToEnergyRecipe) other).getInput());
    }
}