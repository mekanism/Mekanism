package mekanism.common.recipe.ingredients;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.TagChemicalIngredient;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

public class ChemicalIngredientUtil {

    private ChemicalIngredientUtil() {
    }

    //TODO - 1.20.5: Test this
    @SuppressWarnings("unchecked")
    public static <CHEMICAL extends Chemical<CHEMICAL>, INGREDIENT extends IChemicalIngredient<CHEMICAL, INGREDIENT>, SINGLE extends SingleChemicalIngredient<CHEMICAL, INGREDIENT>,
          TAG extends TagChemicalIngredient<CHEMICAL, INGREDIENT>> MapCodec<INGREDIENT> singleOrTagCodec(MapCodec<SINGLE> singleCodec, MapCodec<TAG> tagCodec) {
        return NeoForgeExtraCodecs.xor(singleCodec, tagCodec).flatXmap(
              either -> DataResult.success(either.map(i -> (INGREDIENT) i, i -> (INGREDIENT) i)),
              ingredient -> {
                  if (ingredient instanceof SingleChemicalIngredient) {
                      return DataResult.success(Either.left((SINGLE) ingredient));
                  } else if (ingredient instanceof TagChemicalIngredient) {
                      return DataResult.success(Either.right((TAG) ingredient));
                  }
                  return DataResult.error(() -> "Basic chemical ingredient should be either a chemical or a tag!");
              });
    }

    @SuppressWarnings("RedundantTypeArguments")
    public static <CHEMICAL extends Chemical<CHEMICAL>, INGREDIENT extends IChemicalIngredient<CHEMICAL, INGREDIENT>> MapCodec<INGREDIENT> makeMapCodec(
          Registry<MapCodec<? extends INGREDIENT>> typeRegistry, MapCodec<INGREDIENT> singleOrTagCodec) {
        return NeoForgeExtraCodecs.<MapCodec<? extends INGREDIENT>, INGREDIENT, INGREDIENT>dispatchMapOrElse(typeRegistry.byNameCodec(), IChemicalIngredient::codec,
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
    public static <CHEMICAL extends Chemical<CHEMICAL>, INGREDIENT extends IChemicalIngredient<CHEMICAL, INGREDIENT>> Codec<INGREDIENT> codec(Codec<List<INGREDIENT>> listCodec,
          Codec<INGREDIENT> mapCodecCodec, Function<List<? extends INGREDIENT>, INGREDIENT> compoundCreator) {
        // [{...}, {...}] is turned into a CompoundChemicalIngredient instance
        return Codec.either(listCodec, mapCodecCodec).xmap(either -> either.map(compoundCreator, Function.identity()), ingredient -> {
            // serialize CompoundChemicalIngredient instances as an array over their children
            if (ingredient instanceof CompoundChemicalIngredient<?, ?> compound) {
                return Either.left((List<INGREDIENT>) compound.children());
            } else if (ingredient.isEmpty()) {
                // serialize empty ingredients as []
                return Either.left(Collections.emptyList());
            }
            return Either.right(ingredient);
        });
    }
}