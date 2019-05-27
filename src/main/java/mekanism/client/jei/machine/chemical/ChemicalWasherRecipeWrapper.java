package mekanism.client.jei.machine.chemical;

import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.MekanismRecipeWrapper;
import mekanism.common.recipe.machines.WasherRecipe;
import mekanism.common.tile.TileEntityChemicalWasher;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ChemicalWasherRecipeWrapper<RECIPE extends WasherRecipe> extends MekanismRecipeWrapper<RECIPE> {

    public ChemicalWasherRecipeWrapper(RECIPE recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, new FluidStack(FluidRegistry.WATER, TileEntityChemicalWasher.WATER_USAGE));
        ingredients.setInput(MekanismJEI.TYPE_GAS, recipe.recipeInput.ingredient);
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.recipeOutput.output);
    }
}