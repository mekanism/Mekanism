package mekanism.client.jei.machine;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class ItemStackGasToItemStackRecipeWrapper extends MekanismRecipeWrapper<ItemStackGasToItemStackRecipe> {

    public ItemStackGasToItemStackRecipeWrapper(ItemStackGasToItemStackRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getItemInput().getRepresentations()));
        @NonNull List<@NonNull GasStack> gasInputs = recipe.getGasInput().getRepresentations();
        int scale = TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED * TileEntityAdvancedElectricMachine.BASE_GAS_PER_TICK;
        List<GasStack> scaledGases = gasInputs.stream().map(gas -> gas.copy().withAmount(scale)).collect(Collectors.toList());
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(scaledGases));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }
}