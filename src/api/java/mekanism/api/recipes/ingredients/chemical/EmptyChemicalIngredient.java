package mekanism.api.recipes.ingredients.chemical;

import com.mojang.serialization.MapCodec;
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
public non-sealed class EmptyChemicalIngredient
      extends ChemicalIngredient {

    public static final EmptyChemicalIngredient INSTANCE = new EmptyChemicalIngredient();
    public static final MapCodec<EmptyChemicalIngredient> CODEC = MapCodec.unit(INSTANCE);

    @Internal
    protected EmptyChemicalIngredient() {
    }

    @Override
    public final boolean test(Chemical chemical) {
        return chemical.isEmptyType();
    }

    @Override
    public final Stream<Chemical> generateChemicals() {
        return Stream.empty();
    }

    @Override
    public MapCodec<? extends IChemicalIngredient> codec() {
        return CODEC;
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
