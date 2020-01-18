package mekanism.api.recipes.inputs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTags;
import mekanism.api.providers.IGasProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.Tag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

/**
 * Created by Thiakil on 11/07/2019.
 */
//TODO: Allow for empty gas stacks (at least in 1.14 when we will have an empty variant of GasStack)
public abstract class GasStackIngredient implements InputIngredient<@NonNull GasStack> {

    public static GasStackIngredient from(@NonNull GasStack instance) {
        return from(instance.getType(), instance.getAmount());
    }

    public static GasStackIngredient from(@NonNull IGasProvider instance, int amount) {
        return new Single(instance.getGas(), amount);
    }

    public static GasStackIngredient from(@NonNull Tag<Gas> gasTag, int amount) {
        return new Tagged(gasTag, amount);
    }

    public abstract boolean testType(@NonNull Gas gas);

    public static GasStackIngredient read(PacketBuffer buffer) {
        //TODO: Allow supporting serialization of different types than just the ones we implement?
        IngredientType type = buffer.readEnumValue(IngredientType.class);
        if (type == IngredientType.SINGLE) {
            return Single.read(buffer);
        } else if (type == IngredientType.TAGGED) {
            return Tagged.read(buffer);
        }
        return Multi.read(buffer);
    }

    //TODO: Should we not let this be null?
    public static GasStackIngredient deserialize(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Ingredient cannot be null");
        }
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            int size = jsonArray.size();
            if (size == 0) {
                throw new JsonSyntaxException("Ingredient array cannot be empty, at least one ingredient must be defined");
            } else if (size > 1) {
                GasStackIngredient[] ingredients = new GasStackIngredient[size];
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
        if (jsonObject.has("gas") && jsonObject.has("tag")) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        } else if (jsonObject.has("gas")) {
            return from(SerializerHelper.deserializeGas(jsonObject));
        } else if (jsonObject.has("tag")) {
            if (!jsonObject.has("amount")) {
                throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
            }
            JsonElement count = jsonObject.get("amount");
            if (!JSONUtils.isNumber(count)) {
                throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
            }
            int amount = count.getAsJsonPrimitive().getAsInt();
            if (amount < 1) {
                throw new JsonSyntaxException("Expected amount to be greater than zero.");
            }
            ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(jsonObject, "tag"));
            Tag<Gas> tag = GasTags.getCollection().get(resourceLocation);
            if (tag == null) {
                throw new JsonSyntaxException("Unknown gas tag '" + resourceLocation + "'");
            }
            return from(tag, amount);
        }
        throw new JsonSyntaxException("Expected to receive a resource location representing either a tag or a gas.");
    }

    public static GasStackIngredient createMulti(GasStackIngredient... ingredients) {
        if (ingredients.length == 0) {
            //TODO: Throw error
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        List<GasStackIngredient> cleanedIngredients = new ArrayList<>();
        for (GasStackIngredient ingredient : ingredients) {
            if (ingredient instanceof Multi) {
                //Don't worry about if our inner ingredients are multi as well, as if this is the only external method for
                // creating a multi ingredient, then we are certified they won't be of a higher depth
                cleanedIngredients.addAll(Arrays.asList(((Multi) ingredient).ingredients));
            } else {
                cleanedIngredients.add(ingredient);
            }
        }
        //There should be more than a single item or we would have split out earlier
        return new Multi(cleanedIngredients.toArray(new GasStackIngredient[0]));
    }

    public static class Single extends GasStackIngredient {

        //TODO: Convert this to storing a GasStack?
        @NonNull
        private final Gas gasInstance;
        private final int amount;

        protected Single(@NonNull Gas gasInstance, int amount) {
            this.gasInstance = Objects.requireNonNull(gasInstance);
            this.amount = amount;
        }

        @Override
        public boolean test(@NonNull GasStack gasStack) {
            return testType(gasStack) && gasStack.getAmount() >= amount;
        }

        @Override
        public boolean testType(@NonNull GasStack gasStack) {
            return testType(Objects.requireNonNull(gasStack).getType());
        }

        @Override
        public boolean testType(@NonNull Gas gas) {
            return Objects.requireNonNull(gas) == gasInstance;
        }

        @Override
        public @NonNull GasStack getMatchingInstance(@NonNull GasStack gasStack) {
            return test(gasStack) ? new GasStack(gasInstance, amount) : GasStack.EMPTY;
        }

        @Override
        public @NonNull List<@NonNull GasStack> getRepresentations() {
            return Collections.singletonList(new GasStack(gasInstance, amount));
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnumValue(IngredientType.SINGLE);
            buffer.writeRegistryId(gasInstance);
            buffer.writeInt(amount);
        }

        @Nonnull
        @Override
        public JsonElement serialize() {
            JsonObject json = new JsonObject();
            json.addProperty("amount", amount);
            json.addProperty("gas", gasInstance.getRegistryName().toString());
            return json;
        }

        public static Single read(PacketBuffer buffer) {
            return new Single(buffer.readRegistryId(), buffer.readInt());
        }
    }

    public static class Tagged extends GasStackIngredient {

        @Nonnull
        private final Tag<Gas> tag;
        private final int amount;

        public Tagged(@Nonnull Tag<Gas> tag, int amount) {
            this.tag = tag;
            this.amount = amount;
        }

        @Override
        public boolean test(@NonNull GasStack gasStack) {
            return testType(gasStack) && gasStack.getAmount() >= amount;
        }

        @Override
        public boolean testType(@NonNull GasStack gasStack) {
            return testType(Objects.requireNonNull(gasStack).getType());
        }

        @Override
        public boolean testType(@NonNull Gas gas) {
            return Objects.requireNonNull(gas).isIn(tag);
        }

        @Override
        public @NonNull GasStack getMatchingInstance(@NonNull GasStack gasStack) {
            if (test(gasStack)) {
                //Our gas is in the tag so we make a new stack with the given amount
                return new GasStack(gasStack, amount);
            }
            return GasStack.EMPTY;
        }

        @Override
        @NonNull
        public List<@NonNull GasStack> getRepresentations() {
            //TODO: Can this be cached some how
            List<@NonNull GasStack> representations = new ArrayList<>();
            for (Gas gas : tag.getAllElements()) {
                representations.add(new GasStack(gas, amount));
            }
            return representations;
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnumValue(IngredientType.TAGGED);
            buffer.writeResourceLocation(tag.getId());
            buffer.writeInt(amount);
        }

        @Nonnull
        @Override
        public JsonElement serialize() {
            JsonObject json = new JsonObject();
            json.addProperty("amount", amount);
            json.addProperty("tag", tag.getId().toString());
            return json;
        }

        public static Tagged read(PacketBuffer buffer) {
            //TODO: Should this only check already defined tags??
            return new Tagged(new GasTags.Wrapper(buffer.readResourceLocation()), buffer.readInt());
        }
    }

    //TODO: Maybe name this better, at the very least make it easier/possible to create new instances of this
    // Also cleanup the javadoc comment about this, and try to make the helpers that create a new instance
    // return a normal GasStackIngredient (Single), if we only have a singular one
    public static class Multi extends GasStackIngredient {

        private final GasStackIngredient[] ingredients;

        protected Multi(@NonNull GasStackIngredient... ingredients) {
            this.ingredients = ingredients;
        }

        @Override
        public boolean test(@NonNull GasStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.test(stack));
        }

        @Override
        public boolean testType(@NonNull GasStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(stack));
        }

        @Override
        public boolean testType(@NonNull Gas gas) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(gas));
        }

        @Override
        public @NonNull GasStack getMatchingInstance(@NonNull GasStack stack) {
            for (GasStackIngredient ingredient : ingredients) {
                GasStack matchingInstance = ingredient.getMatchingInstance(stack);
                if (!matchingInstance.isEmpty()) {
                    return matchingInstance;
                }
            }
            return GasStack.EMPTY;
        }

        @NonNull
        @Override
        public List<@NonNull GasStack> getRepresentations() {
            List<@NonNull GasStack> representations = new ArrayList<>();
            for (GasStackIngredient ingredient : ingredients) {
                representations.addAll(ingredient.getRepresentations());
            }
            return representations;
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnumValue(IngredientType.MULTI);
            buffer.writeInt(ingredients.length);
            for (GasStackIngredient ingredient : ingredients) {
                ingredient.write(buffer);
            }
        }

        @Nonnull
        @Override
        public JsonElement serialize() {
            JsonArray json = new JsonArray();
            for (GasStackIngredient ingredient : ingredients) {
                json.add(ingredient.serialize());
            }
            return json;
        }

        public static GasStackIngredient read(PacketBuffer buffer) {
            //TODO: Verify this works
            GasStackIngredient[] ingredients = new GasStackIngredient[buffer.readInt()];
            for (int i = 0; i < ingredients.length; i++) {
                ingredients[i] = GasStackIngredient.read(buffer);
            }
            return createMulti(ingredients);
        }
    }

    private enum IngredientType {
        SINGLE,
        TAGGED,
        MULTI
    }
}
