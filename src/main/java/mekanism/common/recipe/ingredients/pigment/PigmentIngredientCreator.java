package mekanism.common.recipe.ingredients.pigment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
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
public class PigmentIngredientCreator implements IChemicalIngredientCreator<Pigment, IPigmentIngredient> {

    private PigmentIngredientCreator() {
    }

    public static final PigmentIngredientCreator INSTANCE = new PigmentIngredientCreator();

    private static final MapCodec<IPigmentIngredient> SINGLE_OR_TAG_CODEC = ChemicalIngredientUtil.singleOrTagCodec(SinglePigmentIngredient.CODEC, TagPigmentIngredient.CODEC);

    private static final MapCodec<IPigmentIngredient> MAP_CODEC_NONEMPTY = ChemicalIngredientUtil.makeMapCodec(MekanismAPI.PIGMENT_INGREDIENT_TYPES, SINGLE_OR_TAG_CODEC);
    private static final Codec<IPigmentIngredient> MAP_CODEC_CODEC = MAP_CODEC_NONEMPTY.codec();

    private static final Codec<List<IPigmentIngredient>> LIST_CODEC = MAP_CODEC_CODEC.listOf();
    private static final Codec<List<IPigmentIngredient>> LIST_CODEC_NON_EMPTY = ExtraCodecs.nonEmptyList(LIST_CODEC);
    private static final Codec<List<IPigmentIngredient>> LIST_CODEC_MULTIPLE_ELEMENTS = LIST_CODEC.validate(list -> list.size() < 2 ? DataResult.error(() -> "List must have multiple elements") : DataResult.success(list));

    private static final Codec<IPigmentIngredient> CODEC = ChemicalIngredientUtil.codec(LIST_CODEC, MAP_CODEC_CODEC, INSTANCE::ofIngredients);
    private static final Codec<IPigmentIngredient> CODEC_NON_EMPTY = ChemicalIngredientUtil.codec(LIST_CODEC_NON_EMPTY, MAP_CODEC_CODEC, INSTANCE::ofIngredients);

    private static final StreamCodec<RegistryFriendlyByteBuf, IPigmentIngredient> STREAM_CODEC = Pigment.STREAM_CODEC
          .apply(ByteBufCodecs.<RegistryFriendlyByteBuf, Pigment, List<Pigment>>collection(NonNullList::createWithCapacity)).map(
                chemicals -> INSTANCE.of(chemicals.stream()),
                IChemicalIngredient::getChemicals
          );

    @Override
    public IPigmentIngredient empty() {
        return EmptyPigmentIngredient.INSTANCE;
    }

    @Override
    public MapCodec<IPigmentIngredient> singleOrTagCodec() {
        return SINGLE_OR_TAG_CODEC;
    }

    @Override
    public MapCodec<IPigmentIngredient> mapCodecNonEmpty() {
        return MAP_CODEC_NONEMPTY;
    }

    @Override
    public Codec<List<IPigmentIngredient>> listCodec() {
        return LIST_CODEC;
    }

    @Override
    public Codec<List<IPigmentIngredient>> listCodecNonEmpty() {
        return LIST_CODEC_NON_EMPTY;
    }

    @Override
    public Codec<List<IPigmentIngredient>> listCodecMultipleElements() {
        return LIST_CODEC_MULTIPLE_ELEMENTS;
    }

    @Override
    public Codec<IPigmentIngredient> codec() {
        return CODEC;
    }

    @Override
    public Codec<IPigmentIngredient> codecNonEmpty() {
        return CODEC_NON_EMPTY;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, IPigmentIngredient> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public SinglePigmentIngredient of(Holder<Pigment> holder) {
        Objects.requireNonNull(holder, "holder cannot be null");
        return new SinglePigmentIngredient(holder);
    }

    @Override
    public TagPigmentIngredient tag(TagKey<Pigment> tag) {
        Objects.requireNonNull(tag, "tag cannot be null");
        return new TagPigmentIngredient(tag);
    }

    @Override
    public CompoundPigmentIngredient compound(List<IPigmentIngredient> children) {
        Objects.requireNonNull(children, "children cannot be null");
        return new CompoundPigmentIngredient(children);
    }

    @Override
    public IPigmentIngredient difference(IPigmentIngredient base, IPigmentIngredient subtracted) {
        Objects.requireNonNull(base, "base ingredient cannot be null");
        Objects.requireNonNull(subtracted, "subtracted ingredient cannot be null");
        return new DifferencePigmentIngredient(base, subtracted);
    }

    @Override
    public IPigmentIngredient intersection(IPigmentIngredient... ingredients) {
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create an IntersectionPigmentIngredient with no children, use IChemicalIngredientCreator#empty() to create an empty ingredient");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        return new IntersectionPigmentIngredient(List.of(ingredients));
    }

    @Override
    public IPigmentIngredient intersection(List<? extends IPigmentIngredient> ingredients) {
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an IntersectionPigmentIngredient with no children, use IChemicalIngredientCreator#empty() to create an empty ingredient");
        } else if (ingredients.size() == 1) {
            return ingredients.getFirst();
        }
        return new IntersectionPigmentIngredient(List.copyOf(ingredients));
    }
}