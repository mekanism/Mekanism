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
import mekanism.api.recipes.ingredients.IngredientType;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator;
import mekanism.common.recipe.ingredient.IMultiIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ItemStackIngredientCreator implements IItemStackIngredientCreator {

    public static final ItemStackIngredientCreator INSTANCE = new ItemStackIngredientCreator();

    private static final Codec<ItemStackIngredient> CODEC = Codec.either(SingleItemStackIngredient.CODEC, MultiItemStackIngredient.CODEC)
          .xmap(
                either -> either.map(Function.identity(), multi -> {
                    //unbox if we only got one
                    if (multi.ingredients.length == 1) {
                        return multi.ingredients[0];
                    }
                    return multi;
                }),
                stackIngredient -> {
                    if (stackIngredient instanceof SingleItemStackIngredient single) {
                        return Either.left(single);
                    }
                    return Either.right((MultiItemStackIngredient) stackIngredient);
                }
          );
    private static final StreamCodec<RegistryFriendlyByteBuf, ItemStackIngredient> STREAM_CODEC = IngredientType.STREAM_CODEC.<RegistryFriendlyByteBuf>cast().dispatch(InputIngredient::getType, type -> switch (type) {
        case SINGLE -> SingleItemStackIngredient.STREAM_CODEC;
        case MULTI -> MultiItemStackIngredient.STREAM_CODEC;
        case TAGGED -> throw new IllegalStateException("Unable to process tagged item stack ingredients");
    });

    private ItemStackIngredientCreator() {
    }

    @Override
    public Codec<ItemStackIngredient> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ItemStackIngredient> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public ItemStackIngredient from(SizedIngredient ingredient) {
        Objects.requireNonNull(ingredient, "ItemStackIngredients cannot be created from a null ingredient.");
        if (ingredient.ingredient() == Ingredient.EMPTY) {
            //Instance check for empty ingredient, because we could just be empty currently during datagen and want to allow it
            throw new IllegalArgumentException("ItemStackIngredients cannot be created using the empty ingredient.");
        }
        return new SingleItemStackIngredient(ingredient);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Converts a stream of ingredients into a single ingredient by converting the stream to an array and calling {@link #createMulti(ItemStackIngredient[])}.
     */
    @Override
    public ItemStackIngredient createMulti(ItemStackIngredient... ingredients) {
        Objects.requireNonNull(ingredients, "Cannot create a multi ingredient out of a null array.");
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create a multi ingredient out of no ingredients.");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        List<SingleItemStackIngredient> cleanedIngredients = new ArrayList<>();
        for (ItemStackIngredient ingredient : ingredients) {
            if (ingredient instanceof MultiItemStackIngredient multi) {
                //Don't worry about if our inner ingredients are multi as well, as if this is the only external method for
                // creating a multi ingredient, then we are certified they won't be of a higher depth
                Collections.addAll(cleanedIngredients, multi.ingredients);
            } else if (ingredient instanceof SingleItemStackIngredient single) {
                cleanedIngredients.add(single);
            } else {
                throw new IllegalStateException("Unknown ingredient class: " + ingredient);
            }
        }
        //There should be more than a single item, or we would have split out earlier
        return new MultiItemStackIngredient(cleanedIngredients);
    }

    @Override
    public ItemStackIngredient from(Stream<ItemStackIngredient> ingredients) {
        return createMulti(ingredients.toArray(ItemStackIngredient[]::new));
    }

    @NothingNullByDefault
    public static class SingleItemStackIngredient extends ItemStackIngredient {

        //Note: This must be a lazily initialized so that this class can be loaded in tests
        public static final Codec<SingleItemStackIngredient> CODEC = Codec.lazyInitialized(() -> SizedIngredient.FLAT_CODEC.xmap(
              SingleItemStackIngredient::new, SingleItemStackIngredient::getInputRaw
        ));
        public static final StreamCodec<RegistryFriendlyByteBuf, SingleItemStackIngredient> STREAM_CODEC = NeoForgeStreamCodecs.lazy(() ->
              SizedIngredient.STREAM_CODEC.map(SingleItemStackIngredient::new, SingleItemStackIngredient::getInputRaw)
        );

        private final SizedIngredient ingredient;
        @Nullable
        private List<ItemStack> representations;

        private SingleItemStackIngredient(SizedIngredient ingredient) {
            Objects.requireNonNull(ingredient);
            this.ingredient = ingredient;
        }

        @Override
        public boolean test(ItemStack stack) {
            Objects.requireNonNull(stack);
            return ingredient.test(stack);
        }

        @Override
        public boolean testType(ItemStack stack) {
            Objects.requireNonNull(stack);
            return ingredient.ingredient().test(stack);
        }

        @Override
        public ItemStack getMatchingInstance(ItemStack stack) {
            Objects.requireNonNull(stack);
            return test(stack) ? stack.copyWithCount(ingredient.count()) : ItemStack.EMPTY;
        }

        @Override
        public long getNeededAmount(ItemStack stack) {
            Objects.requireNonNull(stack);
            return testType(stack) ? ingredient.count() : 0;
        }

        @Override
        public boolean hasNoMatchingInstances() {
            return ingredient.ingredient().hasNoItems();
        }

        @Override
        public List<@NotNull ItemStack> getRepresentations() {
            if (this.representations == null) {
                //TODO: See if quark or whatever mods used to occasionally have empty stacks in their ingredients still do
                // if so we probably should filter them out of this
                this.representations = List.of(ingredient.getItems());
            }
            return representations;
        }

        /**
         * For use in recipe input caching. Do not use this to modify the backing stack.
         */
        public SizedIngredient getInputRaw() {
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
            SingleItemStackIngredient other = (SingleItemStackIngredient) o;
            //TODO - 1.20.5: Replace this once sized ingredient implements equals and hashcode
            return ingredient.count() == other.ingredient.count() && ingredient.ingredient().equals(other.ingredient.ingredient());
        }

        @Override
        public int hashCode() {
            //return ingredient.hashCode();
            return Objects.hash(ingredient.count(), ingredient.ingredient());
        }
    }

    @NothingNullByDefault
    public static class MultiItemStackIngredient extends ItemStackIngredient implements IMultiIngredient<ItemStack, SingleItemStackIngredient> {

        public static final Codec<MultiItemStackIngredient> CODEC = ExtraCodecs.nonEmptyList(SingleItemStackIngredient.CODEC.listOf()).xmap(
              MultiItemStackIngredient::new, MultiItemStackIngredient::getIngredients
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, MultiItemStackIngredient> STREAM_CODEC = SingleItemStackIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).map(
              MultiItemStackIngredient::new, MultiItemStackIngredient::getIngredients
        );

        private final SingleItemStackIngredient[] ingredients;

        private MultiItemStackIngredient(List<SingleItemStackIngredient> ingredients) {
            this.ingredients = ingredients.toArray(new SingleItemStackIngredient[0]);
        }

        @Override
        public boolean test(ItemStack stack) {
            for (SingleItemStackIngredient ingredient : ingredients) {
                if (ingredient.test(stack)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean testType(ItemStack stack) {
            for (SingleItemStackIngredient ingredient : ingredients) {
                if (ingredient.testType(stack)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public ItemStack getMatchingInstance(ItemStack stack) {
            for (ItemStackIngredient ingredient : ingredients) {
                ItemStack matchingInstance = ingredient.getMatchingInstance(stack);
                if (!matchingInstance.isEmpty()) {
                    return matchingInstance;
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public long getNeededAmount(ItemStack stack) {
            for (ItemStackIngredient ingredient : ingredients) {
                long amount = ingredient.getNeededAmount(stack);
                if (amount > 0) {
                    return amount;
                }
            }
            return 0;
        }

        @Override
        public boolean hasNoMatchingInstances() {
            for (SingleItemStackIngredient ingredient : ingredients) {
                if (!ingredient.hasNoMatchingInstances()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public List<@NotNull ItemStack> getRepresentations() {
            List<@NotNull ItemStack> representations = new ArrayList<>();
            for (ItemStackIngredient ingredient : ingredients) {
                representations.addAll(ingredient.getRepresentations());
            }
            return representations;
        }

        @Override
        public boolean forEachIngredient(Predicate<SingleItemStackIngredient> checker) {
            boolean result = false;
            for (SingleItemStackIngredient ingredient : ingredients) {
                result |= checker.test(ingredient);
            }
            return result;
        }

        @Override
        public <DATA> boolean forEachIngredient(DATA data, BiPredicate<DATA, SingleItemStackIngredient> checker) {
            boolean result = false;
            for (SingleItemStackIngredient ingredient : ingredients) {
                result |= checker.test(data, ingredient);
            }
            return result;
        }

        @Override
        public final List<SingleItemStackIngredient> getIngredients() {
            return List.of(ingredients);
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return Arrays.equals(ingredients, ((MultiItemStackIngredient) o).ingredients);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(ingredients);
        }

        @Override
        public IngredientType getType() {
            return IngredientType.MULTI;
        }
    }
}