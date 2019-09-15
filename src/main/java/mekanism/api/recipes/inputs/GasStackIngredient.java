package mekanism.api.recipes.inputs;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;

/**
 * Created by Thiakil on 11/07/2019.
 */
//TODO: Allow for empty gas stacks (at least in 1.14 when we will have an empty variant of GasStack)
public abstract class GasStackIngredient implements InputIngredient<@NonNull GasStack> {

    public static GasStackIngredient from(@NonNull GasStack instance) {
        return from(instance.getGas(), instance.amount);
    }

    public static GasStackIngredient from(@NonNull Gas instance, int minAmount) {
        return new Instance(instance, minAmount);
    }

    public static GasStackIngredient from(@NonNull String name, int minAmount) {
        return new Named(name, minAmount);
    }

    public abstract boolean testType(@NonNull Gas gas);

    public static class Instance extends GasStackIngredient {

        @NonNull
        private final Gas gasInstance;
        private final int minAmount;

        protected Instance(@NonNull Gas gasInstance, int minAmount) {
            this.gasInstance = Objects.requireNonNull(gasInstance);
            this.minAmount = minAmount;
        }

        @Override
        public boolean test(@NonNull GasStack gasStack) {
            return testType(gasStack) && gasStack.amount >= minAmount;
        }

        @Override
        public boolean testType(@NonNull GasStack gasStack) {
            return testType(Objects.requireNonNull(gasStack).getGas());
        }

        @Override
        public boolean testType(@NonNull Gas gas) {
            return Objects.requireNonNull(gas) == gasInstance;
        }

        @Nullable
        @Override
        //TODO: 1.14 make this return  @NonNull GasStack like IntelliJ wants when implementing unimplemented interface methods
        public GasStack getMatchingInstance(@NonNull GasStack gasStack) {
            return test(gasStack) ? new GasStack(gasInstance, minAmount) : null;
        }

        @Override
        public @NonNull List<GasStack> getRepresentations() {
            return Collections.singletonList(new GasStack(gasInstance, minAmount));
        }
    }

    //TODO: 1.14 remove/replace with one that is based off of Tags
    public static class Named extends GasStackIngredient {

        @Nonnull
        private final String name;
        private final int minAmount;

        protected Named(@Nonnull String name, int minAmount) {
            this.name = name;
            this.minAmount = minAmount;
        }

        @Override
        public boolean test(@NonNull GasStack gasStack) {
            return testType(gasStack) && gasStack.amount >= minAmount;
        }

        @Override
        public boolean testType(@NonNull GasStack gasStack) {
            return testType(Objects.requireNonNull(gasStack).getGas());
        }

        @Override
        public boolean testType(@NonNull Gas gas) {
            return Objects.requireNonNull(gas).getName().equals(this.name);
        }

        @Nullable
        @Override
        //TODO: 1.14 make this return  @NonNull GasStack like IntelliJ wants when implementing unimplemented interface methods
        public GasStack getMatchingInstance(@NonNull GasStack gasStack) {
            if (test(gasStack)) {
                Gas gas = GasRegistry.getGas(name);
                return gas == null ? null : new GasStack(gas, minAmount);
            }
            return null;
        }

        @Override
        public @NonNull List<GasStack> getRepresentations() {
            Gas gas = GasRegistry.getGas(name);
            return gas != null ? Collections.singletonList(new GasStack(gas, minAmount)) : Collections.emptyList();
        }
    }

    //TODO: Should this be more similar to how ItemStackIngredient.Multi is in that it stores multiple GasStackIngredients
    // Benefit would be that then it supports Named, except it would come with a bit of overhead
    public static class Multi extends GasStackIngredient {

        //TODO: Should this be a List or an array
        private final @NonNull GasStack[] matchingStacks;

        //TODO: Make a way to get this that returns a normal GasStackIngredient if we only have one matching stack
        protected Multi(@NonNull GasStack... matching) {
            matchingStacks = matching;
        }

        /**
         * @implNote Does not proxy the gas comparision to testType, so that it only has to loop once, and behaves better if for some reason there are multiple gas stacks
         * of the same type but different sizes. Though that will likely cause other issues.
         */
        @Override
        public boolean test(@NonNull GasStack gasStack) {
            Gas gas = Objects.requireNonNull(gasStack).getGas();
            return Arrays.stream(matchingStacks).anyMatch(stack -> gas == stack.getGas() && gasStack.amount >= stack.amount);
        }

        @Override
        public boolean testType(@NonNull GasStack gasStack) {
            return testType(Objects.requireNonNull(gasStack).getGas());
        }

        @Override
        public boolean testType(@NonNull Gas gas) {
            Objects.requireNonNull(gas);
            return Arrays.stream(matchingStacks).anyMatch(stack -> gas == stack.getGas());
        }

        @Nullable
        @Override
        //TODO: 1.14 make this return  @NonNull GasStack like InteliJ wants when implementing unimplemented interface methods
        public GasStack getMatchingInstance(@NonNull GasStack gasStack) {
            Objects.requireNonNull(gasStack);
            return Arrays.stream(matchingStacks).filter(stack -> gasStack.getGas() == stack.getGas() && gasStack.amount >= stack.amount).findFirst().orElse(null);
        }

        @Override
        public @NonNull List<GasStack> getRepresentations() {
            return Arrays.asList(matchingStacks);
        }
    }
}
