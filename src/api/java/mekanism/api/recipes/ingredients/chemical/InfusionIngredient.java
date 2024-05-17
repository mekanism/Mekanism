package mekanism.api.recipes.ingredients.chemical;

import mekanism.api.chemical.infuse.InfuseType;

/**
 * Convenience class to use as a base for implementing custom infusion ingredients.
 *
 * @since 10.6.0
 */
public abstract non-sealed class InfusionIngredient extends ChemicalIngredient<InfuseType, IInfusionIngredient> implements IInfusionIngredient {
}