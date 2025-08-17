package mekanism.api.recipes.ingredients.chemical;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.jetbrains.annotations.Nullable;

/**
 * Base Chemical ingredient implementation that matches if any of the child ingredients match. This type additionally represents the array notation used in
 * {@linkplain mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator#codec} internally.
 *
 * @see CompoundIngredient CompoundIngredient, its item equivalent
 * @since 10.6.0
 */
@NothingNullByDefault
public non-sealed class CompoundChemicalIngredient extends ChemicalIngredient {

    public static final MapCodec<CompoundChemicalIngredient> CODEC = NeoForgeExtraCodecs.aliasedFieldOf(
          IngredientCreatorAccess.chemical().listCodecMultipleElements(), SerializationConstants.CHILDREN, SerializationConstants.INGREDIENTS
    ).xmap(
          CompoundChemicalIngredient::new, CompoundChemicalIngredient::children
    );

    private final List<ChemicalIngredient> children;

    /**
     * @param children Ingredients to form a union from.
     */
    public CompoundChemicalIngredient(List<ChemicalIngredient> children) {
        if (children.size() < 2) {
            throw new IllegalArgumentException("Compound chemical ingredients must have at least two children");
        }
        this.children = children;
    }

    @Override
    public final Stream<Holder<Chemical>> generateChemicalHolders() {
        return children().stream()
              .flatMap(ChemicalIngredient::generateChemicalHolders)
              .distinct();//Ensure we don't include the same chemical multiple times. Holder overrides #equals at least within same kind of holder
    }

    @Override
    public final boolean test(Holder<Chemical> chemical) {
        for (ChemicalIngredient child : children()) {
            if (child.test(chemical)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@return all the child ingredients that this ingredient is a union of}
     */
    public final List<ChemicalIngredient> children() {
        return children;
    }

    @Override
    public MapCodec<? extends ChemicalIngredient> codec() {
        return CODEC;
    }

    @Override
    public void logMissingTags() {
        children.forEach(ChemicalIngredient::logMissingTags);
    }

    @Override
    public int hashCode() {
        return children.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return children.equals(((CompoundChemicalIngredient) obj).children);
    }
}
