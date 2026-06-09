package mekanism.common.integration.projecte.mappers;

import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.basic.BasicRotaryRecipe;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.util.context.ContextMap;

@RecipeTypeMapper
public class RotaryRecipeMapper extends TypedMekanismRecipeMapper<RotaryRecipe> {

    public RotaryRecipeMapper() {
        super(MekanismConfigTranslations.PE_MAPPER_ROTARY, RotaryRecipe.class, MekanismRecipeType.ROTARY);
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, RotaryRecipe recipe, MekFakeGroupHelper fakeGroupHelper, ContextMap contextMap) {
        boolean handled = true;
        if (OPTIMIZE_BASIC && recipe instanceof BasicRotaryRecipe basicRecipe) {//This will be the case for the majority of our recipes
            if (recipe.hasFluidToChemical()) {
                handled = addConversion(mapper, basicRecipe.getChemicalOutputRaw(), fakeGroupHelper.forIngredient(recipe.getFluidInput(), contextMap));
            }
            if (recipe.hasChemicalToFluid()) {
                handled |= addConversion(mapper, basicRecipe.getFluidOutputRaw(), fakeGroupHelper.forIngredient(recipe.getChemicalInput(), contextMap));
            }
        } else {
            if (recipe.hasFluidToChemical()) {
                handled = addConversions(mapper, recipe.getFluidInput(), recipe::getChemicalOutput, fakeGroupHelper::forFluids, TypedMekanismRecipeMapper::addConversion);
            }
            if (recipe.hasChemicalToFluid()) {
                handled |= addConversions(mapper, recipe.getChemicalInput(), recipe::getFluidOutput, fakeGroupHelper::forChemicals, TypedMekanismRecipeMapper::addConversion);
            }
        }
        return handled;
    }
}