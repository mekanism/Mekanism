package mekanism.common.integration.projecte.mappers;

import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.basic.BasicItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.basic.BasicNucleosynthesizingRecipe;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.machine.TileEntityAntiprotonicNucleosynthesizer;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;

@RecipeTypeMapper
public class ItemStackChemicalToItemStackRecipeMapper extends TypedMekanismRecipeMapper<ItemStackChemicalToItemStackRecipe> {

    public ItemStackChemicalToItemStackRecipeMapper() {
        super(MekanismConfigTranslations.PE_MAPPER_ITEM_CHEMICAL_TO_ITEM, ItemStackChemicalToItemStackRecipe.class, MekanismRecipeType.COMPRESSING,
              MekanismRecipeType.PURIFYING, MekanismRecipeType.INJECTING, MekanismRecipeType.PAINTING, MekanismRecipeType.METALLURGIC_INFUSING,
              MekanismRecipeType.NUCLEOSYNTHESIZING);
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ItemStackChemicalToItemStackRecipe recipe, MekFakeGroupHelper fakeGroupHelper) {
        int scale;
        if (recipe.perTickUsage()) {
            scale = recipe instanceof NucleosynthesizingRecipe ? TileEntityAntiprotonicNucleosynthesizer.BASE_TICKS_REQUIRED : TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED;
        } else {
            scale = 1;
        }
        ItemStack output = null;
        if (OPTIMIZE_BASIC) {
            if (recipe instanceof BasicItemStackChemicalToItemStackRecipe basicRecipe) {
                //This will be the case for the majority of our recipes
                output = basicRecipe.getOutputRaw();
            } else if (recipe instanceof BasicNucleosynthesizingRecipe basicRecipe) {
                //This will be the case for the majority of our recipes
                output = basicRecipe.getOutputRaw();
            }
        }
        if (output != null) {
            return addConversion(mapper, output, forIngredients(
                  fakeGroupHelper.forIngredient(recipe.getItemInput()),
                  fakeGroupHelper.forIngredient(recipe.getChemicalInput()),
                  scale
            ));
        }
        return addConversions(mapper, recipe.getItemInput(), recipe.getChemicalInput(), recipe::getOutput, ItemStack::isEmpty,
              fakeGroupHelper::forItems, fakeGroupHelper::forChemicals, ItemStackLinkedSet.TYPE_AND_TAG, TypedMekanismRecipeMapper::addConversion, scale);
    }
}