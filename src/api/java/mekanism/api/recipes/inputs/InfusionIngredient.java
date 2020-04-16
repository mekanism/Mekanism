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
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfuseTypeTags;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.providers.IInfuseTypeProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.Tag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

/**
 * Created by Thiakil on 12/07/2019.
 */
public abstract class InfusionIngredient implements InputIngredient<@NonNull InfusionStack> {

    public static InfusionIngredient from(@Nonnull InfusionStack instance) {
        return from(instance.getType(), instance.getAmount());
    }

    public static InfusionIngredient from(@Nonnull IInfuseTypeProvider infuseType, long amount) {
        return new Single(infuseType.getInfusionStack(amount));
    }

    public static InfusionIngredient from(@Nonnull Tag<InfuseType> infuseTypeTag, long amount) {
        return new Tagged(infuseTypeTag, amount);
    }

    public abstract boolean testType(@Nonnull InfuseType infuseType);

    public static InfusionIngredient read(PacketBuffer buffer) {
        //TODO: Allow supporting serialization of different types than just the ones we implement?
        IngredientType type = buffer.readEnumValue(IngredientType.class);
        if (type == IngredientType.SINGLE) {
            return Single.read(buffer);
        } else if (type == IngredientType.TAGGED) {
            return Tagged.read(buffer);
        }
        return Multi.read(buffer);
    }

    public static InfusionIngredient deserialize(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Ingredient cannot be null");
        }
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            int size = jsonArray.size();
            if (size == 0) {
                throw new JsonSyntaxException("Ingredient array cannot be empty, at least one ingredient must be defined");
            } else if (size > 1) {
                InfusionIngredient[] ingredients = new InfusionIngredient[size];
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
        if (jsonObject.has(JsonConstants.INFUSE_TYPE) && jsonObject.has(JsonConstants.TAG)) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        } else if (jsonObject.has(JsonConstants.INFUSE_TYPE)) {
            return from(SerializerHelper.deserializeInfuseType(jsonObject));
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
            Tag<InfuseType> tag = InfuseTypeTags.getCollection().get(resourceLocation);
            if (tag == null) {
                throw new JsonSyntaxException("Unknown infuse type tag '" + resourceLocation + "'");
            }
            return from(tag, amount);
        }
        throw new JsonSyntaxException("Expected to receive a resource location representing either a tag or an infusion type.");
    }

    public static InfusionIngredient createMulti(InfusionIngredient... ingredients) {
        if (ingredients.length == 0) {
            //TODO: Throw error
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        List<InfusionIngredient> cleanedIngredients = new ArrayList<>();
        for (InfusionIngredient ingredient : ingredients) {
            if (ingredient instanceof Multi) {
                //Don't worry about if our inner ingredients are multi as well, as if this is the only external method for
                // creating a multi ingredient, then we are certified they won't be of a higher depth
                cleanedIngredients.addAll(Arrays.asList(((Multi) ingredient).ingredients));
            } else {
                cleanedIngredients.add(ingredient);
            }
        }
        //There should be more than a single item or we would have split out earlier
        return new Multi(cleanedIngredients.toArray(new InfusionIngredient[0]));
    }

    public static class Single extends InfusionIngredient {

        @Nonnull
        private final InfusionStack infusionInstance;

        public Single(@Nonnull InfusionStack infusionInstance) {
            this.infusionInstance = infusionInstance;
        }

        @Override
        public boolean test(@Nonnull InfusionStack infuseObject) {
            return testType(infuseObject) && infuseObject.getAmount() >= infusionInstance.getAmount();
        }

        @Override
        public boolean testType(@Nonnull InfusionStack infuseObject) {
            return infusionInstance.isTypeEqual(Objects.requireNonNull(infuseObject));
        }

        @Override
        public boolean testType(@Nonnull InfuseType infuseType) {
            return infusionInstance.isTypeEqual(Objects.requireNonNull(infuseType));
        }

        @Nonnull
        @Override
        public InfusionStack getMatchingInstance(@Nonnull InfusionStack infuseObject) {
            return test(infuseObject) ? infusionInstance.copy() : InfusionStack.EMPTY;
        }

        @Nonnull
        @Override
        public List<@NonNull InfusionStack> getRepresentations() {
            return Collections.singletonList(infusionInstance);
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnumValue(IngredientType.SINGLE);
            infusionInstance.writeToPacket(buffer);
        }

        @Nonnull
        @Override
        public JsonElement serialize() {
            JsonObject json = new JsonObject();
            json.addProperty(JsonConstants.AMOUNT, infusionInstance.getAmount());
            json.addProperty(JsonConstants.INFUSE_TYPE, infusionInstance.getTypeRegistryName().toString());
            return json;
        }

        public static Single read(PacketBuffer buffer) {
            return new Single(InfusionStack.readFromPacket(buffer));
        }
    }

    public static class Tagged extends InfusionIngredient {

        @Nonnull
        private final Tag<InfuseType> tag;
        private final long amount;

        public Tagged(@Nonnull Tag<InfuseType> tag, long amount) {
            this.tag = tag;
            this.amount = amount;
        }

        @Override
        public boolean test(@Nonnull InfusionStack infusionStack) {
            return testType(infusionStack) && infusionStack.getAmount() >= amount;
        }

        @Override
        public boolean testType(@Nonnull InfusionStack infusionStack) {
            return testType(Objects.requireNonNull(infusionStack).getType());
        }

        @Override
        public boolean testType(@Nonnull InfuseType infuseType) {
            return Objects.requireNonNull(infuseType).isIn(tag);
        }

        @Nonnull
        @Override
        public InfusionStack getMatchingInstance(@Nonnull InfusionStack infusionStack) {
            if (test(infusionStack)) {
                //Our infusion type is in the tag so we make a new stack with the given amount
                return new InfusionStack(infusionStack, amount);
            }
            return InfusionStack.EMPTY;
        }

        @Nonnull
        @Override
        public List<@NonNull InfusionStack> getRepresentations() {
            //TODO: Can this be cached some how
            List<@NonNull InfusionStack> representations = new ArrayList<>();
            for (InfuseType infuseType : tag.getAllElements()) {
                representations.add(new InfusionStack(infuseType, amount));
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
            return new Tagged(new InfuseTypeTags.Wrapper(buffer.readResourceLocation()), buffer.readVarLong());
        }
    }

    //TODO: Maybe name this better, at the very least make it easier/possible to create new instances of this
    // Also cleanup the javadoc comment about this, and try to make the helpers that create a new instance
    // return a normal InfusionIngredient (Single), if we only have a singular one
    public static class Multi extends InfusionIngredient {

        private final InfusionIngredient[] ingredients;

        protected Multi(@Nonnull InfusionIngredient... ingredients) {
            this.ingredients = ingredients;
        }

        @Override
        public boolean test(@Nonnull InfusionStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.test(stack));
        }

        @Override
        public boolean testType(@Nonnull InfusionStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(stack));
        }

        @Override
        public boolean testType(@Nonnull InfuseType infuseType) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(infuseType));
        }

        @Nonnull
        @Override
        public InfusionStack getMatchingInstance(@Nonnull InfusionStack stack) {
            for (InfusionIngredient ingredient : ingredients) {
                InfusionStack matchingInstance = ingredient.getMatchingInstance(stack);
                if (!matchingInstance.isEmpty()) {
                    return matchingInstance;
                }
            }
            return InfusionStack.EMPTY;
        }

        @Nonnull
        @Override
        public List<@NonNull InfusionStack> getRepresentations() {
            List<@NonNull InfusionStack> representations = new ArrayList<>();
            for (InfusionIngredient ingredient : ingredients) {
                representations.addAll(ingredient.getRepresentations());
            }
            return representations;
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnumValue(IngredientType.MULTI);
            buffer.writeVarInt(ingredients.length);
            for (InfusionIngredient ingredient : ingredients) {
                ingredient.write(buffer);
            }
        }

        @Nonnull
        @Override
        public JsonElement serialize() {
            JsonArray json = new JsonArray();
            for (InfusionIngredient ingredient : ingredients) {
                json.add(ingredient.serialize());
            }
            return json;
        }

        public static InfusionIngredient read(PacketBuffer buffer) {
            //TODO: Verify this works
            InfusionIngredient[] ingredients = new InfusionIngredient[buffer.readVarInt()];
            for (int i = 0; i < ingredients.length; i++) {
                ingredients[i] = InfusionIngredient.read(buffer);
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
