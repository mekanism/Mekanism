package mekanism.common.recipe.ingredient.creator;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraft.network.FriendlyByteBuf;

@NothingNullByDefault
public abstract class ChemicalStackIngredientCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>> implements IChemicalStackIngredientCreator<CHEMICAL, STACK, INGREDIENT> {

    private final Codec<INGREDIENT> myCodec;

    protected <ING_STACKED extends SingleChemicalStackIngredient<CHEMICAL, STACK>,
          ING_TAGGED extends TaggedChemicalStackIngredient<CHEMICAL, STACK>,
          MULTI extends MultiChemicalStackIngredient<CHEMICAL, STACK, INGREDIENT>>
    ChemicalStackIngredientCreator(Codec<ING_STACKED> stackCodec, Codec<ING_TAGGED> taggedCodec, Function<Codec<INGREDIENT>, Codec<MULTI>> multiCodecSupplier,
          Class<ING_STACKED> stackedClass, Class<ING_TAGGED> taggedClass, Class<MULTI> multiClass, Class<INGREDIENT> ingredientClass) {
        Codec<INGREDIENT> joinedSingle = Codec.either(stackCodec, taggedCodec).xmap(
              either -> either.map(Function.identity(), Function.identity()),
              input -> {
                  if (input instanceof SingleChemicalStackIngredient<?, ?> stack) {
                      return Either.left(stackedClass.cast(stack));
                  }
                  return Either.right(taggedClass.cast(input));
              }
        ).xmap(ingredientClass::cast, Function.identity());
        myCodec = Codec.either(joinedSingle, multiCodecSupplier.apply(joinedSingle)).xmap(
              either -> either.map(Function.identity(), multi -> {
                  //unbox if we only got one
                  if (multi.getIngredients().size() == 1) {
                      return multi.getIngredients().get(0);
                  }
                  return multi;
              }),
              input -> {
                  if (input instanceof MultiChemicalStackIngredient<?, ?, ?> multi) {
                      return Either.right(multiClass.cast(multi));
                  }
                  return Either.left(ingredientClass.cast(input));
              }
        ).xmap(ingredientClass::cast, Function.identity());
    }

    protected abstract ChemicalIngredientDeserializer<CHEMICAL, STACK, INGREDIENT> getDeserializer();

    @Override
    public INGREDIENT read(FriendlyByteBuf buffer) {
        return getDeserializer().read(buffer);
    }

    @Override
    public Codec<INGREDIENT> codec() {
        return myCodec;
    }

    @Override
    @SafeVarargs
    public final INGREDIENT createMulti(INGREDIENT... ingredients) {
        return getDeserializer().createMulti(ingredients);
    }

    protected final void assertNonEmpty(CHEMICAL chemical) {
        if (chemical.isEmptyType()) {
            throw new IllegalArgumentException("ChemicalStackIngredients cannot be created using the empty chemical.");
        }
    }

    protected final void assertPositiveAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("ChemicalStackIngredients must have an amount of at least one. Received size was: " + amount);
        }
    }
}