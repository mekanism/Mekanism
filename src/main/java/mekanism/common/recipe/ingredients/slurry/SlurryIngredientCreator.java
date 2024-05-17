package mekanism.common.recipe.ingredients.slurry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
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
public class SlurryIngredientCreator implements IChemicalIngredientCreator<Slurry, ISlurryIngredient> {

    private SlurryIngredientCreator() {
    }

    public static final SlurryIngredientCreator INSTANCE = new SlurryIngredientCreator();

    private static final MapCodec<ISlurryIngredient> SINGLE_OR_TAG_CODEC = ChemicalIngredientUtil.singleOrTagCodec(SingleSlurryIngredient.CODEC, TagSlurryIngredient.CODEC);

    private static final MapCodec<ISlurryIngredient> MAP_CODEC_NONEMPTY = ChemicalIngredientUtil.makeMapCodec(MekanismAPI.SLURRY_INGREDIENT_TYPES, SINGLE_OR_TAG_CODEC);
    private static final Codec<ISlurryIngredient> MAP_CODEC_CODEC = MAP_CODEC_NONEMPTY.codec();

    private static final Codec<List<ISlurryIngredient>> LIST_CODEC = MAP_CODEC_CODEC.listOf();
    private static final Codec<List<ISlurryIngredient>> LIST_CODEC_NON_EMPTY = ExtraCodecs.nonEmptyList(LIST_CODEC);
    private static final Codec<List<ISlurryIngredient>> LIST_CODEC_MULTIPLE_ELEMENTS = LIST_CODEC.validate(list -> list.size() < 2 ? DataResult.error(() -> "List must have multiple elements") : DataResult.success(list));

    private static final Codec<ISlurryIngredient> CODEC = ChemicalIngredientUtil.codec(LIST_CODEC, MAP_CODEC_CODEC, INSTANCE::ofIngredients);
    private static final Codec<ISlurryIngredient> CODEC_NON_EMPTY = ChemicalIngredientUtil.codec(LIST_CODEC_NON_EMPTY, MAP_CODEC_CODEC, INSTANCE::ofIngredients);

    private static final StreamCodec<RegistryFriendlyByteBuf, ISlurryIngredient> STREAM_CODEC = Slurry.STREAM_CODEC
          .apply(ByteBufCodecs.<RegistryFriendlyByteBuf, Slurry, List<Slurry>>collection(NonNullList::createWithCapacity)).map(
                chemicals -> INSTANCE.of(chemicals.stream()),
                IChemicalIngredient::getChemicals
          );

    @Override
    public ISlurryIngredient empty() {
        return EmptySlurryIngredient.INSTANCE;
    }

    @Override
    public MapCodec<ISlurryIngredient> singleOrTagCodec() {
        return SINGLE_OR_TAG_CODEC;
    }

    @Override
    public MapCodec<ISlurryIngredient> mapCodecNonEmpty() {
        return MAP_CODEC_NONEMPTY;
    }

    @Override
    public Codec<List<ISlurryIngredient>> listCodec() {
        return LIST_CODEC;
    }

    @Override
    public Codec<List<ISlurryIngredient>> listCodecNonEmpty() {
        return LIST_CODEC_NON_EMPTY;
    }

    @Override
    public Codec<List<ISlurryIngredient>> listCodecMultipleElements() {
        return LIST_CODEC_MULTIPLE_ELEMENTS;
    }

    @Override
    public Codec<ISlurryIngredient> codec() {
        return CODEC;
    }

    @Override
    public Codec<ISlurryIngredient> codecNonEmpty() {
        return CODEC_NON_EMPTY;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ISlurryIngredient> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public SingleSlurryIngredient of(Holder<Slurry> holder) {
        Objects.requireNonNull(holder, "holder cannot be null");
        return new SingleSlurryIngredient(holder);
    }

    @Override
    public TagSlurryIngredient tag(TagKey<Slurry> tag) {
        Objects.requireNonNull(tag, "tag cannot be null");
        return new TagSlurryIngredient(tag);
    }

    @Override
    public CompoundSlurryIngredient compound(List<ISlurryIngredient> children) {
        Objects.requireNonNull(children, "children cannot be null");
        return new CompoundSlurryIngredient(children);
    }

    @Override
    public ISlurryIngredient difference(ISlurryIngredient base, ISlurryIngredient subtracted) {
        Objects.requireNonNull(base, "base ingredient cannot be null");
        Objects.requireNonNull(subtracted, "subtracted ingredient cannot be null");
        return new DifferenceSlurryIngredient(base, subtracted);
    }

    @Override
    public ISlurryIngredient intersection(ISlurryIngredient... ingredients) {
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create an IntersectionSlurryIngredient with no children, use IChemicalIngredientCreator#empty() to create an empty ingredient");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        return new IntersectionSlurryIngredient(List.of(ingredients));
    }

    @Override
    public ISlurryIngredient intersection(List<? extends ISlurryIngredient> ingredients) {
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an IntersectionSlurryIngredient with no children, use IChemicalIngredientCreator#empty() to create an empty ingredient");
        } else if (ingredients.size() == 1) {
            return ingredients.getFirst();
        }
        return new IntersectionSlurryIngredient(List.copyOf(ingredients));
    }
}