package mekanism.api.recipes.inputs;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;

/**
 * Created by Thiakil on 11/07/2019.
 */
public abstract class GasStackIngredient implements InputPredicate<@NonNull GasStack> {

    public static GasStackIngredient fromInstance(@NonNull GasStack instance) {
        return new Instance(instance.getGas(), instance.amount);
    }

    public static GasStackIngredient fromInstance(@NonNull Gas instance, int minAmount) {
        return new Instance(instance, minAmount);
    }

    public static GasStackIngredient fromName(@NonNull String name, int minAmount) {
        return new Named(name, minAmount);
    }

    public static class Instance extends GasStackIngredient {

        @NonNull
        private final Gas gasInstance;
        private final int minAmount;

        public Instance(@NonNull Gas gasInstance, int minAmount) {
            this.gasInstance = Objects.requireNonNull(gasInstance);
            this.minAmount = minAmount;
        }

        @Override
        public boolean test(@NonNull GasStack gasStack) {
            return testType(gasStack) && gasStack.amount >= minAmount;
        }

        @Override
        public boolean testType(@NonNull GasStack gasStack) {
            return Objects.requireNonNull(gasStack).getGas() == gasInstance;
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

        public Named(@Nonnull String name, int minAmount) {
            this.name = name;
            this.minAmount = minAmount;
        }

        @Override
        public boolean test(@NonNull GasStack gasStack) {
            return testType(gasStack) && gasStack.amount >= minAmount;
        }

        @Override
        public boolean testType(@NonNull GasStack gasStack) {
            return Objects.requireNonNull(gasStack).getGas().getName().equals(this.name);
        }

        @Override
        public @NonNull List<GasStack> getRepresentations() {
            Gas gas = GasRegistry.getGas(name);
            return gas != null ? Collections.singletonList(new GasStack(gas, minAmount)) : Collections.emptyList();
        }
    }
}
