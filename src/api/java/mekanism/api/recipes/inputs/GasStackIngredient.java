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
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IGasProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.Tag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

/**
 * Created by Thiakil on 11/07/2019.
 */
public abstract class GasStackIngredient implements InputIngredient<@NonNull GasStack> {

    public static GasStackIngredient from(@Nonnull GasStack instance) {
        return from(instance.getType(), instance.getAmount());
    }

    public static GasStackIngredient from(@Nonnull IGasProvider instance, long amount) {
        return new Single(instance.getGasStack(amount));
    }

    public static GasStackIngredient from(@Nonnull Tag<Gas> gasTag, long amount) {
        return new Tagged(gasTag, amount);
    }

    public abstract boolean testType(@Nonnull Gas gas);

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
        if (jsonObject.has(JsonConstants.GAS) && jsonObject.has(JsonConstants.TAG)) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        } else if (jsonObject.has(JsonConstants.GAS)) {
            return from(SerializerHelper.deserializeGas(jsonObject));
        } else if (jsonObject.has(JsonConstants.TAG)) {
            if (!jsonObject.has(JsonConstants.AMOUNT)) {
                throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
            }
            JsonElement count = jsonObject.get(JsonConstants.AMOUNT);
            if (!JSONUtils.isNumber(count)) {
                throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
            }
            long amount = count.getAsJsonPrimitive().getAsLong();
            if (amount < 1) {
                throw new JsonSyntaxException("Expected amount to be greater than zero.");
            }
            ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(jsonObject, JsonConstants.TAG));
            Tag<Gas> tag = ChemicalTags.GAS.getCollection().get(resourceLocation);
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

        @Nonnull
        private final GasStack gasInstance;

        protected Single(@Nonnull GasStack gasInstance) {
            this.gasInstance = Objects.requireNonNull(gasInstance);
        }

        @Override
        public boolean test(@Nonnull GasStack gasStack) {
            return testType(gasStack) && gasStack.getAmount() >= gasInstance.getAmount();
        }

        @Override
        public boolean testType(@Nonnull GasStack gasStack) {
            return gasInstance.isTypeEqual(Objects.requireNonNull(gasStack));
        }

        @Override
        public boolean testType(@Nonnull Gas gas) {
            return gasInstance.isTypeEqual(Objects.requireNonNull(gas));
        }

        @Nonnull
        @Override
        public GasStack getMatchingInstance(@Nonnull GasStack gasStack) {
            return test(gasStack) ? gasInstance.copy() : GasStack.EMPTY;
        }

        @Nonnull
        @Override
        public List<@NonNull GasStack> getRepresentations() {
            return Collections.singletonList(gasInstance);
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnumValue(IngredientType.SINGLE);
            gasInstance.writeToPacket(buffer);
        }

        @Nonnull
        @Override
        public JsonElement serialize() {
            JsonObject json = new JsonObject();
            json.addProperty(JsonConstants.AMOUNT, gasInstance.getAmount());
            json.addProperty(JsonConstants.GAS, gasInstance.getTypeRegistryName().toString());
            return json;
        }

        public static Single read(PacketBuffer buffer) {
            return new Single(GasStack.readFromPacket(buffer));
        }
    }

    public static class Tagged extends GasStackIngredient {

        @Nonnull
        private final Tag<Gas> tag;
        private final long amount;

        public Tagged(@Nonnull Tag<Gas> tag, long amount) {
            this.tag = tag;
            this.amount = amount;
        }

        @Override
        public boolean test(@Nonnull GasStack gasStack) {
            return testType(gasStack) && gasStack.getAmount() >= amount;
        }

        @Override
        public boolean testType(@Nonnull GasStack gasStack) {
            return testType(Objects.requireNonNull(gasStack).getType());
        }

        @Override
        public boolean testType(@Nonnull Gas gas) {
            return Objects.requireNonNull(gas).isIn(tag);
        }

        @Nonnull
        @Override
        public GasStack getMatchingInstance(@Nonnull GasStack gasStack) {
            if (test(gasStack)) {
                //Our gas is in the tag so we make a new stack with the given amount
                return new GasStack(gasStack, amount);
            }
            return GasStack.EMPTY;
        }

        @Nonnull
        @Override
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
            buffer.writeVarLong(amount);
        }

        @Nonnull
        @Override
        public JsonElement serialize() {
            JsonObject json = new JsonObject();
            json.addProperty(JsonConstants.AMOUNT, amount);
            json.addProperty(JsonConstants.TAG, tag.getId().toString());
            return json;
        }

        public static Tagged read(PacketBuffer buffer) {
            return new Tagged(ChemicalTags.gasTag(buffer.readResourceLocation()), buffer.readVarLong());
        }
    }

    public static class Multi extends GasStackIngredient {

        private final GasStackIngredient[] ingredients;

        protected Multi(@Nonnull GasStackIngredient... ingredients) {
            this.ingredients = ingredients;
        }

        @Override
        public boolean test(@Nonnull GasStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.test(stack));
        }

        @Override
        public boolean testType(@Nonnull GasStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(stack));
        }

        @Override
        public boolean testType(@Nonnull Gas gas) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(gas));
        }

        @Nonnull
        @Override
        public GasStack getMatchingInstance(@Nonnull GasStack stack) {
            for (GasStackIngredient ingredient : ingredients) {
                GasStack matchingInstance = ingredient.getMatchingInstance(stack);
                if (!matchingInstance.isEmpty()) {
                    return matchingInstance;
                }
            }
            return GasStack.EMPTY;
        }

        @Nonnull
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
            buffer.writeVarInt(ingredients.length);
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
            GasStackIngredient[] ingredients = new GasStackIngredient[buffer.readVarInt()];
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
