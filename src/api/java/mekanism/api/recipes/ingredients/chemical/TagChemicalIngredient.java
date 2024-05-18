package mekanism.api.recipes.ingredients.chemical;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import java.util.stream.Stream;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
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

    /**
     * Helper to create the codec for tag ingredients.
     */
    @Internal
    protected static <CHEMICAL extends Chemical<CHEMICAL>, TAG extends TagChemicalIngredient<CHEMICAL, ?>> MapCodec<TAG> codec(
          ResourceKey<? extends Registry<CHEMICAL>> registryName, Function<TagKey<CHEMICAL>, TAG> constructor) {
        return TagKey.codec(registryName).xmap(constructor, TagChemicalIngredient::tag).fieldOf(JsonConstants.TAG);
    }

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
