package mekanism.common.integration.crafttweaker.util;

import java.util.List;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.outputs.MachineOutput;

public class AddMekanismRecipe<INPUT extends MachineInput<INPUT>, OUTPUT extends MachineOutput<OUTPUT>, RECIPE extends MachineRecipe<INPUT, OUTPUT, RECIPE>> extends
      RecipeMapModification<INPUT, RECIPE> {

    public AddMekanismRecipe(String name, Recipe<INPUT, OUTPUT, RECIPE> recipeType, RECIPE recipe) {
        super(name, true, recipeType);
        recipes.put(recipe.getInput(), recipe);
    }

    public AddMekanismRecipe(String name, Recipe<INPUT, OUTPUT, RECIPE> recipeType, List<RECIPE> newRecipes) {
        super(name, true, recipeType);
        newRecipes.forEach(recipe -> recipes.put(recipe.getInput(), recipe));
    }
}