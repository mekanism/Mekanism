package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicChemicalChemicalToChemicalRecipe;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

@RecipeTypeMapper
public class ChemicalChemicalToChemicalRecipeMapper extends TypedMekanismRecipeMapper<ChemicalChemicalToChemicalRecipe> {

    public ChemicalChemicalToChemicalRecipeMapper() {
        super(MekanismConfigTranslations.PE_MAPPER_CHEMICAL_CHEMICAL_TO_CHEMICAL, ChemicalChemicalToChemicalRecipe.class,
              MekanismRecipeType.CHEMICAL_INFUSING, MekanismRecipeType.PIGMENT_MIXING);
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ChemicalChemicalToChemicalRecipe recipe, MekFakeGroupHelper fakeGroupHelper) {
        if (OPTIMIZE_BASIC && recipe instanceof BasicChemicalChemicalToChemicalRecipe basicRecipe) {
            //This will be the case for the majority of our recipes
            return addConversion(mapper, basicRecipe.getOutputRaw(), fakeGroupHelper.forIngredients(
                  recipe.getLeftInput(),
                  recipe.getRightInput()
            ));
        }
        return addConversions(mapper, recipe.getLeftInput(), recipe.getRightInput(), recipe::getOutput, ChemicalStack::isEmpty,
              fakeGroupHelper::forChemicals, fakeGroupHelper::forChemicals, null, TypedMekanismRecipeMapper::addConversion);
    }
}