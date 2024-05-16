package mekanism.common.recipe.ingredient.creator;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.IngredientType;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.ingredients.creator.IFluidStackIngredientCreator;
import mekanism.common.recipe.ingredient.IMultiIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FluidStackIngredientCreator implements IFluidStackIngredientCreator {

    public static final FluidStackIngredientCreator INSTANCE = new FluidStackIngredientCreator();

    private static final Codec<FluidStackIngredient> CODEC = Codec.either(SingleFluidStackIngredient.CODEC, MultiFluidStackIngredient.CODEC).xmap(
          either -> either.map(Function.identity(), multi -> {
              //unbox if we only got one
              if (multi.ingredients.length == 1) {
                  return multi.ingredients[0];
              }
              return multi;
          }),
          input -> {
              if (input instanceof SingleFluidStackIngredient single) {
                  return Either.left(single);
              }
              return Either.right((MultiFluidStackIngredient) input);
          }
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, FluidStackIngredient> STREAM_CODEC = IngredientType.STREAM_CODEC.<RegistryFriendlyByteBuf>cast().dispatch(InputIngredient::getType, type -> switch (type) {
        case SINGLE -> SingleFluidStackIngredient.STREAM_CODEC;
        case MULTI -> MultiFluidStackIngredient.STREAM_CODEC;
        case TAGGED -> throw new IllegalStateException("Unable to process tagged fluid stack ingredients");
    });

    private FluidStackIngredientCreator() {
    }

    @Override
    public Codec<FluidStackIngredient> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FluidStackIngredient> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public FluidStackIngredient from(SizedFluidIngredient ingredient) {
        Objects.requireNonNull(ingredient, "FluidStackIngredients cannot be created from a null ingredient.");
        if (ingredient.ingredient().isEmpty()) {
            throw new IllegalArgumentException("FluidStackIngredients cannot be created using the empty ingredient.");
        }
        return new SingleFluidStackIngredient(ingredient);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Converts a stream of ingredients into a single ingredient by converting the stream to an array and calling {@link #createMulti(FluidStackIngredient[])}.
     */
    @Override
    public FluidStackIngredient createMulti(FluidStackIngredient... ingredients) {
        Objects.requireNonNull(ingredients, "Cannot create a multi ingredient out of a null array.");
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create a multi ingredient out of no ingredients.");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        List<SingleFluidStackIngredient> cleanedIngredients = new ArrayList<>();
        for (FluidStackIngredient ingredient : ingredients) {
            if (ingredient instanceof MultiFluidStackIngredient multi) {
                //Don't worry about if our inner ingredients are multi as well, as if this is the only external method for
                // creating a multi ingredient, then we are certified they won't be of a higher depth
                Collections.addAll(cleanedIngredients, multi.ingredients);
            } else if (ingredient instanceof SingleFluidStackIngredient single) {
                cleanedIngredients.add(single);
            } else {
                throw new IllegalStateException("Unknown ingredient class: " + ingredient);
            }
        }
        //There should be more than a single fluid, or we would have split out earlier
        return new MultiFluidStackIngredient(cleanedIngredients);
    }

    @Override
    public FluidStackIngredient from(Stream<FluidStackIngredient> ingredients) {
        return createMulti(ingredients.toArray(FluidStackIngredient[]::new));
    }

    @NothingNullByDefault
    public static class SingleFluidStackIngredient extends FluidStackIngredient {

        //Note: This must be a lazily initialized so that this class can be loaded in tests
        public static final Codec<SingleFluidStackIngredient> CODEC = Codec.lazyInitialized(() -> SizedFluidIngredient.FLAT_CODEC.xmap(
              SingleFluidStackIngredient::new, SingleFluidStackIngredient::getInputRaw
        ));
        public static final StreamCodec<RegistryFriendlyByteBuf, SingleFluidStackIngredient> STREAM_CODEC = NeoForgeStreamCodecs.lazy(() ->
              SizedFluidIngredient.STREAM_CODEC.map(SingleFluidStackIngredient::new, SingleFluidStackIngredient::getInputRaw)
        );

        private final SizedFluidIngredient ingredient;
        @Nullable
        private List<FluidStack> representations;

        private SingleFluidStackIngredient(SizedFluidIngredient ingredient) {
            this.ingredient = Objects.requireNonNull(ingredient);
        }

        @Override
        public boolean test(FluidStack stack) {
            Objects.requireNonNull(stack);
            return ingredient.test(stack);
        }

        @Override
        public boolean testType(FluidStack stack) {
            Objects.requireNonNull(stack);
            return ingredient.ingredient().test(stack);
        }

        @Override
        public FluidStack getMatchingInstance(FluidStack stack) {
            Objects.requireNonNull(stack);
            return test(stack) ? stack.copyWithAmount(ingredient.amount()) : FluidStack.EMPTY;
        }

        @Override
        public long getNeededAmount(FluidStack stack) {
            Objects.requireNonNull(stack);
            return testType(stack) ? ingredient.amount() : 0;
        }

        @Override
        public boolean hasNoMatchingInstances() {
            return ingredient.ingredient().hasNoFluids();
        }

        @Override
        public List<@NotNull FluidStack> getRepresentations() {
            if (this.representations == null) {
                this.representations = List.of(ingredient.getFluids());
            }
            return representations;
        }

        /**
         * For use in recipe input caching. Do not use this to modify the backing stack.
         */
        public SizedFluidIngredient getInputRaw() {
            return ingredient;
        }

        @Override
        public IngredientType getType() {
            return IngredientType.SINGLE;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SingleFluidStackIngredient other = (SingleFluidStackIngredient) o;
            //TODO - 1.20.5: Replace this once sized ingredient implements equals and hashcode
            return ingredient.amount() == other.ingredient.amount() && ingredient.ingredient().equals(other.ingredient.ingredient());
        }

        @Override
        public int hashCode() {
            //return ingredient.hashCode();
            return Objects.hash(ingredient.amount(), ingredient.ingredient());
        }
    }

    @NothingNullByDefault
    public static class MultiFluidStackIngredient extends FluidStackIngredient implements IMultiIngredient<FluidStack, SingleFluidStackIngredient> {

        public static final Codec<MultiFluidStackIngredient> CODEC = ExtraCodecs.nonEmptyList(SingleFluidStackIngredient.CODEC.listOf()).xmap(
              MultiFluidStackIngredient::new, MultiFluidStackIngredient::getIngredients
        );
        //This must be lazy as the base stream codec isn't initialized until after this line happens
        public static final StreamCodec<RegistryFriendlyByteBuf, MultiFluidStackIngredient> STREAM_CODEC = SingleFluidStackIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).map(
              MultiFluidStackIngredient::new, MultiFluidStackIngredient::getIngredients
        );

        private final SingleFluidStackIngredient[] ingredients;

        private MultiFluidStackIngredient(List<SingleFluidStackIngredient> ingredients) {
            this.ingredients = ingredients.toArray(new SingleFluidStackIngredient[0]);
        }

        @Override
        public boolean test(FluidStack stack) {
            for (FluidStackIngredient ingredient : ingredients) {
                if (ingredient.test(stack)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean testType(FluidStack stack) {
            for (FluidStackIngredient ingredient : ingredients) {
                if (ingredient.testType(stack)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public FluidStack getMatchingInstance(FluidStack stack) {
            for (FluidStackIngredient ingredient : ingredients) {
                FluidStack matchingInstance = ingredient.getMatchingInstance(stack);
                if (!matchingInstance.isEmpty()) {
                    return matchingInstance;
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public long getNeededAmount(FluidStack stack) {
            for (FluidStackIngredient ingredient : ingredients) {
                long amount = ingredient.getNeededAmount(stack);
                if (amount > 0) {
                    return amount;
                }
            }
            return 0;
        }

        @Override
        public boolean hasNoMatchingInstances() {
            for (FluidStackIngredient ingredient : ingredients) {
                if (!ingredient.hasNoMatchingInstances()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public List<@NotNull FluidStack> getRepresentations() {
            List<@NotNull FluidStack> representations = new ArrayList<>();
            for (FluidStackIngredient ingredient : ingredients) {
                representations.addAll(ingredient.getRepresentations());
            }
            return representations;
        }

        @Override
        public boolean forEachIngredient(Predicate<SingleFluidStackIngredient> checker) {
            boolean result = false;
            for (SingleFluidStackIngredient ingredient : ingredients) {
                result |= checker.test(ingredient);
            }
            return result;
        }

        @Override
        public <DATA> boolean forEachIngredient(DATA data, BiPredicate<DATA, SingleFluidStackIngredient> checker) {
            boolean result = false;
            for (SingleFluidStackIngredient ingredient : ingredients) {
                result |= checker.test(data, ingredient);
            }
            return result;
        }

        @Override
        public final List<SingleFluidStackIngredient> getIngredients() {
            return List.of(ingredients);
        }

        @Override
        public IngredientType getType() {
            return IngredientType.MULTI;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return Arrays.equals(ingredients, ((MultiFluidStackIngredient) o).ingredients);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(ingredients);
        }
    }
}