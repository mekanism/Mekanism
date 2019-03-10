package mekanism.client.jei.machine.other;

import mekanism.api.gas.GasStack;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class PRCRecipeWrapper implements IRecipeWrapper {

    private final PressurizedRecipe recipe;

    public PRCRecipeWrapper(PressurizedRecipe r) {
        recipe = r;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(ItemStack.class, recipe.recipeInput.getSolid());
        ingredients.setInput(FluidStack.class, recipe.recipeInput.getFluid());
        ingredients.setInput(GasStack.class, recipe.recipeInput.getGas());
        ingredients.setOutput(ItemStack.class, recipe.recipeOutput.getItemOutput());
        ingredients.setOutput(GasStack.class, recipe.recipeOutput.getGasOutput());
    }

    public PressurizedRecipe getRecipe() {
        return recipe;
    }
}
