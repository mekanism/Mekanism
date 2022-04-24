package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(ChemicalDissolutionRecipe.class)
public class ChemicalDissolutionRecipeHandler extends MekanismRecipeHandler<ChemicalDissolutionRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, ChemicalDissolutionRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getItemInput(), recipe.getGasInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager manager, ChemicalDissolutionRecipe recipe, U o) {
        //Only support if the other is a dissolution recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof ChemicalDissolutionRecipe other) {
            return ingredientConflicts(recipe.getItemInput(), other.getItemInput()) &&
                   ingredientConflicts(recipe.getGasInput(), other.getGasInput());
        }
        return false;
    }
}