package mekanism.api.recipes.vanilla_input;

import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

/// Simple implementation of a recipe input of one fluid.
///
/// @since 10.6.0
public record SingleFluidRecipeInput(FluidStack fluid) implements FluidRecipeInput {

    @Override
    public FluidStack getFluid(int index) {
        if (index != 0) {
            throw new IllegalArgumentException("No fluid for index " + index);
        }
        return fluid;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return fluid.isEmpty();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return FluidStack.matches(fluid, ((SingleFluidRecipeInput) o).fluid);
    }

    @Override
    public int hashCode() {
        int hash = FluidStack.hashFluidAndComponents(fluid);
        return 31 * hash + fluid.amount();
    }
}