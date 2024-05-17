package mekanism.common.recipe.ingredients.gas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.common.recipe.ingredients.ChemicalIngredientUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

@NothingNullByDefault
public class GasIngredientCreator implements IChemicalIngredientCreator<Gas, IGasIngredient> {

    private GasIngredientCreator() {
    }

    public static final GasIngredientCreator INSTANCE = new GasIngredientCreator();

    private static final MapCodec<IGasIngredient> SINGLE_OR_TAG_CODEC = ChemicalIngredientUtil.singleOrTagCodec(SingleGasIngredient.CODEC, TagGasIngredient.CODEC);

    private static final MapCodec<IGasIngredient> MAP_CODEC_NONEMPTY = ChemicalIngredientUtil.makeMapCodec(MekanismAPI.GAS_INGREDIENT_TYPES, SINGLE_OR_TAG_CODEC);
    private static final Codec<IGasIngredient> MAP_CODEC_CODEC = MAP_CODEC_NONEMPTY.codec();

    private static final Codec<List<IGasIngredient>> LIST_CODEC = MAP_CODEC_CODEC.listOf();
    private static final Codec<List<IGasIngredient>> LIST_CODEC_NON_EMPTY = ExtraCodecs.nonEmptyList(LIST_CODEC);
    private static final Codec<List<IGasIngredient>> LIST_CODEC_MULTIPLE_ELEMENTS = LIST_CODEC.validate(list -> list.size() < 2 ? DataResult.error(() -> "List must have multiple elements") : DataResult.success(list));

    /**
     * Full codec representing a gas ingredient in all possible forms.
     * <p>
     * Allows for arrays of gas ingredients to be read as a {@link CompoundGasIngredient}, as well as for the {@code type} field to be left out in case of a single gas or
     * tag ingredient.
     *
     * @see #codec(Codec)
     * @see #MAP_CODEC_NONEMPTY
     */
    private static final Codec<IGasIngredient> CODEC = ChemicalIngredientUtil.codec(LIST_CODEC, MAP_CODEC_CODEC, INSTANCE::ofIngredients);
    /**
     * Same as {@link #CODEC}, except not allowing for empty ingredients ({@code []}) to be specified.
     *
     * @see #codec(Codec)
     */
    private static final Codec<IGasIngredient> CODEC_NON_EMPTY = ChemicalIngredientUtil.codec(LIST_CODEC_NON_EMPTY, MAP_CODEC_CODEC, INSTANCE::ofIngredients);

    private static final StreamCodec<RegistryFriendlyByteBuf, IGasIngredient> STREAM_CODEC = Gas.STREAM_CODEC
          .apply(ByteBufCodecs.<RegistryFriendlyByteBuf, Gas, List<Gas>>collection(NonNullList::createWithCapacity)).map(
                chemicals -> INSTANCE.of(chemicals.stream()),
                IChemicalIngredient::getChemicals
          );

    @Override
    public IGasIngredient empty() {
        return EmptyGasIngredient.INSTANCE;
    }

    @Override
    public MapCodec<IGasIngredient> singleOrTagCodec() {
        return SINGLE_OR_TAG_CODEC;
    }

    @Override
    public MapCodec<IGasIngredient> mapCodecNonEmpty() {
        return MAP_CODEC_NONEMPTY;
    }

    @Override
    public Codec<List<IGasIngredient>> listCodec() {
        return LIST_CODEC;
    }

    @Override
    public Codec<List<IGasIngredient>> listCodecNonEmpty() {
        return LIST_CODEC_NON_EMPTY;
    }

    @Override
    public Codec<List<IGasIngredient>> listCodecMultipleElements() {
        return LIST_CODEC_MULTIPLE_ELEMENTS;
    }

    @Override
    public Codec<IGasIngredient> codec() {
        return CODEC;
    }

    @Override
    public Codec<IGasIngredient> codecNonEmpty() {
        return CODEC_NON_EMPTY;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, IGasIngredient> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public SingleGasIngredient of(Holder<Gas> holder) {
        Objects.requireNonNull(holder, "holder cannot be null");
        return new SingleGasIngredient(holder);
    }

    @Override
    public TagGasIngredient tag(TagKey<Gas> tag) {
        Objects.requireNonNull(tag, "tag cannot be null");
        return new TagGasIngredient(tag);
    }

    @Override
    public CompoundGasIngredient compound(List<IGasIngredient> children) {
        Objects.requireNonNull(children, "children cannot be null");
        return new CompoundGasIngredient(children);
    }

    @Override
    public IGasIngredient difference(IGasIngredient base, IGasIngredient subtracted) {
        Objects.requireNonNull(base, "base ingredient cannot be null");
        Objects.requireNonNull(subtracted, "subtracted ingredient cannot be null");
        return new DifferenceGasIngredient(base, subtracted);
    }

    @Override
    public IGasIngredient intersection(IGasIngredient... ingredients) {
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create an IntersectionGasIngredient with no children, use IChemicalIngredientCreator#empty() to create an empty ingredient");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        return new IntersectionGasIngredient(List.of(ingredients));
    }

    @Override
    public IGasIngredient intersection(List<? extends IGasIngredient> ingredients) {
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an IntersectionGasIngredient with no children, use IChemicalIngredientCreator#empty() to create an empty ingredient");
        } else if (ingredients.size() == 1) {
            return ingredients.getFirst();
        }
        return new IntersectionGasIngredient(List.copyOf(ingredients));
    }
}