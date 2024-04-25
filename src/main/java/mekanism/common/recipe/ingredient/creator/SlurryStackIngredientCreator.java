package mekanism.common.recipe.ingredient.creator;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient.MultiSlurryStackIngredient;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

@NothingNullByDefault
public class SlurryStackIngredientCreator extends ChemicalStackIngredientCreator<Slurry, SlurryStack, SlurryStackIngredient> {

    public static final SlurryStackIngredientCreator INSTANCE = new SlurryStackIngredientCreator();

    private SlurryStackIngredientCreator() {
        super(SingleSlurryStackIngredient.CODEC, TaggedSlurryStackIngredient.CODEC, codec -> MultiChemicalStackIngredient.makeCodec(codec, MultiSlurryStackIngredient::new),
              SingleSlurryStackIngredient.STREAM_CODEC, TaggedSlurryStackIngredient.STREAM_CODEC, MultiSlurryStackIngredient.STREAM_CODEC,
              SingleSlurryStackIngredient.class, TaggedSlurryStackIngredient.class, MultiSlurryStackIngredient.class, SlurryStackIngredient.class);
    }

    @Override
    protected SlurryStackIngredient createMultiInternal(List<SlurryStackIngredient> cleanedIngredients) {
        return new MultiSlurryStackIngredient(cleanedIngredients);
    }

    @Override
    public SlurryStackIngredient from(IChemicalProvider<Slurry> provider, long amount) {
        Objects.requireNonNull(provider, "SlurryStackIngredients cannot be created from a null chemical provider.");
        Slurry slurry = provider.getChemical();
        assertNonEmpty(slurry);
        assertPositiveAmount(amount);
        return new SingleSlurryStackIngredient(slurry.getStack(amount));
    }

    @Override
    public SlurryStackIngredient from(TagKey<Slurry> tag, long amount) {
        Objects.requireNonNull(tag, "SlurryStackIngredients cannot be created from a null tag.");
        assertPositiveAmount(amount);
        return new TaggedSlurryStackIngredient(tag, amount);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Converts a stream of ingredients into a single ingredient by converting the stream to an array and calling
     * {@link #createMulti(SlurryStackIngredient[])}.
     */
    @Override
    public SlurryStackIngredient from(Stream<SlurryStackIngredient> ingredients) {
        return createMulti(ingredients.toArray(SlurryStackIngredient[]::new));
    }

    public static class SingleSlurryStackIngredient extends SingleChemicalStackIngredient<Slurry, SlurryStack> implements SlurryStackIngredient {

        //Note: This must be a lazily initialized so that this class can be loaded in tests
        public static Codec<SingleSlurryStackIngredient> CODEC = Codec.lazyInitialized(() -> makeCodec(SlurryStack.MAP_CODEC, SingleSlurryStackIngredient::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, SingleSlurryStackIngredient> STREAM_CODEC = NeoForgeStreamCodecs.lazy(() ->
              makeStreamCodec(SlurryStack.STREAM_CODEC, SingleSlurryStackIngredient::new)
        );

        private SingleSlurryStackIngredient(SlurryStack stack) {
            super(stack);
        }
    }

    public static class TaggedSlurryStackIngredient extends TaggedChemicalStackIngredient<Slurry, SlurryStack> implements SlurryStackIngredient {

        //Note: This must be a lazily initialized so that this class can be loaded in tests
        public static Codec<TaggedSlurryStackIngredient> CODEC = Codec.lazyInitialized(() -> makeCodec(MekanismAPI.SLURRY_REGISTRY_NAME, TaggedSlurryStackIngredient::new));
        public static StreamCodec<RegistryFriendlyByteBuf, TaggedSlurryStackIngredient> STREAM_CODEC = NeoForgeStreamCodecs.lazy(() ->
              makeStreamCodec(MekanismAPI.SLURRY_REGISTRY_NAME, TaggedSlurryStackIngredient::new)
        );

        private TaggedSlurryStackIngredient(TagKey<Slurry> tag, long amount) {
            super(MekanismAPI.SLURRY_REGISTRY.getOrCreateTag(tag), amount);
        }
    }
}