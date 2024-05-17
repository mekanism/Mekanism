package mekanism.api.recipes.ingredients.chemical;

import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Base Chemical ingredient implementation for a singleton that represents an empty chemical ingredient.
 * <p>
 * This is the only instance of an <b>explicitly</b> empty ingredient, and may be used as a fallback in ChemicalIngredient convenience methods (such as when trying to
 * create an ingredient from an empty list).
 *
 * @see mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator#empty()
 * @see IChemicalIngredient#isEmpty()
 * @since 10.6.0
 */
@NothingNullByDefault
public abstract non-sealed class EmptyChemicalIngredient<CHEMICAL extends Chemical<CHEMICAL>, INGREDIENT extends IChemicalIngredient<CHEMICAL, INGREDIENT>>
      extends ChemicalIngredient<CHEMICAL, INGREDIENT> {

    @Internal
    protected EmptyChemicalIngredient() {
    }

    @Override
    public final boolean test(CHEMICAL chemical) {
        return chemical.isEmptyType();
    }

    @Override
    public final Stream<CHEMICAL> generateChemicals() {
        return Stream.empty();
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
