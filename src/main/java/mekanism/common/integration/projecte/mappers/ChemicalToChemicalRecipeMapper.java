package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicChemicalToChemicalRecipe;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

@RecipeTypeMapper
public class ChemicalToChemicalRecipeMapper extends TypedMekanismRecipeMapper<ChemicalToChemicalRecipe> {

    public ChemicalToChemicalRecipeMapper() {
        super(MekanismConfigTranslations.PE_MAPPER_CHEMICAL_TO_CHEMICAL, ChemicalToChemicalRecipe.class, MekanismRecipeType.ACTIVATING, MekanismRecipeType.CENTRIFUGING);
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ChemicalToChemicalRecipe recipe, MekFakeGroupHelper fakeGroupHelper) {
        if (OPTIMIZE_BASIC && recipe instanceof BasicChemicalToChemicalRecipe basicRecipe) {
            //This will be the case for the majority of our recipes
            return addConversion(mapper, basicRecipe.getOutputRaw(), fakeGroupHelper.forIngredient(recipe.getInput()));
        }
        return addConversions(mapper, recipe.getInput(), recipe::getOutput, ChemicalStack::isEmpty, fakeGroupHelper::forChemicals, null,
              TypedMekanismRecipeMapper::addConversion);
    }
}