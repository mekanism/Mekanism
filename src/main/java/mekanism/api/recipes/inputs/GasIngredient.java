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
//TODO: Should this be merged with GasStackIngredient and just be of size one or zero
public abstract class GasIngredient implements InputPredicate<@NonNull Gas> {

    public static GasIngredient fromInstance(@NonNull Gas instance) {
        return new Instance(instance);
    }

    public static GasIngredient fromName(@NonNull String name) {
        return new Named(name);
    }

    /**
     * Primarily for JEI, a list of valid instances of the stack (i.e. a resolved GasStack(s) from the registry)
     *
     * @param size The size to make each returned stack
     *
     * @return List (empty means no valid registrations found and recipe is to be hidden)
     */
    public abstract @NonNull List<GasStack> getRepresentations(int size);

    public static class Instance extends GasIngredient {

        @NonNull
        private final Gas gasInstance;

        public Instance(@NonNull Gas gasInstance) {
            this.gasInstance = Objects.requireNonNull(gasInstance);
        }

        @Override
        public boolean test(@NonNull Gas gas) {
            return testType(gas);
        }

        @Override
        public @NonNull List<GasStack> getRepresentations(int size) {
            return Collections.singletonList(new GasStack(gasInstance, size));
        }

        @Override
        public boolean testType(@NonNull Gas gas) {
            return Objects.requireNonNull(gas) == gasInstance;
        }

        @Override
        public @NonNull List<@NonNull Gas> getRepresentations() {
            return Collections.singletonList(gasInstance);
        }
    }

    //TODO: 1.14 remove/replace with one that is based off of Tags
    public static class Named extends GasIngredient {

        @Nonnull
        private final String name;

        public Named(@Nonnull String name) {
            this.name = name;
        }

        @Override
        public boolean test(@NonNull Gas gas) {
            return testType(gas);
        }

        @Override
        public @NonNull List<GasStack> getRepresentations(int size) {
            Gas gas = GasRegistry.getGas(name);
            return gas != null ? Collections.singletonList(new GasStack(gas, size)) : Collections.emptyList();
        }

        @Override
        public boolean testType(@NonNull Gas gas) {
            return Objects.requireNonNull(gas).getName().equals(this.name);
        }

        @Override
        public @NonNull List<@NonNull Gas> getRepresentations() {
            Gas gas = GasRegistry.getGas(name);
            return gas != null ? Collections.singletonList(gas) : Collections.emptyList();
        }
    }
}
