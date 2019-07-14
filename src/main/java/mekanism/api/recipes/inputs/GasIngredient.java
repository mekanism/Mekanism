package mekanism.api.recipes.inputs;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;

/**
 * Created by Thiakil on 11/07/2019.
 */
public abstract class GasIngredient implements Predicate<@NonNull Gas> {

    public static GasIngredient fromInstance(@NonNull Gas instance){
        return new Instance(instance);
    }

    public static GasIngredient fromName(@NonNull String name) {
        return new Named(name);
    }

    public static class Instance extends GasIngredient {
        @NonNull
        private final Gas gasInstance;

        public Instance(@NonNull Gas gasInstance) {
            this.gasInstance = Objects.requireNonNull(gasInstance);
        }

        @Override
        public boolean test(@NonNull Gas gas) {
            return Objects.requireNonNull(gas) == gasInstance;
        }
    }

    public static class Named extends GasIngredient {
        @Nonnull
        private final String name;

        public Named(@Nonnull String name) {
            this.name = name;
        }

        @Override
        public boolean test(@NonNull Gas gas) {
            return Objects.requireNonNull(gas).getName().equals(this.name);
        }
    }
}
