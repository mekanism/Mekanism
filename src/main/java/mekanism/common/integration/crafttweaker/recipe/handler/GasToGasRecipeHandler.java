package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.api.recipes.IRecipeHandler;
import mekanism.api.recipes.GasToGasRecipe;
import net.minecraft.item.crafting.IRecipe;

@IRecipeHandler.For(GasToGasRecipe.class)
public class GasToGasRecipeHandler extends MekanismRecipeHandler<GasToGasRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, GasToGasRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends IRecipe<?>> boolean doesConflict(IRecipeManager manager, GasToGasRecipe recipe, U other) {
        //Only support if the other is a gas to gas recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return other instanceof GasToGasRecipe && ingredientConflicts(recipe.getInput(), ((GasToGasRecipe) other).getInput());
    }
}