package mekanism.api.recipes.ingredients.chemical;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import org.jetbrains.annotations.Nullable;

/**
 * Base Chemical ingredient implementation that matches the difference of two provided chemical ingredients, i.e. anything contained in {@code base} that is not in
 * {@code subtracted}.
 *
 * @see DifferenceIngredient DifferenceIngredient, its item equivalent
 * @since 10.6.0
 */
@NothingNullByDefault
public non-sealed class DifferenceChemicalIngredient extends ChemicalIngredient {

    public static final MapCodec<DifferenceChemicalIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          IngredientCreatorAccess.chemical().codecNonEmpty().fieldOf(SerializationConstants.BASE).forGetter(DifferenceChemicalIngredient::base),
          IngredientCreatorAccess.chemical().codecNonEmpty().fieldOf(SerializationConstants.SUBTRACTED).forGetter(DifferenceChemicalIngredient::subtracted)
    ).apply(builder, DifferenceChemicalIngredient::new));

    private final ChemicalIngredient base;
    private final ChemicalIngredient subtracted;

    /**
     * @param base       ingredient the chemical must match
     * @param subtracted ingredient the chemical must not match
     */
    public DifferenceChemicalIngredient(ChemicalIngredient base, ChemicalIngredient subtracted) {
        this.base = base;
        this.subtracted = subtracted;
    }

    @Override
    public final Stream<Holder<Chemical>> generateChemicalHolders() {
        //return base().generateChemicalHolders().filter(subtracted().negate());
        //TODO - 1.22: Use the above
        return base().generateChemicalHolders().filter(holder -> !subtracted().test(holder));
    }

    @Override
    public MapCodec<? extends ChemicalIngredient> codec() {
        return CODEC;
    }

    @Override
    public final boolean test(Holder<Chemical> chemical) {
        return base().test(chemical) && !subtracted().test(chemical);
    }

    /**
     * {@return ingredient the chemical must match}
     */
    public final ChemicalIngredient base() {
        return base;
    }

    /**
     * {@return ingredient the chemical must not match}
     */
    public final ChemicalIngredient subtracted() {
        return subtracted;
    }

    @Override
    public void logMissingTags() {
        base().logMissingTags();
    }

    @Override
    public int hashCode() {
        return 31 * base.hashCode() + subtracted.hashCode();
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
