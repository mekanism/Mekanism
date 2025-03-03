package mekanism.common.integration.projecte.mappers;

import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.basic.BasicItemStackToItemStackRecipe;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;

@RecipeTypeMapper
public class ItemStackToItemStackRecipeMapper extends TypedMekanismRecipeMapper<ItemStackToItemStackRecipe> {

    public ItemStackToItemStackRecipeMapper() {
        super(MekanismConfigTranslations.PE_MAPPER_ITEM_TO_ITEM, ItemStackToItemStackRecipe.class, MekanismRecipeType.CRUSHING, MekanismRecipeType.ENRICHING,
              MekanismRecipeType.SMELTING);
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ItemStackToItemStackRecipe recipe, MekFakeGroupHelper fakeGroupHelper) {
        if (OPTIMIZE_BASIC && recipe instanceof BasicItemStackToItemStackRecipe basicRecipe) {
            //This will be the case for the majority of our recipes
            return addConversion(mapper, basicRecipe.getOutputRaw(), fakeGroupHelper.forIngredient(recipe.getInput()));
        }
        return addConversions(mapper, recipe.getInput(), recipe::getOutput, ItemStack::isEmpty, fakeGroupHelper::forItems, ItemStackLinkedSet.TYPE_AND_TAG,
              TypedMekanismRecipeMapper::addConversion);
    }
}