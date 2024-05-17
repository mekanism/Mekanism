package mekanism.api.recipes.ingredients.chemical;

import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

/**
 * Base Chemical ingredient implementation that matches all chemicals within the given tag.
 * <p>
 * Unlike with ingredients, this is an explicit "type" of chemical ingredient, though it may still be written without a type field, see
 * {@link mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator#mapCodecNonEmpty}
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public abstract non-sealed class TagChemicalIngredient<CHEMICAL extends Chemical<CHEMICAL>, INGREDIENT extends IChemicalIngredient<CHEMICAL, INGREDIENT>>
      extends ChemicalIngredient<CHEMICAL, INGREDIENT> {

    private final TagKey<CHEMICAL> tag;

    /**
     * @param tag Tag key to match.
     */
    @Internal
    protected TagChemicalIngredient(TagKey<CHEMICAL> tag) {
        this.tag = tag;
    }

    @Override
    public final boolean test(CHEMICAL chemical) {
        return chemical.is(tag());
    }

    @Override
    public final Stream<CHEMICAL> generateChemicals() {
        return registry().getTag(tag())
              .stream()
              .flatMap(HolderSet::stream)
              .map(Holder::value);
    }

    /**
     * {@return tag key to match}
     */
    public final TagKey<CHEMICAL> tag() {
        return tag;
    }

    /**
     * Registry to look up the tag elements from.
     */
    protected abstract Registry<CHEMICAL> registry();

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return tag.equals(((TagChemicalIngredient<?, ?>) obj).tag);
    }
}
