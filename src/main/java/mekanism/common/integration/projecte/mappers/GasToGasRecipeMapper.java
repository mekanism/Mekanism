package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

@RecipeTypeMapper
public class GasToGasRecipeMapper extends TypedMekanismRecipeMapper<GasToGasRecipe> {

    public GasToGasRecipeMapper() {
        super(GasToGasRecipe.class, MekanismRecipeType.ACTIVATING, MekanismRecipeType.CENTRIFUGING);
    }

    @Override
    public String getName() {
        return "MekGasToGas";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism activating and centrifuging recipes.";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, GasToGasRecipe recipe) {
        boolean handled = false;
        for (ChemicalStack representation : recipe.getInput().getRepresentations()) {
            ChemicalStack output = recipe.getOutput(representation);
            if (!output.isEmpty()) {
                IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                ingredientHelper.put(representation);
                if (ingredientHelper.addAsConversion(output)) {
                    handled = true;
                }
            }
        }
        return handled;
    }
}