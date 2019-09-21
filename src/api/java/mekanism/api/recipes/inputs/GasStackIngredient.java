package mekanism.api.recipes.inputs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.providers.IGasProvider;
import net.minecraft.tags.Tag;

/**
 * Created by Thiakil on 11/07/2019.
 */
//TODO: Allow for empty gas stacks (at least in 1.14 when we will have an empty variant of GasStack)
public abstract class GasStackIngredient implements InputIngredient<@NonNull GasStack> {

    public static GasStackIngredient from(@NonNull GasStack instance) {
        return from(instance.getGas(), instance.getAmount());
    }

    public static GasStackIngredient from(@NonNull IGasProvider instance, int minAmount) {
        return new Instance(instance.getGas(), minAmount);
    }

    public static GasStackIngredient from(@NonNull Tag<Gas> gasTag, int minAmount) {
        return new Tagged(gasTag, minAmount);
    }

    public abstract boolean testType(@NonNull Gas gas);

    public static class Instance extends GasStackIngredient {

        //TODO: Convert this to storing a GasStack?
        @NonNull
        private final Gas gasInstance;
        private final int minAmount;

        protected Instance(@NonNull Gas gasInstance, int minAmount) {
            this.gasInstance = Objects.requireNonNull(gasInstance);
            this.minAmount = minAmount;
        }

        @Override
        public boolean test(@NonNull GasStack gasStack) {
            return testType(gasStack) && gasStack.getAmount() >= minAmount;
        }

        @Override
        public boolean testType(@NonNull GasStack gasStack) {
            return testType(Objects.requireNonNull(gasStack).getGas());
        }

        @Override
        public boolean testType(@NonNull Gas gas) {
            return Objects.requireNonNull(gas) == gasInstance;
        }

        @Override
        public @NonNull GasStack getMatchingInstance(@NonNull GasStack gasStack) {
            return test(gasStack) ? new GasStack(gasInstance, minAmount) : GasStack.EMPTY;
        }

        @Override
        public @NonNull List<@NonNull GasStack> getRepresentations() {
            return Collections.singletonList(new GasStack(gasInstance, minAmount));
        }
    }

    public static class Tagged extends GasStackIngredient {

        @Nonnull
        private final Tag<Gas> tag;
        private final int minAmount;

        public Tagged(@Nonnull Tag<Gas> tag, int minAmount) {
            this.tag = tag;
            this.minAmount = minAmount;
        }

        @Override
        public boolean test(@NonNull GasStack gasStack) {
            return testType(gasStack) && gasStack.getAmount() >= minAmount;
        }

        @Override
        public boolean testType(@NonNull GasStack gasStack) {
            return testType(Objects.requireNonNull(gasStack).getGas());
        }

        @Override
        public boolean testType(@NonNull Gas gas) {
            return Objects.requireNonNull(gas).isIn(tag);
        }

        @Override
        public @NonNull GasStack getMatchingInstance(@NonNull GasStack gasStack) {
            if (test(gasStack)) {
                //Our gas is in the tag so we make a new stack with the given amount
                return new GasStack(gasStack, minAmount);
            }
            return GasStack.EMPTY;
        }

        @Override
        @NonNull
        public List<@NonNull GasStack> getRepresentations() {
            //TODO: Can this be cached some how
            List<@NonNull GasStack> representations = new ArrayList<>();
            for (Gas gas : tag.getAllElements()) {
                representations.add(new GasStack(gas, minAmount));
            }
            return representations;
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
    }
}
