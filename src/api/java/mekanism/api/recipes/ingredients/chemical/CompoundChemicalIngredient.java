package mekanism.api.recipes.ingredients.chemical;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.jspecify.annotations.Nullable;

/// Base Chemical ingredient implementation that matches if any of the child ingredients match. This type additionally represents the array notation used in
/// [mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator#codec][mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator#codec] internally.
///
/// @see CompoundIngredient CompoundIngredient, its item equivalent
/// @see net.neoforged.neoforge.fluids.crafting.CompoundFluidIngredient CompoundFluidIngredient, its fluid equivalent
/// @since 10.6.0
public non-sealed class CompoundChemicalIngredient extends ChemicalIngredient {

    public static final MapCodec<CompoundChemicalIngredient> CODEC = NeoForgeExtraCodecs.aliasedFieldOf(
          IngredientCreatorAccess.chemical().codec().listOf(1, Integer.MAX_VALUE), SerializationConstants.CHILDREN, SerializationConstants.INGREDIENTS
    ).xmap(CompoundChemicalIngredient::new, CompoundChemicalIngredient::children);

    private final List<ChemicalIngredient> children;

    /// @param children Ingredients to form a union from.
    ///
    /// @apiNote Prefer calling via [IChemicalIngredientCreator#ofIngredients(List)]
    public CompoundChemicalIngredient(List<ChemicalIngredient> children) {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Compound chemical ingredient must have at least one child");
        }
        this.children = List.copyOf(children);
    }

    @Override
    public final Stream<Holder<Chemical>> generateChemicals() {
        return children().stream()
              .flatMap(ChemicalIngredient::generateChemicals)
              .distinct();//Ensure we don't include the same chemical multiple times. Holder overrides #equals at least within same kind of holder
    }

    @Override
    public final boolean test(ChemicalResource chemical) {
        for (ChemicalIngredient child : children()) {
            if (child.test(chemical)) {
                return true;
            }
        }
        return false;
    }

    /// {@return all the child ingredients that this ingredient is a union of}
    public final List<ChemicalIngredient> children() {
        return children;
    }

    @Override
    public void logMissingTags() {
        children().forEach(ChemicalIngredient::logMissingTags);
    }

    @Override
    public MapCodec<CompoundChemicalIngredient> codec() {
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
        return obj instanceof CompoundChemicalIngredient other && children().equals(other.children());
    }
}
