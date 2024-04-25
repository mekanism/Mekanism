package mekanism.common.recipe.ingredient.creator;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientInfo;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient.MultiInfusionStackIngredient;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

@NothingNullByDefault
public class InfusionStackIngredientCreator extends ChemicalStackIngredientCreator<InfuseType, InfusionStack, InfusionStackIngredient> {

    public static final InfusionStackIngredientCreator INSTANCE = new InfusionStackIngredientCreator();

    private InfusionStackIngredientCreator() {
        super(SingleInfusionStackIngredient.CODEC, TaggedInfusionStackIngredient.CODEC, codec -> MultiChemicalStackIngredient.makeCodec(codec, MultiInfusionStackIngredient::new),
              SingleInfusionStackIngredient.STREAM_CODEC, TaggedInfusionStackIngredient.STREAM_CODEC, MultiInfusionStackIngredient.STREAM_CODEC,
              SingleInfusionStackIngredient.class, TaggedInfusionStackIngredient.class, MultiInfusionStackIngredient.class, InfusionStackIngredient.class);
    }

    @Override
    protected ChemicalIngredientDeserializer<InfuseType, InfusionStack, InfusionStackIngredient> getDeserializer() {
        return ChemicalIngredientDeserializer.INFUSION;
    }

    @Override
    public InfusionStackIngredient from(IChemicalProvider<InfuseType> provider, long amount) {
        Objects.requireNonNull(provider, "InfusionStackIngredients cannot be created from a null chemical provider.");
        InfuseType infuseType = provider.getChemical();
        assertNonEmpty(infuseType);
        assertPositiveAmount(amount);
        return new SingleInfusionStackIngredient(infuseType.getStack(amount));
    }

    @Override
    public InfusionStackIngredient from(TagKey<InfuseType> tag, long amount) {
        Objects.requireNonNull(tag, "InfusionStackIngredients cannot be created from a null tag.");
        assertPositiveAmount(amount);
        return new TaggedInfusionStackIngredient(tag, amount);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Converts a stream of ingredients into a single ingredient by converting the stream to an array and calling
     * {@link #createMulti(InfusionStackIngredient[])}.
     */
    @Override
    public InfusionStackIngredient from(Stream<InfusionStackIngredient> ingredients) {
        return createMulti(ingredients.toArray(InfusionStackIngredient[]::new));
    }

    public static class SingleInfusionStackIngredient extends SingleChemicalStackIngredient<InfuseType, InfusionStack> implements InfusionStackIngredient {

        //Note: This must be a lazily initialized so that this class can be loaded in tests
        public static Codec<SingleInfusionStackIngredient> CODEC = Codec.lazyInitialized(() -> makeCodec(ChemicalUtils.INFUSION_STACK_CODEC, SingleInfusionStackIngredient::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, SingleInfusionStackIngredient> STREAM_CODEC = NeoForgeStreamCodecs.lazy(() ->
              makeStreamCodec(ChemicalUtils.INFUSION_STACK_STREAM_CODEC, SingleInfusionStackIngredient::new)
        );

        private SingleInfusionStackIngredient(InfusionStack stack) {
            super(stack);
        }

        @Override
        protected ChemicalIngredientInfo<InfuseType, InfusionStack> getIngredientInfo() {
            return ChemicalIngredientInfo.INFUSION;
        }
    }

    public static class TaggedInfusionStackIngredient extends TaggedChemicalStackIngredient<InfuseType, InfusionStack> implements InfusionStackIngredient {

        //Note: This must be a lazily initialized so that this class can be loaded in tests
        public static Codec<TaggedInfusionStackIngredient> CODEC = Codec.lazyInitialized(() -> makeCodec(MekanismAPI.INFUSE_TYPE_REGISTRY_NAME, TaggedInfusionStackIngredient::new));
        public static StreamCodec<RegistryFriendlyByteBuf, TaggedInfusionStackIngredient> STREAM_CODEC = NeoForgeStreamCodecs.lazy(() ->
              makeStreamCodec(MekanismAPI.INFUSE_TYPE_REGISTRY_NAME, TaggedInfusionStackIngredient::new)
        );

        private TaggedInfusionStackIngredient(TagKey<InfuseType> tag, long amount) {
            super(MekanismAPI.INFUSE_TYPE_REGISTRY.getOrCreateTag(tag), amount);
        }

        @Override
        protected ChemicalIngredientInfo<InfuseType, InfusionStack> getIngredientInfo() {
            return ChemicalIngredientInfo.INFUSION;
        }
    }
}