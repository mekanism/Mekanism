package mekanism.common.recipe.ingredients.infusion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IInfusionIngredient;
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
public class InfusionIngredientCreator implements IChemicalIngredientCreator<InfuseType, IInfusionIngredient> {

    private InfusionIngredientCreator() {
    }

    public static final InfusionIngredientCreator INSTANCE = new InfusionIngredientCreator();

    private static final MapCodec<IInfusionIngredient> SINGLE_OR_TAG_CODEC = ChemicalIngredientUtil.singleOrTagCodec(SingleInfusionIngredient.CODEC, TagInfusionIngredient.CODEC);

    private static final MapCodec<IInfusionIngredient> MAP_CODEC_NONEMPTY = ChemicalIngredientUtil.makeMapCodec(MekanismAPI.INFUSION_INGREDIENT_TYPES, SINGLE_OR_TAG_CODEC);
    private static final Codec<IInfusionIngredient> MAP_CODEC_CODEC = MAP_CODEC_NONEMPTY.codec();

    private static final Codec<List<IInfusionIngredient>> LIST_CODEC = MAP_CODEC_CODEC.listOf();
    private static final Codec<List<IInfusionIngredient>> LIST_CODEC_NON_EMPTY = ExtraCodecs.nonEmptyList(LIST_CODEC);
    private static final Codec<List<IInfusionIngredient>> LIST_CODEC_MULTIPLE_ELEMENTS = LIST_CODEC.validate(list -> list.size() < 2 ? DataResult.error(() -> "List must have multiple elements") : DataResult.success(list));

    private static final Codec<IInfusionIngredient> CODEC = ChemicalIngredientUtil.codec(LIST_CODEC, MAP_CODEC_CODEC, INSTANCE::ofIngredients);
    private static final Codec<IInfusionIngredient> CODEC_NON_EMPTY = ChemicalIngredientUtil.codec(LIST_CODEC_NON_EMPTY, MAP_CODEC_CODEC, INSTANCE::ofIngredients);

    private static final StreamCodec<RegistryFriendlyByteBuf, IInfusionIngredient> STREAM_CODEC = InfuseType.STREAM_CODEC
          .apply(ByteBufCodecs.<RegistryFriendlyByteBuf, InfuseType, List<InfuseType>>collection(NonNullList::createWithCapacity)).map(
                chemicals -> INSTANCE.of(chemicals.stream()),
                IChemicalIngredient::getChemicals
          );

    @Override
    public IInfusionIngredient empty() {
        return EmptyInfusionIngredient.INSTANCE;
    }

    @Override
    public MapCodec<IInfusionIngredient> singleOrTagCodec() {
        return SINGLE_OR_TAG_CODEC;
    }

    @Override
    public MapCodec<IInfusionIngredient> mapCodecNonEmpty() {
        return MAP_CODEC_NONEMPTY;
    }

    @Override
    public Codec<List<IInfusionIngredient>> listCodec() {
        return LIST_CODEC;
    }

    @Override
    public Codec<List<IInfusionIngredient>> listCodecNonEmpty() {
        return LIST_CODEC_NON_EMPTY;
    }

    @Override
    public Codec<List<IInfusionIngredient>> listCodecMultipleElements() {
        return LIST_CODEC_MULTIPLE_ELEMENTS;
    }

    @Override
    public Codec<IInfusionIngredient> codec() {
        return CODEC;
    }

    @Override
    public Codec<IInfusionIngredient> codecNonEmpty() {
        return CODEC_NON_EMPTY;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, IInfusionIngredient> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public SingleInfusionIngredient of(Holder<InfuseType> holder) {
        Objects.requireNonNull(holder, "holder cannot be null");
        return new SingleInfusionIngredient(holder);
    }

    @Override
    public TagInfusionIngredient tag(TagKey<InfuseType> tag) {
        Objects.requireNonNull(tag, "tag cannot be null");
        return new TagInfusionIngredient(tag);
    }

    @Override
    public CompoundInfusionIngredient compound(List<IInfusionIngredient> children) {
        Objects.requireNonNull(children, "children cannot be null");
        return new CompoundInfusionIngredient(children);
    }

    @Override
    public IInfusionIngredient difference(IInfusionIngredient base, IInfusionIngredient subtracted) {
        Objects.requireNonNull(base, "base ingredient cannot be null");
        Objects.requireNonNull(subtracted, "subtracted ingredient cannot be null");
        return new DifferenceInfusionIngredient(base, subtracted);
    }

    @Override
    public IInfusionIngredient intersection(IInfusionIngredient... ingredients) {
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create an IntersectionInfusionIngredient with no children, use IChemicalIngredientCreator#empty() to create an empty ingredient");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        return new IntersectionInfusionIngredient(List.of(ingredients));
    }

    @Override
    public IInfusionIngredient intersection(List<? extends IInfusionIngredient> ingredients) {
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an IntersectionInfusionIngredient with no children, use IChemicalIngredientCreator#empty() to create an empty ingredient");
        } else if (ingredients.size() == 1) {
            return ingredients.getFirst();
        }
        return new IntersectionInfusionIngredient(List.copyOf(ingredients));
    }
}