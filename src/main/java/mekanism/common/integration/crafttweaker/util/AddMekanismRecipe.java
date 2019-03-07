package mekanism.common.integration.crafttweaker.util;

import com.blamejared.mtlib.utils.BaseMapAddition;
import mekanism.common.integration.crafttweaker.helpers.RecipeInfoHelper;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;

import java.util.Map;

public class AddMekanismRecipe extends BaseMapAddition<MachineInput, MachineRecipe> {
    public AddMekanismRecipe(String name, Recipe recipeType, MachineRecipe recipe) {
        super(name, recipeType.get());
        recipes.put(recipe.getInput(), recipe);
    }

    @Override
    protected String getRecipeInfo(Map.Entry<MachineInput, MachineRecipe> recipe) {
        return RecipeInfoHelper.getRecipeInfo(recipe);
    }
}