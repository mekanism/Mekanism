package mekanism.common.integration.projecte.mappers;

import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.util.context.ContextMap;

@RecipeTypeMapper
public class ChemicalCrystallizerRecipeMapper extends TypedMekanismRecipeMapper<ChemicalCrystallizerRecipe> {

    public ChemicalCrystallizerRecipeMapper() {
        super(MekanismConfigTranslations.PE_MAPPER_CRYSTALLIZER, ChemicalCrystallizerRecipe.class, MekanismRecipeType.CRYSTALLIZING);
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ChemicalCrystallizerRecipe recipe, MekFakeGroupHelper fakeGroupHelper, ContextMap contextMap) {
        if (OPTIMIZE_BASIC && recipe instanceof BasicChemicalCrystallizerRecipe basicRecipe) {
            //This will be the case for the majority of our recipes
            return addConversion(mapper, basicRecipe.getOutputRaw(), fakeGroupHelper.forIngredient(recipe.getInput(), contextMap));
        }
        return addConversions(mapper, recipe.getInput(), recipe::getOutput, fakeGroupHelper::forChemicals, TypedMekanismRecipeMapper::addConversion);
    }
}