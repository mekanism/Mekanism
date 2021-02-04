package mekanism.common.integration.projecte.mappers;

import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;

@RecipeTypeMapper
public class ItemStackToItemStackRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekItemStackToItemStack";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism Machine recipes that go from item to item. (Crushing, Enriching, Smelting)";
    }

    @Override
    public boolean canHandle(IRecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.CRUSHING || recipeType == MekanismRecipeType.ENRICHING || recipeType == MekanismRecipeType.SMELTING;
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe, INSSFakeGroupManager groupManager) {
        if (!(iRecipe instanceof ItemStackToItemStackRecipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        boolean handled = false;
        ItemStackToItemStackRecipe recipe = (ItemStackToItemStackRecipe) iRecipe;
        for (ItemStack representation : recipe.getInput().getRepresentations()) {
            ItemStack output = recipe.getOutput(representation);
            if (!output.isEmpty()) {
                IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                ingredientHelper.put(representation);
                if (ingredientHelper.addAsConversion(output)) {
                    handled = true;
                }
            }
        }
        return handled;
    }
}