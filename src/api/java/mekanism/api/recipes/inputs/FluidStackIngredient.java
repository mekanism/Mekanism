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
import mekanism.api.annotations.NonNull;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Created by Thiakil on 12/07/2019.
 */
//TODO: Allow for empty fluid stacks (at least in 1.14 with FluidStack.EMPTY)
public abstract class FluidStackIngredient implements InputIngredient<@NonNull FluidStack> {

    public static FluidStackIngredient from(@NonNull Fluid instance, int amount) {
        return from(new FluidStack(instance, amount));
    }

    public static FluidStackIngredient from(@NonNull FluidStack instance) {
        return new Single(instance);
    }

    public static FluidStackIngredient from(@NonNull Tag<Fluid> fluidTag, int minAmount) {
        return new Tagged(fluidTag, minAmount);
    }

    public static FluidStackIngredient read(PacketBuffer buffer) {
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
            throw new JsonSyntaxException("Expected item to be object or array of objects");
        }
        JsonObject jsonObject = json.getAsJsonObject();
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
        if (jsonObject.has("fluid") && jsonObject.has("tag")) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        } else if (jsonObject.has("fluid")) {
            ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(jsonObject, "fluid"));
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourceLocation);
            if (fluid == null || fluid == Fluids.EMPTY) {
                throw new JsonSyntaxException("Invalid fluid type '" + resourceLocation + "'");
            }
            //TODO: Allow for fluid NBT?
            return from(fluid, amount);
        } else if (jsonObject.has("tag")) {
            ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(jsonObject, "tag"));
            Tag<Fluid> tag = FluidTags.getCollection().get(resourceLocation);
            if (tag == null) {
                throw new JsonSyntaxException("Unknown fluid tag '" + resourceLocation + "'");
            }
            return from(tag, amount);
        }
        throw new JsonSyntaxException("Expected to receive a resource location representing either a tag or a fluid.");
    }

    public static FluidStackIngredient createMulti(FluidStackIngredient... ingredients) {
        if (ingredients.length == 0) {
            //TODO: Throw error
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
        //There should be more than a single item or we would have split out earlier
        return new Multi(cleanedIngredients.toArray(new FluidStackIngredient[0]));
    }

    public static class Single extends FluidStackIngredient {

        @NonNull
        private final FluidStack fluidInstance;

        public Single(@NonNull FluidStack fluidInstance) {
            this.fluidInstance = Objects.requireNonNull(fluidInstance);
        }

        @Override
        public boolean test(@NonNull FluidStack fluidStack) {
            return testType(fluidStack) && fluidStack.getAmount() >= fluidInstance.getAmount();
        }

        @Override
        public boolean testType(@NonNull FluidStack fluidStack) {
            return Objects.requireNonNull(fluidStack).isFluidEqual(fluidInstance);
        }

        @Override
        public @NonNull FluidStack getMatchingInstance(@NonNull FluidStack fluidStack) {
            return test(fluidStack) ? fluidInstance : FluidStack.EMPTY;
        }

        @Override
        @NonNull
        public List<@NonNull FluidStack> getRepresentations() {
            return Collections.singletonList(fluidInstance);
        }

        @Override
        public void write(PacketBuffer buffer) {
            fluidInstance.writeToPacket(buffer);
        }

        public static Single read(PacketBuffer buffer) {
            return new Single(FluidStack.readFromPacket(buffer));
        }
    }

    public static class Tagged extends FluidStackIngredient {

        @Nonnull
        private final Tag<Fluid> tag;
        private final int amount;

        public Tagged(@Nonnull Tag<Fluid> tag, int amount) {
            this.tag = tag;
            this.amount = amount;
        }

        @Override
        public boolean test(@NonNull FluidStack fluidStack) {
            return testType(fluidStack) && fluidStack.getAmount() >= amount;
        }

        @Override
        public boolean testType(@NonNull FluidStack fluidStack) {
            return Objects.requireNonNull(fluidStack).getFluid().isIn(tag);
        }

        @Override
        public @NonNull FluidStack getMatchingInstance(@NonNull FluidStack fluidStack) {
            if (test(fluidStack)) {
                //Our fluid is in the tag so we make a new stack with the given amount
                return new FluidStack(fluidStack, amount);
            }
            return FluidStack.EMPTY;
        }

        @Override
        @NonNull
        public List<@NonNull FluidStack> getRepresentations() {
            //TODO: Can this be cached some how
            List<@NonNull FluidStack> representations = new ArrayList<>();
            for (Fluid fluid : tag.getAllElements()) {
                representations.add(new FluidStack(fluid, amount));
            }
            return representations;
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnumValue(IngredientType.TAGGED);
            buffer.writeResourceLocation(tag.getId());
            buffer.writeInt(amount);
        }

        public static Tagged read(PacketBuffer buffer) {
            return new Tagged(new FluidTags.Wrapper(buffer.readResourceLocation()), buffer.readInt());
        }
    }

    //TODO: Maybe name this better, at the very least make it easier/possible to create new instances of this
    // Also cleanup the javadoc comment about this, and try to make the helpers that create a new instance
    // return a normal FluidStackIngredient (Single), if we only have a singular one
    public static class Multi extends FluidStackIngredient {

        private final FluidStackIngredient[] ingredients;

        protected Multi(@NonNull FluidStackIngredient... ingredients) {
            this.ingredients = ingredients;
        }

        @Override
        public boolean test(@NonNull FluidStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.test(stack));
        }

        @Override
        public boolean testType(@NonNull FluidStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(stack));
        }

        @Override
        public @NonNull FluidStack getMatchingInstance(@NonNull FluidStack stack) {
            for (FluidStackIngredient ingredient : ingredients) {
                FluidStack matchingInstance = ingredient.getMatchingInstance(stack);
                if (!matchingInstance.isEmpty()) {
                    return matchingInstance;
                }
            }
            return FluidStack.EMPTY;
        }

        @NonNull
        @Override
        public List<@NonNull FluidStack> getRepresentations() {
            List<@NonNull FluidStack> representations = new ArrayList<>();
            for (FluidStackIngredient ingredient : ingredients) {
                representations.addAll(ingredient.getRepresentations());
            }
            return representations;
        }

        @Override
        public void write(PacketBuffer buffer) {
            buffer.writeEnumValue(IngredientType.MULTI);
            buffer.writeInt(ingredients.length);
            for (FluidStackIngredient ingredient : ingredients) {
                ingredient.write(buffer);
            }
        }

        public static FluidStackIngredient read(PacketBuffer buffer) {
            //TODO: Verify this works
            FluidStackIngredient[] ingredients = new FluidStackIngredient[buffer.readInt()];
            for (int i = 0; i < ingredients.length; i++) {
                ingredients[i] = FluidStackIngredient.read(buffer);
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
