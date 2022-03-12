package mekanism.client.jei.recipe;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;

//TODO - V11: Make the SPS have a proper recipe type to allow for custom recipes
public record SPSJEIRecipe(GasStackIngredient input, GasStack output) {
}