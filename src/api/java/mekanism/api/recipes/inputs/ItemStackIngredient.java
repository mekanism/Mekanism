package mekanism.api.recipes.inputs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.providers.IItemProvider;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.Tag;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.crafting.NBTIngredient;

//TODO: Allow for empty item stacks?
public abstract class ItemStackIngredient implements InputIngredient<@NonNull ItemStack> {

    //TODO: Make ones that take a list of blocks/items

    public static ItemStackIngredient from(@NonNull ItemStack stack) {
        return from(stack, stack.getCount());
    }

    public static ItemStackIngredient from(@NonNull ItemStack stack, int amount) {
        //Support NBT that is on the stack in case it matters
        //It is a protected constructor so pretend we are extending it and implementing it via the {}
        // Note: Only bother making it an NBT ingredient if the stack has NBT, otherwise there is no point in doing the extra checks
        //TODO: Figure out if this note is correct on what we should do
        Ingredient ingredient = stack.hasTag() ? new NBTIngredient(stack) {} : Ingredient.fromStacks(stack);
        return from(ingredient, amount);
    }

    public static ItemStackIngredient from(@NonNull Block block) {
        return from(block, 1);
    }

    public static ItemStackIngredient from(@NonNull Block block, int amount) {
        return from(new ItemStack(block), amount);
    }

    public static ItemStackIngredient from(@NonNull Item item) {
        return from(item, 1);
    }

    public static ItemStackIngredient from(@NonNull Item item, int amount) {
        //By default don't do any wildcard stuff.
        //TODO: Check if anything that is calling this should actually wants the wild card
        return from(new ItemStack(item), amount);
    }

    public static ItemStackIngredient from(@NonNull IItemProvider itemProvider) {
        return from(itemProvider, 1);
    }

    public static ItemStackIngredient from(@NonNull IItemProvider itemProvider, int amount) {
        return from(itemProvider.getItemStack(amount));
    }

    //TODO: Should we instead have it accept a Tag<Item> instead of a resource location
    public static ItemStackIngredient from(@NonNull Tag<Item> itemTag) {
        return from(itemTag, 1);
    }

    //TODO: Should we instead have it accept a Tag<Item> instead of a resource location
    public static ItemStackIngredient from(@NonNull Tag<Item> itemTag, int amount) {
        return from(Ingredient.fromTag(itemTag), amount);
    }

    public static ItemStackIngredient from(@NonNull Ingredient ingredient) {
        return from(ingredient, 1);
    }

    public static ItemStackIngredient from(@NonNull Ingredient ingredient, int amount) {
        return new Single(ingredient, amount);
    }

    public static ItemStackIngredient read(PacketBuffer buffer) {
        //TODO: Allow supporting serialization of different types than just the ones we implement?
        IngredientType type = buffer.readEnumValue(IngredientType.class);
        if (type == IngredientType.SINGLE) {
            return Single.read(buffer);
        }
        return Multi.read(buffer);
    }

    //TODO: Should we not let this be null?
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
        if (jsonObject.has("amount")) {
            JsonElement count = jsonObject.get("amount");
            if (!JSONUtils.isNumber(count)) {
                throw new JsonSyntaxException("Expected amount to be a number that is one or larger.");
            }
            amount = count.getAsJsonPrimitive().getAsInt();
            if (amount < 1) {
                throw new JsonSyntaxException("Expected amount to larger than or equal to one");
            }
        }
        JsonElement jsonelement = JSONUtils.isJsonArray(jsonObject, "ingredient") ? JSONUtils.getJsonArray(jsonObject, "ingredient") :
                                  JSONUtils.getJsonObject(jsonObject, "ingredient");
        Ingredient ingredient = Ingredient.deserialize(jsonelement);
        return from(ingredient, amount);
    }

    public static ItemStackIngredient createMulti(ItemStackIngredient... ingredients) {
        if (ingredients.length == 0) {
            //TODO: Throw error
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
        //There should be more than a single item or we would have split out earlier
        return new Multi(cleanedIngredients.toArray(new ItemStackIngredient[0]));
    }

    public static class Single extends ItemStackIngredient {

        @NonNull
        private final Ingredient ingredient;
        private final int amount;

        public Single(@NonNull Ingredient ingredient, int amount) {
            this.ingredient = Objects.requireNonNull(ingredient);
            this.amount = amount;
        }

        @Override
        public boolean test(@NonNull ItemStack stack) {
            return testType(stack) && stack.getCount() >= amount;
        }

        @Override
        public boolean testType(@NonNull ItemStack stack) {
            //TODO: Should this fail on empty stacks
            return ingredient.test(stack);
        }

        @Override
        public @NonNull ItemStack getMatchingInstance(@NonNull ItemStack stack) {
            if (test(stack)) {
                ItemStack matching = stack.copy();
                matching.setCount(amount);
                return matching;
            }
            return ItemStack.EMPTY;
        }

        @NonNull
        @Override
        public List<@NonNull ItemStack> getRepresentations() {
            //TODO: Can this be cached some how
            List<@NonNull ItemStack> representations = new ArrayList<>();
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                //TODO: if there a cleaner way to do this that doesn't require copying at least when the size is the same
                ItemStack copy = stack.copy();
                copy.setCount(amount);
                representations.add(copy);
            }
            return representations;
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnumValue(IngredientType.SINGLE);
            ingredient.write(buffer);
            buffer.writeInt(amount);
        }

        public static Single read(PacketBuffer buffer) {
            return new Single(Ingredient.read(buffer), buffer.readInt());
        }
    }

    //TODO: Maybe name this better, at the very least make it easier/possible to create new instances of this
    // Also cleanup the javadoc comment about this, and try to make the helpers that create a new instance
    // return a normal ItemStackIngredient (Single), if we only have a singular one
    public static class Multi extends ItemStackIngredient {

        private final ItemStackIngredient[] ingredients;

        protected Multi(@NonNull ItemStackIngredient... ingredients) {
            this.ingredients = ingredients;
        }

        @Override
        public boolean test(@NonNull ItemStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.test(stack));
        }

        @Override
        public boolean testType(@NonNull ItemStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(stack));
        }

        @Override
        public @NonNull ItemStack getMatchingInstance(@NonNull ItemStack stack) {
            for (ItemStackIngredient ingredient : ingredients) {
                ItemStack matchingInstance = ingredient.getMatchingInstance(stack);
                if (!matchingInstance.isEmpty()) {
                    return matchingInstance;
                }
            }
            return ItemStack.EMPTY;
        }

        @NonNull
        @Override
        public List<@NonNull ItemStack> getRepresentations() {
            List<@NonNull ItemStack> representations = new ArrayList<>();
            for (ItemStackIngredient ingredient : ingredients) {
                representations.addAll(ingredient.getRepresentations());
            }
            return representations;
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnumValue(IngredientType.MULTI);
            buffer.writeInt(ingredients.length);
            for (ItemStackIngredient ingredient : ingredients) {
                ingredient.write(buffer);
            }
        }

        public static ItemStackIngredient read(PacketBuffer buffer) {
            //TODO: Verify this works
            ItemStackIngredient[] ingredients = new ItemStackIngredient[buffer.readInt()];
            for (int i = 0; i < ingredients.length; i++) {
                ingredients[i] = ItemStackIngredient.read(buffer);
            }
            return createMulti(ingredients);
        }
    }

    private enum IngredientType {
        SINGLE,
        MULTI
    }
}