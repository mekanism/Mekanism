package mekanism.api.recipes.ingredients.chemical;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

/// Base Chemical ingredient implementation that matches if all child ingredients match
///
/// @see net.neoforged.neoforge.common.crafting.IntersectionIngredient IntersectionIngredient, its item equivalent
/// @see net.neoforged.neoforge.fluids.crafting.IntersectionFluidIngredient IntersectionFluidIngredient, its fluid equivalent
/// @since 10.6.0
public non-sealed class IntersectionChemicalIngredient extends ChemicalIngredient {

    public static final MapCodec<IntersectionChemicalIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          IngredientCreatorAccess.chemical().codec().listOf(1, Integer.MAX_VALUE).fieldOf(SerializationConstants.CHILDREN).forGetter(IntersectionChemicalIngredient::children)
    ).apply(builder, IntersectionChemicalIngredient::new));

    private final List<ChemicalIngredient> children;

    /// @param children Ingredients to form an intersection from.
    ///
    /// @apiNote Prefer calling via [IChemicalIngredientCreator#intersection(List)]
    public IntersectionChemicalIngredient(List<ChemicalIngredient> children) {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Intersection chemical ingredient must have at least one child");
        }
        this.children = children;
    }

    @Override
    public final boolean test(ChemicalResource chemical) {
        for (ChemicalIngredient child : children()) {
            if (!child.test(chemical)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final Stream<Holder<Chemical>> generateChemicals() {
        return children().stream()
              .flatMap(ChemicalIngredient::generateChemicals)
              .distinct()//Ensure we don't include the same chemical multiple times. Holder overrides #equals at least within same kind of holder
              .filter(this::test);
    }

    /// {@return all the child ingredients that this ingredient is an intersection of}
    public final List<ChemicalIngredient> children() {
        return children;
    }

    @Override
    public void logMissingTags() {
        children().forEach(ChemicalIngredient::logMissingTags);
    }

    @Override
    public MapCodec<IntersectionChemicalIngredient> codec() {
        return CODEC;
    }

    @Override
    public int hashCode() {
        return children().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof IntersectionChemicalIngredient other && children().equals(other.children());
    }
}
