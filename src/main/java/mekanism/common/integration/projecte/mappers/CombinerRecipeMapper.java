package mekanism.common.integration.projecte.mappers;

import java.util.Objects;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.basic.BasicCombinerRecipe;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

@RecipeTypeMapper
public class CombinerRecipeMapper extends TypedMekanismRecipeMapper<CombinerRecipe> {

    public CombinerRecipeMapper() {
        super(MekanismConfigTranslations.PE_MAPPER_COMBINER, CombinerRecipe.class, MekanismRecipeType.COMBINING);
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, CombinerRecipe recipe, MekFakeGroupHelper fakeGroupHelper) {
        if (OPTIMIZE_BASIC && recipe instanceof BasicCombinerRecipe basicRecipe) {
            //This will be the case for the majority of our recipes
            return addConversion(mapper, basicRecipe.getOutputRaw(), fakeGroupHelper.forIngredients(
                  recipe.getMainInput(),
                  recipe.getExtraInput()
            ));
        }
        return addConversions(mapper, recipe.getMainInput(), recipe.getExtraInput(), recipe::getOutput, Objects::nonNull,
              fakeGroupHelper::forItems, fakeGroupHelper::forItems, null, TypedMekanismRecipeMapper::addConversion);
    }
}