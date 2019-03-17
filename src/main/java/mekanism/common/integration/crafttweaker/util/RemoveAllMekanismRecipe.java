package mekanism.common.integration.crafttweaker.util;

import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;

public class RemoveAllMekanismRecipe<RECIPE extends MachineRecipe> extends RecipeMapModification<MachineInput, RECIPE> {

    public RemoveAllMekanismRecipe(String name, Recipe recipeType) {
        super(name, false, recipeType);
    }

    @Override
    public void apply() {
        //Don't move this into the constructor so that if an addon registers recipes late, we can still remove them
        recipes.putAll(map);
        super.apply();
    }

    @Override
    public String describe() {
        return "Removed all recipes for " + name;
    }
}