package mekanism.common.recipe.ingredients;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.DifferenceChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IntersectionChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.TagChemicalIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

public class ChemicalIngredientCreator implements IChemicalIngredientCreator {
    public static final ChemicalIngredientCreator INSTANCE = new ChemicalIngredientCreator();

    private static final MapCodec<IChemicalIngredient> SINGLE_OR_TAG_CODEC = ChemicalIngredientUtil.singleOrTagCodec(SingleChemicalIngredient.CODEC, TagChemicalIngredient.CODEC);

    private static final MapCodec<IChemicalIngredient> MAP_CODEC_NONEMPTY = ChemicalIngredientUtil.makeMapCodec(MekanismAPI.CHEMICAL_INGREDIENT_TYPES, SINGLE_OR_TAG_CODEC);
    private static final Codec<IChemicalIngredient> MAP_CODEC_CODEC = MAP_CODEC_NONEMPTY.codec();

    private static final Codec<List<IChemicalIngredient>> LIST_CODEC = MAP_CODEC_CODEC.listOf();
    private static final Codec<List<IChemicalIngredient>> LIST_CODEC_NON_EMPTY = ExtraCodecs.nonEmptyList(LIST_CODEC);
    private static final Codec<List<IChemicalIngredient>> LIST_CODEC_MULTIPLE_ELEMENTS = LIST_CODEC.validate(list -> list.size() < 2 ? DataResult.error(() -> "List must have multiple elements") : DataResult.success(list));

    /**
     * Full codec representing a gas ingredient in all possible forms.
     * <p>
     * Allows for arrays of gas ingredients to be read as a {@link CompoundChemicalIngredient}, as well as for the {@code type} field to be left out in case of a single gas or
     * tag ingredient.
     *
     * @see #MAP_CODEC_NONEMPTY
     */
    private static final Codec<IChemicalIngredient> CODEC = ChemicalIngredientUtil.codec(LIST_CODEC, MAP_CODEC_CODEC, INSTANCE::ofIngredients);
    /**
     * Same as {@link #CODEC}, except not allowing for empty ingredients ({@code []}) to be specified.
     *
     * @see #CODEC
     */
    private static final Codec<IChemicalIngredient> CODEC_NON_EMPTY = ChemicalIngredientUtil.codec(LIST_CODEC_NON_EMPTY, MAP_CODEC_CODEC, INSTANCE::ofIngredients);

    private static final StreamCodec<RegistryFriendlyByteBuf, IChemicalIngredient> STREAM_CODEC = Chemical.STREAM_CODEC
          .apply(ByteBufCodecs.<RegistryFriendlyByteBuf, Chemical, List<Chemical>>collection(NonNullList::createWithCapacity)).map(
                chemicals -> INSTANCE.of(chemicals.stream()),
                IChemicalIngredient::getChemicals
          );

    @Override
    public MapCodec<IChemicalIngredient> singleOrTagCodec() {
        return SINGLE_OR_TAG_CODEC;
    }

    @Override
    public MapCodec<IChemicalIngredient> mapCodecNonEmpty() {
        return MAP_CODEC_NONEMPTY;
    }

    @Override
    public Codec<List<IChemicalIngredient>> listCodec() {
        return LIST_CODEC;
    }

    @Override
    public Codec<List<IChemicalIngredient>> listCodecNonEmpty() {
        return LIST_CODEC_NON_EMPTY;
    }

    @Override
    public Codec<List<IChemicalIngredient>> listCodecMultipleElements() {
        return LIST_CODEC_MULTIPLE_ELEMENTS;
    }

    @Override
    public Codec<IChemicalIngredient> codec() {
        return CODEC;
    }

    @Override
    public Codec<IChemicalIngredient> codecNonEmpty() {
        return CODEC_NON_EMPTY;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, IChemicalIngredient> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public SingleChemicalIngredient of(Holder<Chemical> holder) {
        Objects.requireNonNull(holder, "holder cannot be null");
        return new SingleChemicalIngredient(holder);
    }

    @Override
    public TagChemicalIngredient tag(TagKey<Chemical> tag) {
        Objects.requireNonNull(tag, "tag cannot be null");
        return new TagChemicalIngredient(tag);
    }

    @Override
    public CompoundChemicalIngredient compound(List<IChemicalIngredient> children) {
        Objects.requireNonNull(children, "children cannot be null");
        return new CompoundChemicalIngredient(children);
    }

    @Override
    public IChemicalIngredient difference(IChemicalIngredient base, IChemicalIngredient subtracted) {
        Objects.requireNonNull(base, "base ingredient cannot be null");
        Objects.requireNonNull(subtracted, "subtracted ingredient cannot be null");
        return new DifferenceChemicalIngredient(base, subtracted);
    }

    @Override
    public IChemicalIngredient intersection(IChemicalIngredient... ingredients) {
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create an IntersectionChemicalIngredient with no children, use IChemicalIngredientCreator#empty() to create an empty ingredient");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        return new IntersectionChemicalIngredient(List.of(ingredients));
    }

    @Override
    public IChemicalIngredient intersection(List<? extends IChemicalIngredient> ingredients) {
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an IntersectionChemicalIngredient with no children, use IChemicalIngredientCreator#empty() to create an empty ingredient");
        } else if (ingredients.size() == 1) {
            return ingredients.getFirst();
        }
        return new IntersectionChemicalIngredient(List.copyOf(ingredients));
    }
}
