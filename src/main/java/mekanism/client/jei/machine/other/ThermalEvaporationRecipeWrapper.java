package mekanism.client.jei.machine.other;

import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraftforge.fluids.FluidStack;

public class ThermalEvaporationRecipeWrapper implements IRecipeWrapper {

    private final ThermalEvaporationRecipe recipe;

    public ThermalEvaporationRecipeWrapper(ThermalEvaporationRecipe r) {
        recipe = r;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(FluidStack.class, recipe.getInput().ingredient);
        ingredients.setOutput(FluidStack.class, recipe.getOutput().output);
    }

    public ThermalEvaporationRecipe getRecipe() {
        return recipe;
    }
}
