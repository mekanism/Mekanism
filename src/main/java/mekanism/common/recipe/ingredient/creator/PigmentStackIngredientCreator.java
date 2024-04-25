package mekanism.common.recipe.ingredient.creator;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientInfo;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient.MultiPigmentStackIngredient;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

@NothingNullByDefault
public class PigmentStackIngredientCreator extends ChemicalStackIngredientCreator<Pigment, PigmentStack, PigmentStackIngredient> {

    public static final PigmentStackIngredientCreator INSTANCE = new PigmentStackIngredientCreator();

    private PigmentStackIngredientCreator() {
        super(SinglePigmentStackIngredient.CODEC, TaggedPigmentStackIngredient.CODEC, codec -> MultiChemicalStackIngredient.makeCodec(codec, MultiPigmentStackIngredient::new),
              SinglePigmentStackIngredient.STREAM_CODEC, TaggedPigmentStackIngredient.STREAM_CODEC, MultiPigmentStackIngredient.STREAM_CODEC,
              SinglePigmentStackIngredient.class, TaggedPigmentStackIngredient.class, MultiPigmentStackIngredient.class, PigmentStackIngredient.class);
    }

    @Override
    protected ChemicalIngredientDeserializer<Pigment, PigmentStack, PigmentStackIngredient> getDeserializer() {
        return ChemicalIngredientDeserializer.PIGMENT;
    }

    @Override
    public PigmentStackIngredient from(IChemicalProvider<Pigment> provider, long amount) {
        Objects.requireNonNull(provider, "PigmentStackIngredients cannot be created from a null chemical provider.");
        Pigment pigment = provider.getChemical();
        assertNonEmpty(pigment);
        assertPositiveAmount(amount);
        return new SinglePigmentStackIngredient(pigment.getStack(amount));
    }

    @Override
    public PigmentStackIngredient from(TagKey<Pigment> tag, long amount) {
        Objects.requireNonNull(tag, "PigmentStackIngredients cannot be created from a null tag.");
        assertPositiveAmount(amount);
        return new TaggedPigmentStackIngredient(tag, amount);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Converts a stream of ingredients into a single ingredient by converting the stream to an array and calling
     * {@link #createMulti(PigmentStackIngredient[])}.
     */
    @Override
    public PigmentStackIngredient from(Stream<PigmentStackIngredient> ingredients) {
        return createMulti(ingredients.toArray(PigmentStackIngredient[]::new));
    }

    public static class SinglePigmentStackIngredient extends SingleChemicalStackIngredient<Pigment, PigmentStack> implements PigmentStackIngredient {

        //Note: This must be a lazily initialized so that this class can be loaded in tests
        public static Codec<SinglePigmentStackIngredient> CODEC = Codec.lazyInitialized(() -> makeCodec(ChemicalUtils.PIGMENT_STACK_CODEC, SinglePigmentStackIngredient::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, SinglePigmentStackIngredient> STREAM_CODEC = NeoForgeStreamCodecs.lazy(() ->
              makeStreamCodec(ChemicalUtils.PIGMENT_STACK_STREAM_CODEC, SinglePigmentStackIngredient::new)
        );

        private SinglePigmentStackIngredient(PigmentStack stack) {
            super(stack);
        }

        @Override
        protected ChemicalIngredientInfo<Pigment, PigmentStack> getIngredientInfo() {
            return ChemicalIngredientInfo.PIGMENT;
        }
    }

    public static class TaggedPigmentStackIngredient extends TaggedChemicalStackIngredient<Pigment, PigmentStack> implements PigmentStackIngredient {

        //Note: This must be a lazily initialized so that this class can be loaded in tests
        public static Codec<TaggedPigmentStackIngredient> CODEC = Codec.lazyInitialized(() -> makeCodec(MekanismAPI.PIGMENT_REGISTRY_NAME, TaggedPigmentStackIngredient::new));
        public static StreamCodec<RegistryFriendlyByteBuf, TaggedPigmentStackIngredient> STREAM_CODEC = NeoForgeStreamCodecs.lazy(() ->
              makeStreamCodec(MekanismAPI.PIGMENT_REGISTRY_NAME, TaggedPigmentStackIngredient::new)
        );

        private TaggedPigmentStackIngredient(TagKey<Pigment> tag, long amount) {
            super(MekanismAPI.PIGMENT_REGISTRY.getOrCreateTag(tag), amount);
        }

        @Override
        protected ChemicalIngredientInfo<Pigment, PigmentStack> getIngredientInfo() {
            return ChemicalIngredientInfo.PIGMENT;
        }
    }
}