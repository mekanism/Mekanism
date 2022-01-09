package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import mekanism.api.recipes.SawmillRecipe;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(SawmillRecipe.class)
public class SawmillRecipeHandler extends MekanismRecipeHandler<SawmillRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, SawmillRecipe recipe) {
        //Note: We take advantage of the fact that if we have a recipe we have at least one output and that we can skip parameters
        // as if they were optional
        boolean hasSecondary = recipe.getSecondaryChance() > 0;
        return buildCommandString(manager, recipe, recipe.getInput(),
              recipe.getMainOutputDefinition().isEmpty() ? SKIP_OPTIONAL_PARAM : recipe.getMainOutputDefinition(),
              hasSecondary ? recipe.getSecondaryOutputDefinition() : SKIP_OPTIONAL_PARAM,
              hasSecondary ? recipe.getSecondaryChance() : SKIP_OPTIONAL_PARAM
        );
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager manager, SawmillRecipe recipe, U o) {
        //Only support if the other is a sawmill recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return o instanceof SawmillRecipe other && ingredientConflicts(recipe.getInput(), other.getInput());
    }
}