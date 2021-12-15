package mekanism.api.recipes.inputs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NonNull;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.crafting.NBTIngredient;

/**
 * Base implementation for how Mekanism handle's ItemStack Ingredients.
 */
public abstract class ItemStackIngredient implements InputIngredient<@NonNull ItemStack> {

    /**
     * Creates an Item Stack Ingredient that matches a given item stack.
     *
     * @param stack Item stack to match.
     */
    public static ItemStackIngredient from(@Nonnull ItemStack stack) {
        return from(stack, stack.getCount());
    }

    /**
     * Creates an Item Stack Ingredient that matches a given item stack with a specified amount.
     *
     * @param stack  Item stack to match.
     * @param amount Amount needed.
     *
     * @apiNote If the amount needed is the same as the stack's size, {@link #from(ItemStack)} can be used instead.
     */
    public static ItemStackIngredient from(@Nonnull ItemStack stack, int amount) {
        //Support NBT that is on the stack in case it matters
        //It is a protected constructor so pretend we are extending it and implementing it via the {}
        // Note: Only bother making it an NBT ingredient if the stack has NBT, otherwise there is no point in doing the extra checks
        Ingredient ingredient = stack.hasTag() ? new NBTIngredient(stack) {} : Ingredient.of(stack);
        return from(ingredient, amount);
    }

    /**
     * Creates an Item Stack Ingredient that matches a provided item.
     *
     * @param item Item provider that provides the item to match.
     */
    public static ItemStackIngredient from(@Nonnull IItemProvider item) {
        return from(item, 1);
    }

    /**
     * Creates an Item Stack Ingredient that matches a provided item and amount.
     *
     * @param item   Item provider that provides the item to match.
     * @param amount Amount needed.
     */
    public static ItemStackIngredient from(@Nonnull IItemProvider item, int amount) {
        return from(new ItemStack(item), amount);
    }

    /**
     * Creates an Item Stack Ingredient that matches a given item tag.
     *
     * @param tag Tag to match.
     */
    public static ItemStackIngredient from(@Nonnull ITag<Item> tag) {
        return from(tag, 1);
    }

    /**
     * Creates an Item Stack Ingredient that matches a given item tag and amount.
     *
     * @param tag    Tag to match.
     * @param amount Amount needed.
     */
    public static ItemStackIngredient from(@Nonnull ITag<Item> tag, int amount) {
        return from(Ingredient.of(tag), amount);
    }

    /**
     * Creates an Item Stack Ingredient that matches a given ingredient.
     *
     * @param ingredient Ingredient to match.
     */
    public static ItemStackIngredient from(@Nonnull Ingredient ingredient) {
        return from(ingredient, 1);
    }

    /**
     * Creates an Item Stack Ingredient that matches a given ingredient and amount.
     *
     * @param ingredient Ingredient to match.
     * @param amount     Amount needed.
     */
    public static ItemStackIngredient from(@Nonnull Ingredient ingredient, int amount) {
        return new Single(ingredient, amount);
    }

    /**
     * Reads an Item Stack Ingredient from a Packet Buffer.
     *
     * @param buffer Buffer to read from.
     *
     * @return Item Stack Ingredient.
     */
    public static ItemStackIngredient read(PacketBuffer buffer) {
        IngredientType type = buffer.readEnum(IngredientType.class);
        if (type == IngredientType.SINGLE) {
            return from(Ingredient.fromNetwork(buffer), buffer.readVarInt());
        }
        ItemStackIngredient[] ingredients = new ItemStackIngredient[buffer.readVarInt()];
        for (int i = 0; i < ingredients.length; i++) {
            ingredients[i] = ItemStackIngredient.read(buffer);
        }
        return createMulti(ingredients);
    }

    /**
     * Helper to deserialize a Json Object into an Item Stack Ingredient.
     *
     * @param json Json object to deserialize.
     *
     * @return Item Stack Ingredient.
     */
    public static ItemStackIngredient deserialize(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Ingredient cannot be null");
        }
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            int size = jsonArray.size();
            if (size == 0) {
                throw new JsonSyntaxException("Ingredient array cannot be empty, at least one ingredient must be defined");
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
            throw new JsonSyntaxException("Expected item to be object or array of objects");
        }
        JsonObject jsonObject = json.getAsJsonObject();
        int amount = 1;
        if (jsonObject.has(JsonConstants.AMOUNT)) {
            JsonElement count = jsonObject.get(JsonConstants.AMOUNT);
            if (!JSONUtils.isNumberValue(count)) {
                throw new JsonSyntaxException("Expected amount to be a number that is one or larger.");
            }
            amount = count.getAsJsonPrimitive().getAsInt();
            if (amount < 1) {
                throw new JsonSyntaxException("Expected amount to larger than or equal to one");
            }
        }
        JsonElement jsonelement = JSONUtils.isArrayNode(jsonObject, JsonConstants.INGREDIENT) ? JSONUtils.getAsJsonArray(jsonObject, JsonConstants.INGREDIENT) :
                                  JSONUtils.getAsJsonObject(jsonObject, JsonConstants.INGREDIENT);
        Ingredient ingredient = Ingredient.fromJson(jsonelement);
        return from(ingredient, amount);
    }

    /**
     * Combines multiple Item Stack Ingredients into a single Item Stack Ingredient.
     *
     * @param ingredients Ingredients to combine.
     *
     * @return Combined Item Stack Ingredient.
     */
    public static ItemStackIngredient createMulti(ItemStackIngredient... ingredients) {
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create a multi ingredient out of no ingredients.");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        List<ItemStackIngredient> cleanedIngredients = new ArrayList<>();
        for (ItemStackIngredient ingredient : ingredients) {
            if (ingredient instanceof Multi) {
                //Don't worry about if our inner ingredients are multi as well, as if this is the only external method for
                // creating a multi ingredient, then we are certified they won't be of a higher depth
                cleanedIngredients.addAll(Arrays.asList(((Multi) ingredient).ingredients));
            } else {
                cleanedIngredients.add(ingredient);
            }
        }
        //There should be more than a single item, or we would have split out earlier
        return new Multi(cleanedIngredients.toArray(new ItemStackIngredient[0]));
    }

    public static class Single extends ItemStackIngredient {

        @Nonnull
        private final Ingredient ingredient;
        private final int amount;

        private Single(@Nonnull Ingredient ingredient, int amount) {
            this.ingredient = Objects.requireNonNull(ingredient);
            this.amount = amount;
        }

        @Override
        public boolean test(@Nonnull ItemStack stack) {
            return testType(stack) && stack.getCount() >= amount;
        }

        @Override
        public boolean testType(@Nonnull ItemStack stack) {
            return ingredient.test(stack);
        }

        @Nonnull
        @Override
        public ItemStack getMatchingInstance(@Nonnull ItemStack stack) {
            if (test(stack)) {
                ItemStack matching = stack.copy();
                matching.setCount(amount);
                return matching;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public long getNeededAmount(@Nonnull ItemStack stack) {
            return testType(stack) ? amount : 0;
        }

        @Nonnull
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
        public void write(PacketBuffer buffer) {
            buffer.writeEnum(IngredientType.SINGLE);
            ingredient.toNetwork(buffer);
            buffer.writeVarInt(amount);
        }

        @Nonnull
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

    public static class Multi extends ItemStackIngredient {

        private final ItemStackIngredient[] ingredients;

        private Multi(@Nonnull ItemStackIngredient... ingredients) {
            this.ingredients = ingredients;
        }

        @Override
        public boolean test(@Nonnull ItemStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.test(stack));
        }

        @Override
        public boolean testType(@Nonnull ItemStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(stack));
        }

        @Nonnull
        @Override
        public ItemStack getMatchingInstance(@Nonnull ItemStack stack) {
            for (ItemStackIngredient ingredient : ingredients) {
                ItemStack matchingInstance = ingredient.getMatchingInstance(stack);
                if (!matchingInstance.isEmpty()) {
                    return matchingInstance;
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public long getNeededAmount(@Nonnull ItemStack stack) {
            for (ItemStackIngredient ingredient : ingredients) {
                long amount = ingredient.getNeededAmount(stack);
                if (amount > 0) {
                    return amount;
                }
            }
            return 0;
        }

        @Nonnull
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
        public void write(PacketBuffer buffer) {
            buffer.writeEnum(IngredientType.MULTI);
            buffer.writeVarInt(ingredients.length);
            for (ItemStackIngredient ingredient : ingredients) {
                ingredient.write(buffer);
            }
        }

        @Nonnull
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