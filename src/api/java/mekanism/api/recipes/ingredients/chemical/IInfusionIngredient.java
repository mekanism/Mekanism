package mekanism.api.recipes.ingredients.chemical;

import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import org.jetbrains.annotations.NotNull;

/**
 * {@inheritDoc}
 *
 * @see mekanism.api.recipes.ingredients.InfusionStackIngredient
 * @since 10.6.0
 */
public non-sealed interface IInfusionIngredient extends IChemicalIngredient<InfuseType, IInfusionIngredient> {

    @NotNull
    @Override
    default IChemicalIngredientCreator<InfuseType, IInfusionIngredient> ingredientCreator() {
        return IngredientCreatorAccess.infusion();
    }
}