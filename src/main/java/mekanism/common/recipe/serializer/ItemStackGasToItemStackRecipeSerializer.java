package mekanism.common.recipe.serializer;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;

public class ItemStackGasToItemStackRecipeSerializer<RECIPE extends ItemStackGasToItemStackRecipe> extends
      ItemStackChemicalToItemStackRecipeSerializer<Gas, GasStack, GasStackIngredient, RECIPE> {

    public ItemStackGasToItemStackRecipeSerializer(IFactory<Gas, GasStack, GasStackIngredient, RECIPE> factory) {
        super(factory);
    }

    @Override
    protected ChemicalIngredientDeserializer<Gas, GasStack, GasStackIngredient> getDeserializer() {
        return ChemicalIngredientDeserializer.GAS;
    }
}