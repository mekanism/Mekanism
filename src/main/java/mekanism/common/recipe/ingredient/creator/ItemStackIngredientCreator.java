package mekanism.common.recipe.ingredient.creator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.creator.IItemStackIngredientCreator;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackIngredientCreator implements IItemStackIngredientCreator {

    public static final ItemStackIngredientCreator INSTANCE = new ItemStackIngredientCreator();

    private ItemStackIngredientCreator() {
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
        IngredientType type = buffer.readEnum(IngredientType.class);
        if (type == IngredientType.SINGLE) {
            return from(Ingredient.fromNetwork(buffer), buffer.readVarInt());
        }
        ItemStackIngredient[] ingredients = new ItemStackIngredient[buffer.readVarInt()];
        for (int i = 0; i < ingredients.length; i++) {
            ingredients[i] = read(buffer);
        }
        return createMulti(ingredients);
    }

    @Override
    public ItemStackIngredient deserialize(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Ingredient cannot be null.");
        }
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            int size = jsonArray.size();
            if (size == 0) {
                throw new JsonSyntaxException("Ingredient array cannot be empty, at least one ingredient must be defined.");
            } else if (size > 1) {
                ItemStackIngredient[] ingredients = new ItemStackIngredient[size];
                for (int i = 0; i < size; i++) {
                    //Read all the ingredients
                    ingredients[i] = deserialize(jsonArray.get(i));
                }
                return createMulti(ingredients);
            }
            //If we only have a single element, just set our json as that so that we don't have to use Multi for efficiency reasons
            json = jsonArray.get(0);
        }
        if (!json.isJsonObject()) {
            throw new JsonSyntaxException("Expected item to be object or array of objects.");
        }
        JsonObject jsonObject = json.getAsJsonObject();
        int amount = 1;
        if (jsonObject.has(JsonConstants.AMOUNT)) {
            JsonElement count = jsonObject.get(JsonConstants.AMOUNT);
            if (!GsonHelper.isNumberValue(count)) {
                throw new JsonSyntaxException("Expected amount to be a number that is one or larger.");
            }
            amount = count.getAsJsonPrimitive().getAsInt();
            if (amount < 1) {
                throw new JsonSyntaxException("Expected amount to larger than or equal to one.");
            }
        }
        JsonElement jsonelement = GsonHelper.isArrayNode(jsonObject, JsonConstants.INGREDIENT) ? GsonHelper.getAsJsonArray(jsonObject, JsonConstants.INGREDIENT) :
                                  GsonHelper.getAsJsonObject(jsonObject, JsonConstants.INGREDIENT);
        Ingredient ingredient = Ingredient.fromJson(jsonelement);
        return from(ingredient, amount);
    }

    @Override
    public ItemStackIngredient createMulti(ItemStackIngredient... ingredients) {
        Objects.requireNonNull(ingredients, "Cannot create a multi ingredient out of a null array.");
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create a multi ingredient out of no ingredients.");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        List<ItemStackIngredient> cleanedIngredients = new ArrayList<>();
        for (ItemStackIngredient ingredient : ingredients) {
            if (ingredient instanceof MultiItemStackIngredient multi) {
                //Don't worry about if our inner ingredients are multi as well, as if this is the only external method for
                // creating a multi ingredient, then we are certified they won't be of a higher depth
                Collections.addAll(cleanedIngredients, multi.ingredients);
            } else {
                cleanedIngredients.add(ingredient);
            }
        }
        //There should be more than a single item, or we would have split out earlier
        return new MultiItemStackIngredient(cleanedIngredients.toArray(new ItemStackIngredient[0]));
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static class SingleItemStackIngredient extends ItemStackIngredient {

        @Nonnull
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
            if (test(stack)) {
                ItemStack matching = stack.copy();
                matching.setCount(amount);
                return matching;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public long getNeededAmount(ItemStack stack) {
            return testType(stack) ? amount : 0;
        }

        @Override
        public List<@NonNull ItemStack> getRepresentations() {
            //TODO: Can this be cached some how
            List<@NonNull ItemStack> representations = new ArrayList<>();
            for (ItemStack stack : ingredient.getItems()) {
                if (stack.getCount() == amount) {
                    representations.add(stack);
                } else {
                    ItemStack copy = stack.copy();
                    copy.setCount(amount);
                    representations.add(copy);
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

        @Override
        public void write(FriendlyByteBuf buffer) {
            buffer.writeEnum(IngredientType.SINGLE);
            ingredient.toNetwork(buffer);
            buffer.writeVarInt(amount);
        }

        @Override
        public JsonElement serialize() {
            JsonObject json = new JsonObject();
            if (amount > 1) {
                json.addProperty(JsonConstants.AMOUNT, amount);
            }
            json.add(JsonConstants.INGREDIENT, ingredient.toJson());
            return json;
        }
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static class MultiItemStackIngredient extends ItemStackIngredient {

        @Nonnull
        private final ItemStackIngredient[] ingredients;

        private MultiItemStackIngredient(ItemStackIngredient... ingredients) {
            this.ingredients = ingredients;
        }

        @Override
        public boolean test(ItemStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.test(stack));
        }

        @Override
        public boolean testType(ItemStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(stack));
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
        public List<@NonNull ItemStack> getRepresentations() {
            List<@NonNull ItemStack> representations = new ArrayList<>();
            for (ItemStackIngredient ingredient : ingredients) {
                representations.addAll(ingredient.getRepresentations());
            }
            return representations;
        }

        /**
         * For use in recipe input caching, checks all ingredients even if some match.
         *
         * @return {@code true} if any ingredient matches.
         */
        public boolean forEachIngredient(Predicate<ItemStackIngredient> checker) {
            boolean result = false;
            for (ItemStackIngredient ingredient : ingredients) {
                result |= checker.test(ingredient);
            }
            return result;
        }

        @Override
        public void write(FriendlyByteBuf buffer) {
            buffer.writeEnum(IngredientType.MULTI);
            buffer.writeVarInt(ingredients.length);
            for (ItemStackIngredient ingredient : ingredients) {
                ingredient.write(buffer);
            }
        }

        @Override
        public JsonElement serialize() {
            JsonArray json = new JsonArray();
            for (ItemStackIngredient ingredient : ingredients) {
                json.add(ingredient.serialize());
            }
            return json;
        }
    }

    private enum IngredientType {
        SINGLE,
        MULTI
    }
}