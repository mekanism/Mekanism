package mekanism.api.recipes.ingredients.chemical;

import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

/**
 * Base Chemical ingredient implementation that only matches the given chemical.
 * <p>
 * Unlike with ingredients, this is an explicit "type" of chemical ingredient, though it may still be written without a type field, see
 * {@link mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator#mapCodecNonEmpty}
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public abstract non-sealed class SingleChemicalIngredient<CHEMICAL extends Chemical<CHEMICAL>, INGREDIENT extends IChemicalIngredient<CHEMICAL, INGREDIENT>>
      extends ChemicalIngredient<CHEMICAL, INGREDIENT> {

    private final Holder<CHEMICAL> chemical;

    /**
     * @param chemical Holder for the chemical to match.
     */
    @Internal
    protected SingleChemicalIngredient(Holder<CHEMICAL> chemical) {
        if (chemical.is(MekanismAPI.EMPTY_CHEMICAL_NAME)) {
            throw new IllegalStateException("SingleChemicalIngredient must not be constructed with mekanism:empty, use IChemicalIngredientCreator.empty() instead!");
        }
        this.chemical = chemical;
    }

    @Override
    public final boolean test(CHEMICAL chemical) {
        return chemical == this.chemical.value();
    }

    @Override
    public final Stream<CHEMICAL> generateChemicals() {
        return Stream.of(chemical.value());
    }

    /**
     * {@return holder for the chemical to match}
     */
    public final Holder<CHEMICAL> chemical() {
        return chemical;
    }

    @Override
    public int hashCode() {
        return chemical.value().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return chemical.is(((SingleChemicalIngredient<CHEMICAL, INGREDIENT>) obj).chemical);
    }
}
