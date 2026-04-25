package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicWashingRecipe;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.util.context.ContextMap;

@RecipeTypeMapper
public class FluidChemicalToChemicalRecipeMapper extends TypedMekanismRecipeMapper<FluidChemicalToChemicalRecipe> {

    public FluidChemicalToChemicalRecipeMapper() {
        super(MekanismConfigTranslations.PE_MAPPER_WASHING, FluidChemicalToChemicalRecipe.class, MekanismRecipeType.WASHING);
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, FluidChemicalToChemicalRecipe recipe, MekFakeGroupHelper fakeGroupHelper, ContextMap contextMap) {
        if (OPTIMIZE_BASIC && recipe instanceof BasicWashingRecipe basicRecipe) {
            //This will be the case for the majority of our recipes
            return addConversion(mapper, basicRecipe.getOutputRaw(), fakeGroupHelper.forIngredients(
                  contextMap, recipe.getFluidInput(),
                  recipe.getChemicalInput()
            ));
        }
        return addConversions(mapper, recipe.getFluidInput(), recipe.getChemicalInput(), recipe::getOutput, ChemicalStack::isEmpty,
              fakeGroupHelper::forFluids, fakeGroupHelper::forChemicals, null, TypedMekanismRecipeMapper::addConversion, contextMap);
    }
}