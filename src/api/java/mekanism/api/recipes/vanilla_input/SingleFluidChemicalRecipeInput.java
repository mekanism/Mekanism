package mekanism.api.recipes.vanilla_input;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Simple implementation of a recipe input of one fluid and one chemical.
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public record SingleFluidChemicalRecipeInput(FluidStack fluid, ChemicalStack chemical) implements FluidChemicalRecipeInput {

    @Override
    public FluidStack getFluid(int index) {
        if (index != 0) {
            throw new IllegalArgumentException("No fluid for index " + index);
        }
        return fluid;
    }

    @Override
    public ChemicalStack getChemical(int index) {
        if (index != 0) {
            throw new IllegalArgumentException("No chemical for index " + index);
        }
        return chemical;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return fluid.isEmpty() || chemical.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SingleFluidChemicalRecipeInput other = (SingleFluidChemicalRecipeInput) o;
        return chemical.equals(other.chemical) && FluidStack.matches(fluid, other.fluid);
    }

    @Override
    public int hashCode() {
        int hash = chemical.hashCode();
        hash = 31 * hash + FluidStack.hashFluidAndComponents(fluid);
        hash = 31 * hash + fluid.getAmount();
        return hash;
    }
}