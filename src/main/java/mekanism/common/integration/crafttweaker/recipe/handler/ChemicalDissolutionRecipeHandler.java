package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.api.recipes.IRecipeHandler;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import net.minecraft.item.crafting.IRecipe;

@IRecipeHandler.For(ChemicalDissolutionRecipe.class)
public class ChemicalDissolutionRecipeHandler extends MekanismRecipeHandler<ChemicalDissolutionRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, ChemicalDissolutionRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getItemInput(), recipe.getGasInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends IRecipe<?>> boolean doesConflict(IRecipeManager manager, ChemicalDissolutionRecipe recipe, U other) {
        //Only support if the other is a dissolution recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (other instanceof ChemicalDissolutionRecipe) {
            ChemicalDissolutionRecipe otherRecipe = (ChemicalDissolutionRecipe) other;
            return ingredientConflicts(recipe.getItemInput(), otherRecipe.getItemInput()) &&
                   ingredientConflicts(recipe.getGasInput(), otherRecipe.getGasInput());
        }
        return false;
    }
}