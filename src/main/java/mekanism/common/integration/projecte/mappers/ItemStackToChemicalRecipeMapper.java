package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.basic.BasicItemStackToChemicalRecipe;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

@RecipeTypeMapper
public class ItemStackToChemicalRecipeMapper extends TypedMekanismRecipeMapper<ItemStackToChemicalRecipe> {

    public ItemStackToChemicalRecipeMapper() {
        super(MekanismConfigTranslations.PE_MAPPER_ITEM_TO_CHEMICAL, ItemStackToChemicalRecipe.class, MekanismRecipeType.CHEMICAL_CONVERSION,
              MekanismRecipeType.OXIDIZING, MekanismRecipeType.PIGMENT_EXTRACTING);
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ItemStackToChemicalRecipe recipe, MekFakeGroupHelper fakeGroupHelper) {
        if (OPTIMIZE_BASIC && recipe instanceof BasicItemStackToChemicalRecipe basicRecipe) {
            //This will be the case for the majority of our recipes
            return addConversion(mapper, basicRecipe.getOutputRaw(), fakeGroupHelper.forIngredient(recipe.getInput()));
        }
        return addConversions(mapper, recipe.getInput(), recipe::getOutput, ChemicalStack::isEmpty, fakeGroupHelper::forItems, null,
              TypedMekanismRecipeMapper::addConversion);
    }
}