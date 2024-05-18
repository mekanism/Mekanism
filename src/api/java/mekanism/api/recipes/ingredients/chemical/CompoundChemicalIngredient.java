package mekanism.api.recipes.ingredients.chemical;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

/**
 * Base Chemical ingredient implementation that matches if any of the child ingredients match. This type additionally represents the array notation used in
 * {@linkplain mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator#codec} internally.
 *
 * @see CompoundIngredient CompoundIngredient, its item equivalent
 * @since 10.6.0
 */
@NothingNullByDefault
public abstract non-sealed class CompoundChemicalIngredient<CHEMICAL extends Chemical<CHEMICAL>, INGREDIENT extends IChemicalIngredient<CHEMICAL, INGREDIENT>>
      extends ChemicalIngredient<CHEMICAL, INGREDIENT> {

    /**
     * Helper to create the codec for compound ingredients.
     */
    @Internal
    protected static <INGREDIENT extends IChemicalIngredient<?, INGREDIENT>, COMPOUND extends CompoundChemicalIngredient<?, INGREDIENT>> MapCodec<COMPOUND> codec(
          IChemicalIngredientCreator<?, INGREDIENT> creator, Function<List<INGREDIENT>, COMPOUND> constructor) {
        return NeoForgeExtraCodecs.aliasedFieldOf(creator.listCodecMultipleElements(), JsonConstants.CHILDREN, JsonConstants.INGREDIENTS).xmap(
              constructor, CompoundChemicalIngredient::children
        );
    }

    private final List<INGREDIENT> children;

    /**
     * @param children Ingredients to form a union from.
     */
    @Internal
    protected CompoundChemicalIngredient(List<INGREDIENT> children) {
        if (children.size() < 2) {
            throw new IllegalArgumentException("Compound chemical ingredients must have at least two children");
        }
        this.children = children;
    }

    @Override
    public final Stream<CHEMICAL> generateChemicals() {
        return children().stream()
              .flatMap(IChemicalIngredient::generateChemicals)
              .distinct();//Ensure we don't include the same chemical multiple times
    }

    @Override
    public final boolean test(CHEMICAL chemical) {
        for (INGREDIENT child : children()) {
            if (child.test(chemical)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@return all the child ingredients that this ingredient is a union of}
     */
    public final List<INGREDIENT> children() {
        return children;
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
        return children.equals(((CompoundChemicalIngredient<?, ?>) obj).children);
    }
}
