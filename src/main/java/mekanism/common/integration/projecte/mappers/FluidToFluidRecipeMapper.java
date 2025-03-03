package mekanism.common.integration.projecte.mappers;

import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.basic.BasicFluidToFluidRecipe;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackLinkedSet;

@RecipeTypeMapper
public class FluidToFluidRecipeMapper extends TypedMekanismRecipeMapper<FluidToFluidRecipe> {

    public FluidToFluidRecipeMapper() {
        super(MekanismConfigTranslations.PE_MAPPER_EVAPORATING, FluidToFluidRecipe.class, MekanismRecipeType.EVAPORATING);
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, FluidToFluidRecipe recipe, MekFakeGroupHelper fakeGroupHelper) {
        if (OPTIMIZE_BASIC && recipe instanceof BasicFluidToFluidRecipe basicRecipe) {
            //This will be the case for the majority of our recipes
            return addConversion(mapper, basicRecipe.getOutputRaw(), fakeGroupHelper.forIngredient(recipe.getInput()));
        }
        return addConversions(mapper, recipe.getInput(), recipe::getOutput, FluidStack::isEmpty, fakeGroupHelper::forFluids, FluidStackLinkedSet.TYPE_AND_COMPONENTS,
              TypedMekanismRecipeMapper::addConversion);
    }
}