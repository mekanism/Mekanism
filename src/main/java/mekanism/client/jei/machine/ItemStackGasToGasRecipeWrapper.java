package mekanism.client.jei.machine;

import java.util.Collections;
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
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getItemInput().getRepresentations()));
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getGasInput().getRepresentations(
              TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED * TileEntityAdvancedElectricMachine.BASE_GAS_PER_TICK)));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutputDefinition());
    }
}