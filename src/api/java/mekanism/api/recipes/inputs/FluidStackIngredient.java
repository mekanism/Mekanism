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
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NonNull;
import mekanism.api.providers.IFluidProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

/**
 * Base implementation for how Mekanism handle's FluidStack Ingredients.
 */
public abstract class FluidStackIngredient implements InputIngredient<@NonNull FluidStack> {

    /**
     * Creates a Fluid Stack Ingredient that matches a given fluid and amount.
     *
     * @param instance Fluid to match.
     * @param amount   Amount needed.
     */
    public static FluidStackIngredient from(@Nonnull Fluid instance, int amount) {
        return from(new FluidStack(instance, amount));
    }

    /**
     * Creates a Fluid Stack Ingredient that matches a provided fluid and amount.
     *
     * @param provider Fluid provider that provides the fluid to match.
     * @param amount   Amount needed.
     */
    public static FluidStackIngredient from(@Nonnull IFluidProvider provider, int amount) {
        return from(provider.getFluidStack(amount));
    }

    /**
     * Creates a Fluid Stack Ingredient that matches a given fluid stack.
     *
     * @param instance Fluid stack to match.
     */
    public static FluidStackIngredient from(@Nonnull FluidStack instance) {
        return new Single(instance);
    }

    /**
     * Creates a Fluid Stack Ingredient that matches a given fluid tag and amount.
     *
     * @param tag    Tag to match.
     * @param amount Amount needed.
     */
    public static FluidStackIngredient from(@Nonnull ITag<Fluid> tag, int amount) {
        return new Tagged(tag, amount);
    }

    /**
     * Reads a Fluid Stack Ingredient from a Packet Buffer.
     *
     * @param buffer Buffer to read from.
     *
     * @return Fluid Stack Ingredient.
     */
    public static FluidStackIngredient read(PacketBuffer buffer) {
        IngredientType type = buffer.readEnum(IngredientType.class);
        if (type == IngredientType.SINGLE) {
            return from(FluidStack.readFromPacket(buffer));
        } else if (type == IngredientType.TAGGED) {
            return from(FluidTags.bind(buffer.readResourceLocation().toString()), buffer.readVarInt());
        }
        FluidStackIngredient[] ingredients = new FluidStackIngredient[buffer.readVarInt()];
        for (int i = 0; i < ingredients.length; i++) {
            ingredients[i] = FluidStackIngredient.read(buffer);
        }
        return createMulti(ingredients);
    }

    /**
     * Helper to deserialize a Json Object into a Fluid Stack Ingredient.
     *
     * @param json Json object to deserialize.
     *
     * @return Fluid Stack Ingredient.
     */
    public static FluidStackIngredient deserialize(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Ingredient cannot be null");
        }
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            int size = jsonArray.size();
            if (size == 0) {
                throw new JsonSyntaxException("Ingredient array cannot be empty, at least one ingredient must be defined");
            } else if (size > 1) {
                FluidStackIngredient[] ingredients = new FluidStackIngredient[size];
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
            throw new JsonSyntaxException("Expected fluid to be object or array of objects");
        }
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has(JsonConstants.FLUID) && jsonObject.has(JsonConstants.TAG)) {
            throw new JsonParseException("An ingredient entry is either a tag or an fluid, not both");
        } else if (jsonObject.has(JsonConstants.FLUID)) {
            return from(SerializerHelper.deserializeFluid(jsonObject));
        } else if (jsonObject.has(JsonConstants.TAG)) {
            if (!jsonObject.has(JsonConstants.AMOUNT)) {
                throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
            }
            JsonElement count = jsonObject.get(JsonConstants.AMOUNT);
            if (!JSONUtils.isNumberValue(count)) {
                throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
            }
            int amount = count.getAsJsonPrimitive().getAsInt();
            if (amount < 1) {
                throw new JsonSyntaxException("Expected amount to be greater than zero.");
            }
            ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getAsString(jsonObject, JsonConstants.TAG));
            ITag<Fluid> tag = TagCollectionManager.getInstance().getFluids().getTag(resourceLocation);
            if (tag == null) {
                throw new JsonSyntaxException("Unknown fluid tag '" + resourceLocation + "'");
            }
            return from(tag, amount);
        }
        throw new JsonSyntaxException("Expected to receive a resource location representing either a tag or a fluid.");
    }

    /**
     * Combines multiple Fluid Stack Ingredients into a single Fluid Stack Ingredient.
     *
     * @param ingredients Ingredients to combine.
     *
     * @return Combined Fluid Stack Ingredient.
     */
    public static FluidStackIngredient createMulti(FluidStackIngredient... ingredients) {
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create a multi ingredient out of no ingredients.");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        List<FluidStackIngredient> cleanedIngredients = new ArrayList<>();
        for (FluidStackIngredient ingredient : ingredients) {
            if (ingredient instanceof Multi) {
                //Don't worry about if our inner ingredients are multi as well, as if this is the only external method for
                // creating a multi ingredient, then we are certified they won't be of a higher depth
                cleanedIngredients.addAll(Arrays.asList(((Multi) ingredient).ingredients));
            } else {
                cleanedIngredients.add(ingredient);
            }
        }
        //There should be more than a single fluid, or we would have split out earlier
        return new Multi(cleanedIngredients.toArray(new FluidStackIngredient[0]));
    }

    public static class Single extends FluidStackIngredient {

        @Nonnull
        private final FluidStack fluidInstance;

        private Single(@Nonnull FluidStack fluidInstance) {
            this.fluidInstance = Objects.requireNonNull(fluidInstance);
        }

        @Override
        public boolean test(@Nonnull FluidStack fluidStack) {
            return testType(fluidStack) && fluidStack.getAmount() >= fluidInstance.getAmount();
        }

        @Override
        public boolean testType(@Nonnull FluidStack fluidStack) {
            return Objects.requireNonNull(fluidStack).isFluidEqual(fluidInstance);
        }

        @Nonnull
        @Override
        public FluidStack getMatchingInstance(@Nonnull FluidStack fluidStack) {
            return test(fluidStack) ? fluidInstance.copy() : FluidStack.EMPTY;
        }

        @Override
        public long getNeededAmount(@Nonnull FluidStack stack) {
            return testType(stack) ? fluidInstance.getAmount() : 0;
        }

        @Nonnull
        @Override
        public List<@NonNull FluidStack> getRepresentations() {
            return Collections.singletonList(fluidInstance);
        }

        /**
         * For use in recipe input caching. Do not use this to modify the backing stack.
         */
        public FluidStack getInputRaw() {
            return fluidInstance;
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnum(IngredientType.SINGLE);
            fluidInstance.writeToPacket(buffer);
        }

        @Nonnull
        @Override
        public JsonElement serialize() {
            JsonObject json = new JsonObject();
            json.addProperty(JsonConstants.AMOUNT, fluidInstance.getAmount());
            json.addProperty(JsonConstants.FLUID, fluidInstance.getFluid().getRegistryName().toString());
            if (fluidInstance.hasTag()) {
                json.addProperty(JsonConstants.NBT, fluidInstance.getTag().toString());
            }
            return json;
        }
    }

    public static class Tagged extends FluidStackIngredient {

        @Nonnull
        private final ITag<Fluid> tag;
        private final int amount;

        private Tagged(@Nonnull ITag<Fluid> tag, int amount) {
            this.tag = tag;
            this.amount = amount;
        }

        @Override
        public boolean test(@Nonnull FluidStack fluidStack) {
            return testType(fluidStack) && fluidStack.getAmount() >= amount;
        }

        @Override
        public boolean testType(@Nonnull FluidStack fluidStack) {
            return Objects.requireNonNull(fluidStack).getFluid().is(tag);
        }

        @Nonnull
        @Override
        public FluidStack getMatchingInstance(@Nonnull FluidStack fluidStack) {
            if (test(fluidStack)) {
                //Our fluid is in the tag, so we make a new stack with the given amount
                return new FluidStack(fluidStack, amount);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public long getNeededAmount(@Nonnull FluidStack stack) {
            return testType(stack) ? amount : 0;
        }

        @Nonnull
        @Override
        public List<@NonNull FluidStack> getRepresentations() {
            //TODO: Can this be cached some how
            List<@NonNull FluidStack> representations = new ArrayList<>();
            for (Fluid fluid : TagResolverHelper.getRepresentations(tag)) {
                representations.add(new FluidStack(fluid, amount));
            }
            return representations;
        }

        /**
         * For use in recipe input caching.
         */
        public List<Fluid> getRawInput() {
            return tag.getValues();
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnum(IngredientType.TAGGED);
            buffer.writeResourceLocation(TagCollectionManager.getInstance().getFluids().getIdOrThrow(tag));
            buffer.writeVarInt(amount);
        }

        @Nonnull
        @Override
        public JsonElement serialize() {
            JsonObject json = new JsonObject();
            json.addProperty(JsonConstants.AMOUNT, amount);
            json.addProperty(JsonConstants.TAG, TagCollectionManager.getInstance().getFluids().getIdOrThrow(tag).toString());
            return json;
        }
    }

    public static class Multi extends FluidStackIngredient {

        private final FluidStackIngredient[] ingredients;

        private Multi(@Nonnull FluidStackIngredient... ingredients) {
            this.ingredients = ingredients;
        }

        @Override
        public boolean test(@Nonnull FluidStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.test(stack));
        }

        @Override
        public boolean testType(@Nonnull FluidStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(stack));
        }

        @Nonnull
        @Override
        public FluidStack getMatchingInstance(@Nonnull FluidStack stack) {
            for (FluidStackIngredient ingredient : ingredients) {
                FluidStack matchingInstance = ingredient.getMatchingInstance(stack);
                if (!matchingInstance.isEmpty()) {
                    return matchingInstance;
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public long getNeededAmount(@Nonnull FluidStack stack) {
            for (FluidStackIngredient ingredient : ingredients) {
                long amount = ingredient.getNeededAmount(stack);
                if (amount > 0) {
                    return amount;
                }
            }
            return 0;
        }

        @Nonnull
        @Override
        public List<@NonNull FluidStack> getRepresentations() {
            List<@NonNull FluidStack> representations = new ArrayList<>();
            for (FluidStackIngredient ingredient : ingredients) {
                representations.addAll(ingredient.getRepresentations());
            }
            return representations;
        }

        /**
         * For use in recipe input caching, checks all ingredients even if some match.
         *
         * @return {@code true} if any ingredient matches.
         */
        public boolean forEachIngredient(Predicate<FluidStackIngredient> checker) {
            boolean result = false;
            for (FluidStackIngredient ingredient : ingredients) {
                result |= checker.test(ingredient);
            }
            return result;
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnum(IngredientType.MULTI);
            buffer.writeVarInt(ingredients.length);
            for (FluidStackIngredient ingredient : ingredients) {
                ingredient.write(buffer);
            }
        }

        @Nonnull
        @Override
        public JsonElement serialize() {
            JsonArray json = new JsonArray();
            for (FluidStackIngredient ingredient : ingredients) {
                json.add(ingredient.serialize());
            }
            return json;
        }
    }

    private enum IngredientType {
        SINGLE,
        TAGGED,
        MULTI
    }
}
