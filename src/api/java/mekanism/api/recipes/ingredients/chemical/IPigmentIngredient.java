package mekanism.api.recipes.ingredients.chemical;

import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import org.jetbrains.annotations.NotNull;

/**
 * {@inheritDoc}
 *
 * @see mekanism.api.recipes.ingredients.PigmentStackIngredient
 * @since 10.6.0
 */
public non-sealed interface IPigmentIngredient extends IChemicalIngredient<Pigment, IPigmentIngredient> {

    @NotNull
    @Override
    default IChemicalIngredientCreator<Pigment, IPigmentIngredient> ingredientCreator() {
        return IngredientCreatorAccess.pigment();
    }
}