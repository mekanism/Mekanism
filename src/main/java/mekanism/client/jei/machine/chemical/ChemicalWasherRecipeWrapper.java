package mekanism.client.jei.machine.chemical;

import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.machines.WasherRecipe;
import mekanism.common.tile.TileEntityChemicalWasher;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ChemicalWasherRecipeWrapper implements IRecipeWrapper {

    private final WasherRecipe recipe;

    public ChemicalWasherRecipeWrapper(WasherRecipe r) {
        recipe = r;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients
              .setInput(VanillaTypes.FLUID, new FluidStack(FluidRegistry.WATER, TileEntityChemicalWasher.WATER_USAGE));
        ingredients.setInput(MekanismJEI.TYPE_GAS, recipe.recipeInput.ingredient);
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.recipeOutput.output);
    }

    public WasherRecipe getRecipe() {
        return recipe;
    }
}