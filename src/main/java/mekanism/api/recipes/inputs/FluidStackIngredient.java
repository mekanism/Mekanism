package mekanism.api.recipes.inputs;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

/**
 * Created by Thiakil on 12/07/2019.
 */
public abstract class FluidStackIngredient implements InputIngredient<@NonNull FluidStack> {

    public static FluidStackIngredient from(@NonNull Fluid instance, int minAmount) {
        return from(new FluidStack(instance, minAmount));
    }

    public static FluidStackIngredient from(@NonNull FluidStack instance) {
        return new Instance(instance);
    }

    public static FluidStackIngredient from(@NonNull String name, int minAmount) {
        return new Named(name, minAmount);
    }

    public static class Instance extends FluidStackIngredient {

        @NonNull
        private final FluidStack fluidInstance;

        public Instance(@NonNull FluidStack fluidInstance) {
            this.fluidInstance = Objects.requireNonNull(fluidInstance);
        }

        @Override
        public boolean test(@NonNull FluidStack fluidStack) {
            return testType(fluidStack) && fluidStack.amount >= fluidInstance.amount;
        }

        @Override
        public boolean testType(@NonNull FluidStack fluidStack) {
            return Objects.requireNonNull(fluidStack).isFluidEqual(fluidInstance);
        }

        @Override
        @NonNull
        public List<FluidStack> getRepresentations() {
            return Collections.singletonList(fluidInstance);
        }
    }

    //TODO: 1.14 remove/replace with one that is based off of Tags
    public static class Named extends FluidStackIngredient {

        @Nonnull
        private final String name;
        private final int minAmount;

        public Named(@Nonnull String name, int minAmount) {
            this.name = name;
            this.minAmount = minAmount;
        }

        @Override
        public boolean test(@NonNull FluidStack fluidStack) {
            return testType(fluidStack) && fluidStack.amount >= minAmount;
        }

        @Override
        public boolean testType(@NonNull FluidStack fluidStack) {
            return Objects.requireNonNull(fluidStack).getFluid().getName().equals(this.name);
        }

        @Override
        @NonNull
        public List<FluidStack> getRepresentations() {
            Fluid fluid = FluidRegistry.getFluid(this.name);
            return fluid != null ? Collections.singletonList(new FluidStack(fluid, minAmount)) : Collections.emptyList();
        }
    }
}
