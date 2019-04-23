package mekanism.client.jei.machine;

import java.util.Arrays;
import mekanism.common.recipe.inputs.DoubleMachineInput;
import mekanism.common.recipe.machines.DoubleMachineRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class DoubleMachineRecipeWrapper implements IRecipeWrapper {

    private final DoubleMachineRecipe recipe;

    public DoubleMachineRecipeWrapper(DoubleMachineRecipe r) {
        recipe = r;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        DoubleMachineInput input = (DoubleMachineInput) recipe.getInput();
        ingredients.setInputs(VanillaTypes.ITEM, Arrays.asList(input.itemStack, input.extraStack));
        ingredients.setOutput(VanillaTypes.ITEM, ((ItemStackOutput) recipe.getOutput()).output);
    }

    public DoubleMachineRecipe getRecipe() {
        return recipe;
    }
}