package mekanism.client.jei.recipe;

import javax.annotation.Nullable;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;

public record BoilerJEIRecipe(@Nullable GasStackIngredient superHeatedCoolant, FluidStackIngredient water, GasStack steam, GasStack cooledCoolant,
                              double temperature) {
}