package mekanism.common.recipe.ingredient.creator;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientInfo;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient.MultiGasStackIngredient;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

@NothingNullByDefault
public class GasStackIngredientCreator extends ChemicalStackIngredientCreator<Gas, GasStack, GasStackIngredient> {

    public static final GasStackIngredientCreator INSTANCE = new GasStackIngredientCreator();

    private GasStackIngredientCreator() {
        super(SingleGasStackIngredient.CODEC, TaggedGasStackIngredient.CODEC, codec->MultiGasStackIngredient.makeCodec(codec, MultiGasStackIngredient::new), SingleGasStackIngredient.class, TaggedGasStackIngredient.class, MultiGasStackIngredient.class, GasStackIngredient.class);
    }

    @Override
    protected ChemicalIngredientDeserializer<Gas, GasStack, GasStackIngredient> getDeserializer() {
        return ChemicalIngredientDeserializer.GAS;
    }

    @Override
    public GasStackIngredient from(IChemicalProvider<Gas> provider, long amount) {
        Objects.requireNonNull(provider, "GasStackIngredients cannot be created from a null chemical provider.");
        Gas gas = provider.getChemical();
        assertNonEmpty(gas);
        assertPositiveAmount(amount);
        return new SingleGasStackIngredient(gas.getStack(amount));
    }

    @Override
    public GasStackIngredient from(TagKey<Gas> tag, long amount) {
        Objects.requireNonNull(tag, "GasStackIngredients cannot be created from a null tag.");
        assertPositiveAmount(amount);
        return new TaggedGasStackIngredient(tag, amount);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Converts a stream of ingredients into a single ingredient by converting the stream to an array and calling {@link #createMulti(GasStackIngredient[])}.
     */
    @Override
    public GasStackIngredient from(Stream<GasStackIngredient> ingredients) {
        return createMulti(ingredients.toArray(GasStackIngredient[]::new));
    }

    public static class SingleGasStackIngredient extends SingleChemicalStackIngredient<Gas, GasStack> implements GasStackIngredient {

        //Note: This must be a lazily initialized so that this class can be loaded in tests
        static Codec<SingleGasStackIngredient> CODEC = ExtraCodecs.lazyInitializedCodec(() -> makeCodec(ChemicalUtils.GAS_STACK_CODEC, SingleGasStackIngredient::new));

        private SingleGasStackIngredient(GasStack stack) {
            super(stack);
        }

        @Override
        protected ChemicalIngredientInfo<Gas, GasStack> getIngredientInfo() {
            return ChemicalIngredientInfo.GAS;
        }
    }

    public static class TaggedGasStackIngredient extends TaggedChemicalStackIngredient<Gas, GasStack> implements GasStackIngredient {

        //Note: This must be a lazily initialized so that this class can be loaded in tests
        static Codec<TaggedGasStackIngredient> CODEC = ExtraCodecs.lazyInitializedCodec(() -> makeCodec(ChemicalTags.GAS, TaggedGasStackIngredient::new));

        private TaggedGasStackIngredient(TagKey<Gas> tag, long amount) {
            super(ChemicalTags.GAS, tag, amount);
        }

        @Override
        protected ChemicalIngredientInfo<Gas, GasStack> getIngredientInfo() {
            return ChemicalIngredientInfo.GAS;
        }
    }
}