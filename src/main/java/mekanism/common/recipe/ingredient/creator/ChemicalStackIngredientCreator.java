package mekanism.common.recipe.ingredient.creator;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.IngredientType;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public abstract class ChemicalStackIngredientCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>> implements IChemicalStackIngredientCreator<CHEMICAL, STACK, INGREDIENT> {

    private final Codec<INGREDIENT> myCodec;
    private final StreamCodec<RegistryFriendlyByteBuf, INGREDIENT> myStreamCodec;

    protected <ING_STACKED extends SingleChemicalStackIngredient<CHEMICAL, STACK>,
          ING_TAGGED extends TaggedChemicalStackIngredient<CHEMICAL, STACK>,
          MULTI extends MultiChemicalStackIngredient<CHEMICAL, STACK, INGREDIENT>>
    ChemicalStackIngredientCreator(Codec<ING_STACKED> stackCodec, Codec<ING_TAGGED> taggedCodec, Function<Codec<INGREDIENT>, Codec<MULTI>> multiCodecSupplier,
          StreamCodec<RegistryFriendlyByteBuf, ING_STACKED> singleStreamCodec, StreamCodec<RegistryFriendlyByteBuf, ING_TAGGED> taggedStreamCodec,
          StreamCodec<RegistryFriendlyByteBuf, MULTI> multiStreamCodec,
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
        myStreamCodec = IngredientType.STREAM_CODEC.<RegistryFriendlyByteBuf>cast().dispatch(InputIngredient::getType, type -> (StreamCodec<RegistryFriendlyByteBuf, INGREDIENT>) switch (type) {
            case SINGLE -> singleStreamCodec;
            case TAGGED -> taggedStreamCodec;
            case MULTI -> multiStreamCodec;
        });
    }

    @Override
    public Codec<INGREDIENT> codec() {
        return myCodec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, INGREDIENT> streamCodec() {
        return myStreamCodec;
    }

    @Override
    @SafeVarargs
    public final INGREDIENT createMulti(INGREDIENT... ingredients) {
        Objects.requireNonNull(ingredients, "Cannot create a multi ingredient out of a null array.");
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create a multi ingredient out of no ingredients.");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        List<INGREDIENT> cleanedIngredients = new ArrayList<>();
        for (INGREDIENT ingredient : ingredients) {
            if (ingredient instanceof MultiChemicalStackIngredient) {
                //Don't worry about if our inner ingredients are multi as well, as if this is the only external method for
                // creating a multi ingredient, then we are certified they won't be of a higher depth
                cleanedIngredients.addAll(((MultiChemicalStackIngredient<CHEMICAL, STACK, INGREDIENT>) ingredient).getIngredients());
            } else {
                cleanedIngredients.add(ingredient);
            }
        }
        //There should be more than a single ingredient, or we would have split out earlier
        return createMultiInternal(cleanedIngredients);
    }

    protected abstract INGREDIENT createMultiInternal(List<INGREDIENT> cleanedIngredients);

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