package mekanism.common.integration.crafttweaker.util;

import crafttweaker.CraftTweakerAPI;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.outputs.MachineOutput;

public class RemoveMekanismRecipe<INPUT extends MachineInput<INPUT>, OUTPUT extends MachineOutput<OUTPUT>, RECIPE extends MachineRecipe<INPUT, OUTPUT, RECIPE>> extends
      RecipeMapModification<INPUT, RECIPE> {

    private final IngredientWrapper input;
    private final IngredientWrapper output;

    public RemoveMekanismRecipe(String name, Recipe<INPUT, OUTPUT, RECIPE> recipeType, IngredientWrapper output,
          IngredientWrapper input) {
        super(name, false, recipeType);
        this.input = input;
        this.output = output;
    }

    @Override
    public void apply() {
        //Don't move this into the constructor so that if an addon registers recipes late, we can still remove them
        map.forEach((key, value) -> {
            if (IngredientHelper.matches(key, input) && IngredientHelper.matches(value.getOutput(), output)) {
                recipes.put(key, value);
            }
        });
        if (recipes.isEmpty()) {
            String warning = "";
            if (input.isEmpty()) {
                if (!output
                      .isEmpty()) { //It should never be the case they both are empty but just in case they are ignore it
                    warning = String.format("output: '%s'", output.toString());
                }
            } else if (output.isEmpty()) {
                warning = String.format("input: '%s'", input.toString());
            } else {
                warning = String.format("input: '%s' and output: '%s'", input.toString(), output.toString());
            }
            if (!warning.isEmpty()) {
                CraftTweakerAPI.logWarning(String.format("No %s recipe found for %s. Command ignored!", name, warning));
            }
        } else {
            super.apply();
            //Describe it, as we don't describe it when describe is normally used as we don't have the information yet
            CraftTweakerAPI.logInfo(super.describe());
        }
    }

    @Override
    public String describe() {
        //Don't describe anything. It is too early for us to have a full description
        return null;
    }
}
