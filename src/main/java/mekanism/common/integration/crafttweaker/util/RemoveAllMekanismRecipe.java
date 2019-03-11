package mekanism.common.integration.crafttweaker.util;

import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.utils.BaseMapRemoval;
import java.util.Map.Entry;
import mekanism.common.integration.crafttweaker.helpers.RecipeInfoHelper;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;

public class RemoveAllMekanismRecipe<RECIPE extends MachineRecipe> extends BaseMapRemoval<MachineInput, RECIPE> {

    public RemoveAllMekanismRecipe(String name, Recipe recipeType) {
        super(name, recipeType.get());
    }

    @Override
    public void apply() {
        //Don't move this into the constructor so that if an addon registers recipes late, we can still remove them
        recipes.putAll(map);
        super.apply();
        LogHelper.logInfo("Removed all recipes for " + name);
    }

    @Override
    public String describe() {
        //Don't describe anything. It is too early for us to have a full description
        return null;
    }

    @Override
    protected String getRecipeInfo(Entry<MachineInput, RECIPE> recipe) {
        return RecipeInfoHelper.getRecipeInfo(recipe);
    }
}