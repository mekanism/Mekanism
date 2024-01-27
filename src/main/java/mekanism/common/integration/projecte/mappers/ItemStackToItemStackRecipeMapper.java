package mekanism.common.integration.projecte.mappers;

import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;

@RecipeTypeMapper
public class ItemStackToItemStackRecipeMapper extends TypedMekanismRecipeMapper<ItemStackToItemStackRecipe> {

    public ItemStackToItemStackRecipeMapper() {
        super(ItemStackToItemStackRecipe.class, MekanismRecipeType.CRUSHING, MekanismRecipeType.ENRICHING, MekanismRecipeType.SMELTING);
    }

    @Override
    public String getName() {
        return "MekItemStackToItemStack";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism Machine recipes that go from item to item. (Crushing, Enriching, Smelting)";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ItemStackToItemStackRecipe recipe) {
        boolean handled = false;
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