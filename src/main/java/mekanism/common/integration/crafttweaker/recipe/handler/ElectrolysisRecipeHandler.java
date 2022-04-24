package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(ElectrolysisRecipe.class)
public class ElectrolysisRecipeHandler extends MekanismRecipeHandler<ElectrolysisRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, ElectrolysisRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition(),
              recipe.getEnergyMultiplier().equals(FloatingLong.ONE) ? SKIP_OPTIONAL_PARAM : recipe.getEnergyMultiplier());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager manager, ElectrolysisRecipe recipe, U o) {
        //Only support if the other is an electrolysis recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return o instanceof ElectrolysisRecipe other && ingredientConflicts(recipe.getInput(), other.getInput());
    }
}