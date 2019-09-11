package mekanism.client.jei.machine;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class ItemStackGasToGasRecipeWrapper extends MekanismRecipeWrapper<ItemStackGasToGasRecipe> {

    public ItemStackGasToGasRecipeWrapper(ItemStackGasToGasRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getItemInput().getRepresentations()));
        @NonNull List<@NonNull GasStack> gasInputs = recipe.getGasInput().getRepresentations();
        //TODO: Should this be "generalized" to some values that are not stored in the chemical dissolution chamber class
        int scale = TileEntityChemicalDissolutionChamber.BASE_INJECT_USAGE * TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED;
        List<GasStack> scaledGases = gasInputs.stream().map(gas -> gas.copy().withAmount(scale)).collect(Collectors.toList());
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(scaledGases));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutputDefinition());
    }
}