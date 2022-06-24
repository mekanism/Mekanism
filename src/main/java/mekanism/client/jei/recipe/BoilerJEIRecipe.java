package mekanism.client.jei.recipe;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import org.jetbrains.annotations.Nullable;

public record BoilerJEIRecipe(@Nullable GasStackIngredient superHeatedCoolant, FluidStackIngredient water, GasStack steam, GasStack cooledCoolant,
                              double temperature) {
}