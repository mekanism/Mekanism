package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.basic.BasicChemicalDissolutionRecipe;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

@RecipeTypeMapper
public class ChemicalDissolutionRecipeMapper extends TypedMekanismRecipeMapper<ChemicalDissolutionRecipe> {

    public ChemicalDissolutionRecipeMapper() {
        super(MekanismConfigTranslations.PE_MAPPER_DISSOLUTION, ChemicalDissolutionRecipe.class, MekanismRecipeType.DISSOLUTION);
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ChemicalDissolutionRecipe recipe, MekFakeGroupHelper fakeGroupHelper) {
        int scale = recipe.perTickUsage() ? TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED : 1;
        if (OPTIMIZE_BASIC && recipe instanceof BasicChemicalDissolutionRecipe basicRecipe) {
            //This will be the case for the majority of our recipes
            return addConversion(mapper, basicRecipe.getOutputRaw(), forIngredients(
                  fakeGroupHelper.forIngredient(recipe.getItemInput()),
                  fakeGroupHelper.forIngredient(recipe.getChemicalInput()),
                  scale
            ));
        }
        return addConversions(mapper, recipe.getItemInput(), recipe.getChemicalInput(), recipe::getOutput, ChemicalStack::isEmpty,
              fakeGroupHelper::forItems, fakeGroupHelper::forChemicals, null, TypedMekanismRecipeMapper::addConversion, scale);
    }
}