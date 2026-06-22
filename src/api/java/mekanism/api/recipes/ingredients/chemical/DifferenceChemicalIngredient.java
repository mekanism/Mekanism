package mekanism.api.recipes.ingredients.chemical;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.stream.Stream;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import org.jspecify.annotations.Nullable;

/// Base Chemical ingredient implementation that matches the difference of two provided chemical ingredients, i.e. anything contained in `base` that is not in
/// `subtracted`.
///
/// @see DifferenceIngredient DifferenceIngredient, its item equivalent
/// @see net.neoforged.neoforge.fluids.crafting.DifferenceFluidIngredient DifferenceFluidIngredient, its fluid equivalent
/// @since 10.6.0
public non-sealed class DifferenceChemicalIngredient extends ChemicalIngredient {

    public static final MapCodec<DifferenceChemicalIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          IngredientCreatorAccess.chemical().codec().fieldOf(SerializationConstants.BASE).forGetter(DifferenceChemicalIngredient::base),
          IngredientCreatorAccess.chemical().codec().fieldOf(SerializationConstants.SUBTRACTED).forGetter(DifferenceChemicalIngredient::subtracted)
    ).apply(builder, DifferenceChemicalIngredient::new));

    private final ChemicalIngredient base;
    private final ChemicalIngredient subtracted;

    /// @param base       ingredient the chemical must match
    /// @param subtracted ingredient the chemical must not match
    ///
    /// @apiNote Prefer calling via [IChemicalIngredientCreator#difference(ChemicalIngredient, ChemicalIngredient)]
    public DifferenceChemicalIngredient(ChemicalIngredient base, ChemicalIngredient subtracted) {
        this.base = Objects.requireNonNull(base, "Base ingredient may not be null");
        this.subtracted = Objects.requireNonNull(subtracted, "Subtracted ingredient may not be null");
    }

    @Override
    public final Stream<Holder<Chemical>> generateChemicals() {
        return base().generateChemicals().filter(holder -> !subtracted().test(holder));
    }

    @Override
    public final boolean test(ChemicalResource chemical) {
        return base().test(chemical) && !subtracted().test(chemical);
    }

    /// {@return ingredient the chemical must match}
    public final ChemicalIngredient base() {
        return base;
    }

    /// {@return ingredient the chemical must not match}
    public final ChemicalIngredient subtracted() {
        return subtracted;
    }

    @Override
    public void logMissingTags() {
        base().logMissingTags();
    }

    @Override
    public MapCodec<DifferenceChemicalIngredient> codec() {
        return CODEC;
    }

    @Override
    public int hashCode() {
        return 31 * base().hashCode() + subtracted().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof DifferenceChemicalIngredient other) {
            return base().equals(other.base()) && subtracted().equals(other.subtracted());
        }
        return false;
    }
}
