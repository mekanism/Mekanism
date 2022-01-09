package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import mekanism.api.recipes.GasToGasRecipe;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(GasToGasRecipe.class)
public class GasToGasRecipeHandler extends MekanismRecipeHandler<GasToGasRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, GasToGasRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager manager, GasToGasRecipe recipe, U o) {
        //Only support if the other is a gas to gas recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return o instanceof GasToGasRecipe other && ingredientConflicts(recipe.getInput(), other.getInput());
    }
}