package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.api.recipes.IRecipeHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import net.minecraft.item.crafting.IRecipe;

@IRecipeHandler.For(ElectrolysisRecipe.class)
public class ElectrolysisRecipeHandler extends MekanismRecipeHandler<ElectrolysisRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, ElectrolysisRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getLeftGasOutputRepresentation(), recipe.getRightGasOutputRepresentation(),
              recipe.getEnergyMultiplier().equals(FloatingLong.ONE) ? SKIP_OPTIONAL_PARAM : recipe.getEnergyMultiplier());
    }

    @Override
    public <U extends IRecipe<?>> boolean doesConflict(IRecipeManager manager, ElectrolysisRecipe recipe, U other) {
        //Only support if the other is an electrolysis recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return other instanceof ElectrolysisRecipe && ingredientConflicts(recipe.getInput(), ((ElectrolysisRecipe) other).getInput());
    }
}