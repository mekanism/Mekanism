package mekanism.api.recipes.vanilla_input;

import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Simple implementation of a recipe input of one fluid.
 *
 * @since 10.6.0
 */
@NothingNullByDefault
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
    public boolean equals(Object o) {
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
        return 31 * hash + fluid.getAmount();
    }
}