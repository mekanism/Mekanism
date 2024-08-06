package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;

@RecipeTypeMapper
public class ItemStackToInfuseTypeRecipeMapper extends TypedMekanismRecipeMapper<ItemStackToInfuseTypeRecipe> {

    public ItemStackToInfuseTypeRecipeMapper() {
        super(ItemStackToInfuseTypeRecipe.class, MekanismRecipeType.INFUSION_CONVERSION);
    }

    @Override
    public String getName() {
        return "MekItemStackToInfuseType";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism item stack to infuse type conversion recipes.";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ItemStackToInfuseTypeRecipe recipe) {
        boolean handled = false;
        for (ItemStack representation : recipe.getInput().getRepresentations()) {
            ChemicalStack output = recipe.getOutput(representation);
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