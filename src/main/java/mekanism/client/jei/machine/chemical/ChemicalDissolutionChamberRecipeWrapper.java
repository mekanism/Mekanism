package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismFluids;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ChemicalDissolutionChamberRecipeWrapper implements IRecipeWrapper {

    private final DissolutionRecipe recipe;

    public ChemicalDissolutionChamberRecipeWrapper(DissolutionRecipe r) {
        recipe = r;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(MekanismJEI.TYPE_GAS, new GasStack(MekanismFluids.SulfuricAcid,
              TileEntityChemicalDissolutionChamber.BASE_INJECT_USAGE
                    * TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED));
        ingredients.setInput(VanillaTypes.ITEM, recipe.recipeInput.ingredient);
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.recipeOutput.output);
    }

    public DissolutionRecipe getRecipe() {
        return recipe;
    }
}