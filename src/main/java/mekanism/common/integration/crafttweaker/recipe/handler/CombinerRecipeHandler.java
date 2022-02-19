package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.api.recipes.IRecipeHandler;
import mekanism.api.recipes.CombinerRecipe;
import net.minecraft.item.crafting.IRecipe;

@IRecipeHandler.For(CombinerRecipe.class)
public class CombinerRecipeHandler extends MekanismRecipeHandler<CombinerRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, CombinerRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getMainInput(), recipe.getExtraInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends IRecipe<?>> boolean doesConflict(IRecipeManager manager, CombinerRecipe recipe, U other) {
        //Only support if the other is a combiner recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (other instanceof CombinerRecipe) {
            CombinerRecipe otherRecipe = (CombinerRecipe) other;
            return ingredientConflicts(recipe.getMainInput(), otherRecipe.getMainInput()) &&
                   ingredientConflicts(recipe.getExtraInput(), otherRecipe.getExtraInput());
        }
        return false;
    }
}