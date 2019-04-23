package mekanism.client.jei.machine;

import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class MachineRecipeWrapper implements IRecipeWrapper {

    private final BasicMachineRecipe recipe;

    public MachineRecipeWrapper(BasicMachineRecipe r) {
        recipe = r;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, ((ItemStackInput) recipe.getInput()).ingredient);
        ingredients.setOutput(VanillaTypes.ITEM, ((ItemStackOutput) recipe.getOutput()).output);
    }

    public BasicMachineRecipe getRecipe() {
        return recipe;
    }
}