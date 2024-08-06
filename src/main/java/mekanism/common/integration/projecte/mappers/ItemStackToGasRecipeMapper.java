package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;

@RecipeTypeMapper
public class ItemStackToGasRecipeMapper extends TypedMekanismRecipeMapper<ItemStackToGasRecipe> {

    public ItemStackToGasRecipeMapper() {
        super(ItemStackToGasRecipe.class, MekanismRecipeType.GAS_CONVERSION, MekanismRecipeType.OXIDIZING);
    }

    @Override
    public String getName() {
        return "MekItemStackToGas";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism item stack to gas recipes. (Gas conversion, Oxidizing)";
    }

    @Override
    protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, ItemStackToGasRecipe recipe) {
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