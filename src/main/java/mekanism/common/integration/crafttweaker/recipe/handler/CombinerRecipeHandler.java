package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import mekanism.api.recipes.CombinerRecipe;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(CombinerRecipe.class)
public class CombinerRecipeHandler extends MekanismRecipeHandler<CombinerRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, CombinerRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getMainInput(), recipe.getExtraInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager manager, CombinerRecipe recipe, U o) {
        //Only support if the other is a combiner recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof CombinerRecipe other) {
            return ingredientConflicts(recipe.getMainInput(), other.getMainInput()) &&
                   ingredientConflicts(recipe.getExtraInput(), other.getExtraInput());
        }
        return false;
    }
}