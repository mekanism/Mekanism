package mekanism.generators.client.jei.recipe;

import javax.annotation.Nullable;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;

//If null -> coolant is water
public record FissionJEIRecipe(@Nullable GasStackIngredient inputCoolant, GasStackIngredient fuel, GasStack outputCoolant, GasStack waste) {
}