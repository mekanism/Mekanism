package mekanism.api.recipes.inputs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.Tag;
import net.minecraftforge.fluids.FluidStack;

/**
 * Created by Thiakil on 12/07/2019.
 */
//TODO: Allow for empty fluid stacks (at least in 1.14 with FluidStack.EMPTY)
public abstract class FluidStackIngredient implements InputIngredient<@NonNull FluidStack> {

    public static FluidStackIngredient from(@NonNull Fluid instance, int minAmount) {
        return from(new FluidStack(instance, minAmount));
    }

    public static FluidStackIngredient from(@NonNull FluidStack instance) {
        return new Instance(instance);
    }

    //TODO: Should we also accept a resource location for this.
    // Would it be clear the resourcelocation is for the tag and not registry name of the fluid
    public static FluidStackIngredient from(@NonNull Tag<Fluid> fluidTag, int minAmount) {
        return new Tagged(fluidTag, minAmount);
    }

    public static class Instance extends FluidStackIngredient {

        @NonNull
        private final FluidStack fluidInstance;

        public Instance(@NonNull FluidStack fluidInstance) {
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
    }

    public static class Tagged extends FluidStackIngredient {

        @Nonnull
        private final Tag<Fluid> tag;
        private final int minAmount;

        public Tagged(@Nonnull Tag<Fluid> tag, int minAmount) {
            this.tag = tag;
            this.minAmount = minAmount;
        }

        @Override
        public boolean test(@NonNull FluidStack fluidStack) {
            return testType(fluidStack) && fluidStack.getAmount() >= minAmount;
        }

        @Override
        public boolean testType(@NonNull FluidStack fluidStack) {
            return Objects.requireNonNull(fluidStack).getFluid().isIn(tag);
        }

        @Override
        public @NonNull FluidStack getMatchingInstance(@NonNull FluidStack fluidStack) {
            if (test(fluidStack)) {
                //Our fluid is in the tag so we make a new stack with the given amount
                return new FluidStack(fluidStack, minAmount);
            }
            return FluidStack.EMPTY;
        }

        @Override
        @NonNull
        public List<@NonNull FluidStack> getRepresentations() {
            //TODO: Can this be cached some how
            List<@NonNull FluidStack> representations = new ArrayList<>();
            for (Fluid fluid : tag.getAllElements()) {
                representations.add(new FluidStack(fluid, minAmount));
            }
            return representations;
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
    }
}
