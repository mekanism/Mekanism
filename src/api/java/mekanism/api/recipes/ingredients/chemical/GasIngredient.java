package mekanism.api.recipes.ingredients.chemical;

import mekanism.api.chemical.gas.Gas;

/**
 * Convenience class to use as a base for implementing custom gas ingredients.
 *
 * @since 10.6.0
 */
public abstract non-sealed class GasIngredient extends ChemicalIngredient<Gas, IGasIngredient> implements IGasIngredient {
}