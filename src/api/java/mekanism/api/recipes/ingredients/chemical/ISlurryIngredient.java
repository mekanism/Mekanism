package mekanism.api.recipes.ingredients.chemical;

import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import org.jetbrains.annotations.NotNull;


/**
 * {@inheritDoc}
 *
 * @see mekanism.api.recipes.ingredients.SlurryStackIngredient
 * @since 10.6.0
 */
public non-sealed interface ISlurryIngredient extends IChemicalIngredient<Slurry, ISlurryIngredient> {

    @NotNull
    @Override
    default IChemicalIngredientCreator<Slurry, ISlurryIngredient> ingredientCreator() {
        return IngredientCreatorAccess.slurry();
    }
}