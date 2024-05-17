package mekanism.api.recipes.ingredients.chemical;

import mekanism.api.chemical.pigment.Pigment;

/**
 * Convenience class to use as a base for implementing custom pigment ingredients.
 *
 * @since 10.6.0
 */
public abstract non-sealed class PigmentIngredient extends ChemicalIngredient<Pigment, IPigmentIngredient> implements IPigmentIngredient {
}