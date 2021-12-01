package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.api.recipes.IRecipeHandler;
import mekanism.api.recipes.SawmillRecipe;
import net.minecraft.item.crafting.IRecipe;

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
    public <U extends IRecipe<?>> boolean doesConflict(IRecipeManager manager, SawmillRecipe recipe, U other) {
        //Only support if the other is a sawmill recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return other instanceof SawmillRecipe && ingredientConflicts(recipe.getInput(), ((SawmillRecipe) other).getInput());
    }
}