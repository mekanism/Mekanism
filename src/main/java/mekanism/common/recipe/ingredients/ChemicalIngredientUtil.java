package mekanism.common.recipe.ingredients;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.TagChemicalIngredient;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

public class ChemicalIngredientUtil {

    private ChemicalIngredientUtil() {
    }

    @SuppressWarnings("unchecked")
    public static MapCodec<IChemicalIngredient> singleOrTagCodec(MapCodec<SingleChemicalIngredient> singleCodec, MapCodec<TagChemicalIngredient> tagCodec) {
        return NeoForgeExtraCodecs.xor(singleCodec, tagCodec).flatXmap(
              either -> DataResult.success(either.map(i -> i, IChemicalIngredient.class::cast)),
              ingredient -> {
                  if (ingredient instanceof SingleChemicalIngredient) {
                      return DataResult.success(Either.left((SingleChemicalIngredient) ingredient));
                  } else if (ingredient instanceof TagChemicalIngredient) {
                      return DataResult.success(Either.right((TagChemicalIngredient) ingredient));
                  }
                  return DataResult.error(() -> "Basic chemical ingredient should be either a chemical or a tag!");
              });
    }

    @SuppressWarnings("RedundantTypeArguments")
    public static MapCodec<IChemicalIngredient> makeMapCodec(
          Registry<MapCodec<? extends IChemicalIngredient>> typeRegistry, MapCodec<IChemicalIngredient> singleOrTagCodec) {
        return NeoForgeExtraCodecs.<MapCodec<? extends IChemicalIngredient>, IChemicalIngredient, IChemicalIngredient>dispatchMapOrElse(typeRegistry.byNameCodec(), IChemicalIngredient::codec,
              Function.identity(), singleOrTagCodec).xmap(
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
    }

    @SuppressWarnings("unchecked")
    public static Codec<IChemicalIngredient> codec(Codec<List<IChemicalIngredient>> listCodec,
          Codec<IChemicalIngredient> mapCodecCodec, Function<List<? extends IChemicalIngredient>, IChemicalIngredient> compoundCreator) {
        // [{...}, {...}] is turned into a CompoundChemicalIngredient instance
        return Codec.either(listCodec, mapCodecCodec).xmap(either -> either.map(compoundCreator, Function.identity()), ingredient -> {
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
}