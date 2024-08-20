package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

@RecipeTypeMapper
public class ChemicalToChemicalRecipeMapper extends TypedMekanismRecipeMapper<ChemicalToChemicalRecipe> {

    public ChemicalToChemicalRecipeMapper() {
        super(ChemicalToChemicalRecipe.class, MekanismRecipeType.ACTIVATING, MekanismRecipeType.CENTRIFUGING);
    }

    @Override
    public String getName() {
        return "MekChemicalToChemical";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism activating and centrifuging recipes.";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ChemicalToChemicalRecipe recipe) {
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