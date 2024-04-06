package mekanism.common.recipe.ingredient.creator;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator;
import mekanism.common.recipe.ingredient.IMultiIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ItemStackIngredientCreator implements IItemStackIngredientCreator {

    public static final ItemStackIngredientCreator INSTANCE = new ItemStackIngredientCreator();

    private final Codec<ItemStackIngredient> CODEC = Codec.either(SingleItemStackIngredient.CODEC, MultiItemStackIngredient.CODEC)
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

    private ItemStackIngredientCreator() {
    }

    @Override
    public Codec<ItemStackIngredient> codec() {
        return CODEC;
    }

    @Override
    public ItemStackIngredient from(Ingredient ingredient, int amount) {
        Objects.requireNonNull(ingredient, "ItemStackIngredients cannot be created from a null ingredient.");
        if (ingredient == Ingredient.EMPTY) {
            //Instance check for empty ingredient, because we could just be empty currently during datagen and want to allow it
            throw new IllegalArgumentException("ItemStackIngredients cannot be created using the empty ingredient.");
        } else if (amount <= 0) {
            throw new IllegalArgumentException("ItemStackIngredients must have an amount of at least one. Received size was: " + amount);
        }
        return new SingleItemStackIngredient(ingredient, amount);
    }

    @Override
    public ItemStackIngredient read(FriendlyByteBuf buffer) {
        Objects.requireNonNull(buffer, "ItemStackIngredients cannot be read from a null packet buffer.");
        return switch (buffer.readEnum(IngredientType.class)) {
            case SINGLE -> from(Ingredient.fromNetwork(buffer), buffer.readVarInt());
            case MULTI -> createMulti(buffer.readArray(ItemStackIngredient[]::new, this::read));
        };
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
        return new MultiItemStackIngredient(cleanedIngredients.toArray(new SingleItemStackIngredient[0]));
    }

    @Override
    public ItemStackIngredient from(Stream<ItemStackIngredient> ingredients) {
        return createMulti(ingredients.toArray(ItemStackIngredient[]::new));
    }

    @NothingNullByDefault
    public static class SingleItemStackIngredient extends ItemStackIngredient {

        //Note: This must be a lazily initialized so that this class can be loaded in tests
        static final Codec<SingleItemStackIngredient> CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(i -> i.group(
              Ingredient.CODEC.fieldOf(JsonConstants.INGREDIENT).forGetter(SingleItemStackIngredient::getInputRaw),
              ExtraCodecs.POSITIVE_INT.optionalFieldOf(JsonConstants.AMOUNT, 1).forGetter(SingleItemStackIngredient::getAmountRaw)
        ).apply(i, SingleItemStackIngredient::new)));

        private final Ingredient ingredient;
        private final int amount;

        private SingleItemStackIngredient(Ingredient ingredient, int amount) {
            this.ingredient = Objects.requireNonNull(ingredient);
            this.amount = amount;
        }

        @Override
        public boolean test(ItemStack stack) {
            return testType(stack) && stack.getCount() >= amount;
        }

        @Override
        public boolean testType(ItemStack stack) {
            return ingredient.test(stack);
        }

        @Override
        public ItemStack getMatchingInstance(ItemStack stack) {
            return test(stack) ? stack.copyWithCount(amount) : ItemStack.EMPTY;
        }

        @Override
        public long getNeededAmount(ItemStack stack) {
            return testType(stack) ? amount : 0;
        }

        @Override
        public boolean hasNoMatchingInstances() {
            ItemStack[] items = ingredient.getItems();
            if (items.length == 0) {
                return true;
            } else if (items.length == 1) {
                //Manually compare it as we want to make sure we don't initialize the capabilities on it
                // to ensure we reduce any potential lag from this comparison
                ItemStack item = items[0];
                return item.getItem() == Items.BARRIER && item.getHoverName().getContents() instanceof PlainTextContents contents && contents.text().startsWith("Empty Tag: ");
            }
            return false;
        }

        @Override
        public List<@NotNull ItemStack> getRepresentations() {
            //TODO: Can this be cached somehow
            List<@NotNull ItemStack> representations = new ArrayList<>();
            for (ItemStack stack : ingredient.getItems()) {
                if (!stack.isEmpty()) {
                    //Ignore empty stacks as some mods have ingredients that some stacks are empty
                    representations.add(stack.copyWithCount(amount));
                }
            }
            return representations;
        }

        /**
         * For use in recipe input caching. Do not use this to modify the backing stack.
         */
        public Ingredient getInputRaw() {
            return ingredient;
        }

        /**
         * For use in CrT comparing.
         */
        public int getAmountRaw() {
            return amount;
        }

        @Override
        public void write(FriendlyByteBuf buffer) {
            buffer.writeEnum(IngredientType.SINGLE);
            ingredient.toNetwork(buffer);
            buffer.writeVarInt(amount);
        }
    }

    @NothingNullByDefault
    public static class MultiItemStackIngredient extends ItemStackIngredient implements IMultiIngredient<ItemStack, SingleItemStackIngredient> {

        static final Codec<MultiItemStackIngredient> CODEC = ExtraCodecs.nonEmptyList(SingleItemStackIngredient.CODEC.listOf()).xmap(
              lst -> new MultiItemStackIngredient(lst.toArray(new SingleItemStackIngredient[0])),
              MultiItemStackIngredient::getIngredients
        );

        private final SingleItemStackIngredient[] ingredients;

        private MultiItemStackIngredient(SingleItemStackIngredient... ingredients) {
            this.ingredients = ingredients;
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
        public void write(FriendlyByteBuf buffer) {
            buffer.writeEnum(IngredientType.MULTI);
            buffer.writeArray(ingredients, (buf, ingredient) -> ingredient.write(buf));
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
    }

    private enum IngredientType {
        SINGLE,
        MULTI
    }
}