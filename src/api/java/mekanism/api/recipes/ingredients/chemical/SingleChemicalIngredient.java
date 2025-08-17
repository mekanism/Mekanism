package mekanism.api.recipes.ingredients.chemical;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.core.Holder;
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
public non-sealed class SingleChemicalIngredient extends ChemicalIngredient {

    public static final MapCodec<SingleChemicalIngredient> CODEC = ChemicalStack.CHEMICAL_NON_EMPTY_HOLDER_CODEC.xmap(SingleChemicalIngredient::new, SingleChemicalIngredient::chemical)
          .fieldOf(SerializationConstants.CHEMICAL);

    private final Holder<Chemical> chemical;

    /**
     * @param chemical Holder for the chemical to match.
     */
    public SingleChemicalIngredient(Holder<Chemical> chemical) {
        if (chemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
            throw new IllegalStateException("SingleChemicalIngredient must not be constructed with mekanism:empty, use IChemicalIngredientCreator.empty() instead!");
        }
        this.chemical = chemical;
    }

    @Override
    public final boolean test(Holder<Chemical> chemical) {
        return chemical.is(this.chemical);
    }

    @Override
    public final Stream<Holder<Chemical>> generateChemicalHolders() {
        return Stream.of(chemical);
    }

    /**
     * {@return holder for the chemical to match}
     */
    public final Holder<Chemical> chemical() {
        return chemical;
    }

    @Override
    public MapCodec<SingleChemicalIngredient> codec() {
        return CODEC;
    }

    @Override
    public void logMissingTags() {
        if (!chemical.isBound()) {
            MekanismAPI.logger.error("Unbound chemical: {}", chemical.getRegisteredName());
        }
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
        return test(((SingleChemicalIngredient) obj).chemical);
    }
}
