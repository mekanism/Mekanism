package mekanism.api.recipes.ingredients.chemical;

import mekanism.api.chemical.slurry.Slurry;

/**
 * Convenience class to use as a base for implementing custom slurry ingredients.
 *
 * @since 10.6.0
 */
public abstract non-sealed class SlurryIngredient extends ChemicalIngredient<Slurry, ISlurryIngredient> implements ISlurryIngredient {
}