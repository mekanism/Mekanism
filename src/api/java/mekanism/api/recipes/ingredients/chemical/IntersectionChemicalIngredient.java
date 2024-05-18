package mekanism.api.recipes.ingredients.chemical;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

/**
 * Base Chemical ingredient implementation that matches if all child ingredients match
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public abstract non-sealed class IntersectionChemicalIngredient<CHEMICAL extends Chemical<CHEMICAL>, INGREDIENT extends IChemicalIngredient<CHEMICAL, INGREDIENT>>
      extends ChemicalIngredient<CHEMICAL, INGREDIENT> {

    /**
     * Helper to create the codec for intersection ingredients.
     */
    @Internal
    protected static <INGREDIENT extends IChemicalIngredient<?, INGREDIENT>, INTERSECTION extends IntersectionChemicalIngredient<?, INGREDIENT>> MapCodec<INTERSECTION> codec(
          IChemicalIngredientCreator<?, INGREDIENT> creator, Function<List<INGREDIENT>, INTERSECTION> constructor) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
              creator.listCodecMultipleElements().fieldOf(JsonConstants.CHILDREN).forGetter(IntersectionChemicalIngredient::children)
        ).apply(builder, constructor));
    }

    private final List<INGREDIENT> children;

    /**
     * @param children Ingredients to form an intersection from.
     */
    @Internal
    protected IntersectionChemicalIngredient(List<INGREDIENT> children) {
        if (children.size() < 2) {
            throw new IllegalArgumentException("Intersection chemical ingredients require at least two ingredients");
        }
        this.children = children;
    }

    @Override
    public final boolean test(CHEMICAL chemical) {
        for (INGREDIENT child : children) {
            if (!child.test(chemical)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final Stream<CHEMICAL> generateChemicals() {
        return children.stream()
              .flatMap(IChemicalIngredient::generateChemicals)
              .distinct()//Ensure we don't include the same chemical multiple times
              .filter(this);
    }

    /**
     * {@return all the child ingredients that this ingredient is an intersection of}
     */
    public final List<INGREDIENT> children() {
        return children;
    }

    @Override
    public int hashCode() {
        return Objects.hash(children);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return children.equals(((IntersectionChemicalIngredient<?, ?>) obj).children);
    }
}
