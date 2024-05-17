package mekanism.api.recipes.ingredients.chemical;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import org.jetbrains.annotations.NotNull;

/**
 * {@inheritDoc}
 *
 * @see mekanism.api.recipes.ingredients.GasStackIngredient
 * @since 10.6.0
 */
public non-sealed interface IGasIngredient extends IChemicalIngredient<Gas, IGasIngredient> {

    @NotNull
    @Override
    default IChemicalIngredientCreator<Gas, IGasIngredient> ingredientCreator() {
        return IngredientCreatorAccess.gas();
    }
}