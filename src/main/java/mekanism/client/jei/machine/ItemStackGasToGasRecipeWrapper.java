package mekanism.client.jei.machine;

import java.util.Arrays;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class ItemStackGasToGasRecipeWrapper extends MekanismRecipeWrapper<ItemStackGasToGasRecipe> {

    public ItemStackGasToGasRecipeWrapper(ItemStackGasToGasRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.ITEM, Arrays.asList(recipe.getItemInput().getMatchingStacks()));
        ingredients.setInputs(MekanismJEI.TYPE_GAS, recipe.getGasInput().getRepresentations(
              TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED * TileEntityAdvancedElectricMachine.BASE_GAS_PER_TICK));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutputDefinition());
    }
}