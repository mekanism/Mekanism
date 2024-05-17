package mekanism.api.recipes.ingredients.chemical;

import java.util.Objects;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

/**
 * Base Chemical ingredient implementation that matches the difference of two provided chemical ingredients, i.e. anything contained in {@code base} that is not in
 * {@code subtracted}.
 *
 * @see DifferenceIngredient DifferenceIngredient, its item equivalent
 * @since 10.6.0
 */
@NothingNullByDefault
public abstract non-sealed class DifferenceChemicalIngredient<CHEMICAL extends Chemical<CHEMICAL>, INGREDIENT extends IChemicalIngredient<CHEMICAL, INGREDIENT>>
      extends ChemicalIngredient<CHEMICAL, INGREDIENT> {

    private final INGREDIENT base;
    private final INGREDIENT subtracted;

    /**
     * @param base       ingredient the chemical must match
     * @param subtracted ingredient the chemical must not match
     */
    @Internal
    protected DifferenceChemicalIngredient(INGREDIENT base, INGREDIENT subtracted) {
        this.base = base;
        this.subtracted = subtracted;
    }

    @Override
    public final Stream<CHEMICAL> generateChemicals() {
        return base().generateChemicals().filter(subtracted().negate());
    }

    @Override
    public final boolean test(CHEMICAL chemical) {
        return base().test(chemical) && !subtracted().test(chemical);
    }

    /**
     * {@return ingredient the chemical must match}
     */
    public final INGREDIENT base() {
        return base;
    }

    /**
     * {@return ingredient the chemical must not match}
     */
    public final INGREDIENT subtracted() {
        return subtracted;
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, subtracted);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DifferenceChemicalIngredient<?, ?> other = (DifferenceChemicalIngredient<?, ?>) obj;
        return base.equals(other.base) && subtracted.equals(other.subtracted);
    }
}
