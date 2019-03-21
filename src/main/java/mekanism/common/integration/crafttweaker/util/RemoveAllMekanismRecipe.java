package mekanism.common.integration.crafttweaker.util;

import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.outputs.MachineOutput;

public class RemoveAllMekanismRecipe<INPUT extends MachineInput<INPUT>, OUTPUT extends MachineOutput<OUTPUT>, RECIPE extends MachineRecipe<INPUT, OUTPUT, RECIPE>> extends
      RecipeMapModification<INPUT, RECIPE> {

    public RemoveAllMekanismRecipe(String name, Recipe<INPUT, OUTPUT, RECIPE> recipeType) {
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