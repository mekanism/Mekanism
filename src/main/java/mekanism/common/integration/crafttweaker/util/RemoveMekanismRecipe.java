package mekanism.common.integration.crafttweaker.util;

import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.utils.BaseMapRemoval;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.helpers.RecipeInfoHelper;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.outputs.MachineOutput;

import java.util.Map;

public class RemoveMekanismRecipe<INPUT extends MachineInput<INPUT>, OUTPUT extends MachineOutput<OUTPUT>, RECIPE extends MachineRecipe<INPUT, OUTPUT, RECIPE>> extends BaseMapRemoval<INPUT, RECIPE> {
    public RemoveMekanismRecipe(String name, Recipe recipeType, IngredientWrapper output, IngredientWrapper input) {
        super(name, recipeType.get());
        map.forEach((key, value) -> {
            if (IngredientHelper.matches(key, input) && IngredientHelper.matches(value.getOutput(), output)) {
                recipes.put(key, value);
            }
        });
        if (recipes.isEmpty()) {
            String warning = "";
            if (input.isEmpty()) {
                if (!output.isEmpty()) { //It should never be the case they both are empty but just in case they are ignore it
                    warning = String.format("output: '%s'", output.toString());
                }
            } else if (output.isEmpty()) {
                warning = String.format("input: '%s'", input.toString());
            } else {
                warning = String.format("input: '%s' and output: '%s'", input.toString(), output.toString());
            }
            if (!warning.isEmpty()) {
                LogHelper.logWarning(String.format("No %s recipe found for %s. Command ignored!", name, warning));
            }
        }
    }

    @Override
    protected String getRecipeInfo(Map.Entry<INPUT, RECIPE> recipe) {
        return RecipeInfoHelper.getRecipeInfo(recipe);
    }

    @Override
    public String describe() {
        //Only describe it if it didn't already throw a warning about no recipes found
        return recipes.isEmpty() ? "" : super.describe();
    }
}