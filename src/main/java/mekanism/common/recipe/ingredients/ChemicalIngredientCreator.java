package mekanism.common.recipe.ingredients;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.chemical.ChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.DifferenceChemicalIngredient;
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
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

@NothingNullByDefault
public class ChemicalIngredientCreator implements IChemicalIngredientCreator {

    public static final ChemicalIngredientCreator INSTANCE = new ChemicalIngredientCreator();

    private static final MapCodec<ChemicalIngredient> SINGLE_OR_TAG_CODEC = NeoForgeExtraCodecs.xor(SingleChemicalIngredient.CODEC, TagChemicalIngredient.CODEC).flatXmap(
          either -> DataResult.success(either.map(i -> i, ChemicalIngredient.class::cast)),
          ingredient -> {
              if (ingredient instanceof SingleChemicalIngredient singleIngredient) {
                  return DataResult.success(Either.left(singleIngredient));
              } else if (ingredient instanceof TagChemicalIngredient tagIngredient) {
                  return DataResult.success(Either.right(tagIngredient));
              }
              return DataResult.error(() -> "Basic chemical ingredient should be either a chemical or a tag!");
          });

    private static final MapCodec<ChemicalIngredient> MAP_CODEC_NONEMPTY = NeoForgeExtraCodecs.dispatchMapOrElse(MekanismAPI.CHEMICAL_INGREDIENT_TYPES.byNameCodec(), ChemicalIngredient::codec,
          Function.identity(), SINGLE_OR_TAG_CODEC).xmap(
          either -> either.map(Function.identity(), Function.identity()),
          ingredient -> {
              // prefer serializing without a type field, if possible
              if (ingredient instanceof SingleChemicalIngredient || ingredient instanceof TagChemicalIngredient) {
                  return Either.right(ingredient);
              }
              return Either.left(ingredient);
          }
    ).validate(ingredient -> {
        if (ingredient.isEmpty()) {
            return DataResult.error(() -> "Cannot serialize empty chemical ingredient using the map codec");
        }
        return DataResult.success(ingredient);
    });
    private static final Codec<ChemicalIngredient> MAP_CODEC_CODEC = MAP_CODEC_NONEMPTY.codec();

    private static final Codec<List<ChemicalIngredient>> LIST_CODEC = MAP_CODEC_CODEC.listOf();
    private static final Codec<List<ChemicalIngredient>> LIST_CODEC_NON_EMPTY = ExtraCodecs.nonEmptyList(LIST_CODEC);
    private static final Codec<List<ChemicalIngredient>> LIST_CODEC_MULTIPLE_ELEMENTS = LIST_CODEC.validate(list -> list.size() < 2 ? DataResult.error(() -> "List must have multiple elements") : DataResult.success(list));

    /**
     * Full codec representing a gas ingredient in all possible forms.
     * <p>
     * Allows for arrays of gas ingredients to be read as a {@link CompoundChemicalIngredient}, as well as for the {@code type} field to be left out in case of a single
     * gas or tag ingredient.
     *
     * @see #MAP_CODEC_NONEMPTY
     */
    private static final Codec<ChemicalIngredient> CODEC = codec(LIST_CODEC);
    /**
     * Same as {@link #CODEC}, except not allowing for empty ingredients ({@code []}) to be specified.
     *
     * @see #CODEC
     */
    private static final Codec<ChemicalIngredient> CODEC_NON_EMPTY = codec(LIST_CODEC_NON_EMPTY);

    private static Codec<ChemicalIngredient> codec(Codec<List<ChemicalIngredient>> listCodec) {
        // [{...}, {...}] is turned into a CompoundChemicalIngredient instance
        return Codec.either(listCodec, MAP_CODEC_CODEC).xmap(either -> either.map(INSTANCE::ofIngredients, Function.identity()), ingredient -> {
            // serialize CompoundChemicalIngredient instances as an array over their children
            if (ingredient instanceof CompoundChemicalIngredient compound) {
                return Either.left(compound.children());
            } else if (ingredient.isEmpty()) {
                // serialize empty ingredients as []
                return Either.left(Collections.emptyList());
            }
            return Either.right(ingredient);
        });
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, ChemicalIngredient> STREAM_CODEC = Chemical.HOLDER_STREAM_CODEC
          .apply(ByteBufCodecs.<RegistryFriendlyByteBuf, Holder<Chemical>, List<Holder<Chemical>>>collection(NonNullList::createWithCapacity)).map(
                chemicals -> INSTANCE.ofHolders(chemicals.stream()),
                ChemicalIngredient::getChemicalHolders
          );

    @Override
    public MapCodec<ChemicalIngredient> singleOrTagCodec() {
        return SINGLE_OR_TAG_CODEC;
    }

    @Override
    public MapCodec<ChemicalIngredient> mapCodecNonEmpty() {
        return MAP_CODEC_NONEMPTY;
    }

    @Override
    public Codec<List<ChemicalIngredient>> listCodec() {
        return LIST_CODEC;
    }

    @Override
    public Codec<List<ChemicalIngredient>> listCodecNonEmpty() {
        return LIST_CODEC_NON_EMPTY;
    }

    @Override
    public Codec<List<ChemicalIngredient>> listCodecMultipleElements() {
        return LIST_CODEC_MULTIPLE_ELEMENTS;
    }

    @Override
    public Codec<ChemicalIngredient> codec() {
        return CODEC;
    }

    @Override
    public Codec<ChemicalIngredient> codecNonEmpty() {
        return CODEC_NON_EMPTY;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ChemicalIngredient> streamCodec() {
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
    public CompoundChemicalIngredient compound(List<ChemicalIngredient> children) {
        Objects.requireNonNull(children, "children cannot be null");
        return new CompoundChemicalIngredient(children);
    }

    @Override
    public ChemicalIngredient difference(ChemicalIngredient base, ChemicalIngredient subtracted) {
        Objects.requireNonNull(base, "base ingredient cannot be null");
        Objects.requireNonNull(subtracted, "subtracted ingredient cannot be null");
        return new DifferenceChemicalIngredient(base, subtracted);
    }

    @Override
    public ChemicalIngredient intersection(ChemicalIngredient... ingredients) {
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create an IntersectionChemicalIngredient with no children, use IChemicalIngredientCreator#empty() to create an empty ingredient");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        return new IntersectionChemicalIngredient(List.of(ingredients));
    }

    @Override
    public ChemicalIngredient intersection(List<? extends ChemicalIngredient> ingredients) {
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an IntersectionChemicalIngredient with no children, use IChemicalIngredientCreator#empty() to create an empty ingredient");
        } else if (ingredients.size() == 1) {
            return ingredients.getFirst();
        }
        return new IntersectionChemicalIngredient(List.copyOf(ingredients));
    }
}
