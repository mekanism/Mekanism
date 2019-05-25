package mekanism.client.jei.machine;

import java.util.Arrays;
import mekanism.common.recipe.inputs.DoubleMachineInput;
import mekanism.common.recipe.machines.DoubleMachineRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class DoubleMachineRecipeWrapper<RECIPE extends DoubleMachineRecipe<RECIPE>> extends MekanismRecipeWrapper<RECIPE> {

    public DoubleMachineRecipeWrapper(RECIPE recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        DoubleMachineInput input = recipe.getInput();
        ingredients.setInputs(VanillaTypes.ITEM, Arrays.asList(input.itemStack, input.extraStack));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput().output);
    }
}