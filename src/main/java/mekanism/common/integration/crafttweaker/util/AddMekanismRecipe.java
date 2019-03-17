package mekanism.common.integration.crafttweaker.util;

import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;

public class AddMekanismRecipe extends RecipeMapModification<MachineInput, MachineRecipe> {

    public AddMekanismRecipe(String name, Recipe recipeType, MachineRecipe recipe) {
        super(name, true, recipeType);
        recipes.put(recipe.getInput(), recipe);
    }
}