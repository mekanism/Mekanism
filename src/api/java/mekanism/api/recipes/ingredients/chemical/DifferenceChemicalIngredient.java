package mekanism.api.recipes.ingredients.chemical;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
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
public non-sealed class DifferenceChemicalIngredient
      extends ChemicalIngredient {

    public static final MapCodec<DifferenceChemicalIngredient> CODEC = codec(IngredientCreatorAccess.chemical(), DifferenceChemicalIngredient::new);

    /**
     * Helper to create the codec for difference ingredients.
     */
    @Internal
    protected static MapCodec<DifferenceChemicalIngredient> codec(
          IChemicalIngredientCreator creator, BiFunction<IChemicalIngredient, IChemicalIngredient, DifferenceChemicalIngredient> constructor) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
              creator.codecNonEmpty().fieldOf(SerializationConstants.BASE).forGetter(DifferenceChemicalIngredient::base),
              creator.codecNonEmpty().fieldOf(SerializationConstants.SUBTRACTED).forGetter(DifferenceChemicalIngredient::subtracted)
        ).apply(builder, constructor));
    }

    private final IChemicalIngredient base;
    private final IChemicalIngredient subtracted;

    /**
     * @param base       ingredient the chemical must match
     * @param subtracted ingredient the chemical must not match
     */
    public DifferenceChemicalIngredient(IChemicalIngredient base, IChemicalIngredient subtracted) {
        this.base = base;
        this.subtracted = subtracted;
    }

    @Override
    public final Stream<Chemical> generateChemicals() {
        return base().generateChemicals().filter(subtracted().negate());
    }

    @Override
    public MapCodec<? extends IChemicalIngredient> codec() {
        return CODEC;
    }

    @Override
    public final boolean test(Chemical chemical) {
        return base().test(chemical) && !subtracted().test(chemical);
    }

    /**
     * {@return ingredient the chemical must match}
     */
    public final IChemicalIngredient base() {
        return base;
    }

    /**
     * {@return ingredient the chemical must not match}
     */
    public final IChemicalIngredient subtracted() {
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
        DifferenceChemicalIngredient other = (DifferenceChemicalIngredient) obj;
        return base.equals(other.base) && subtracted.equals(other.subtracted);
    }
}
