package mekanism.api.recipes.vanilla_input;

import com.mojang.datafixers.util.Either;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Simple implementation of a recipe input of for {@link mekanism.api.recipes.RotaryRecipe}.
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public record RotaryRecipeInput(Either<FluidStack, ChemicalStack> input) implements FluidChemicalRecipeInput {

    @Override
    public FluidStack getFluid(int index) {
        if (index != 0) {
            throw new IllegalArgumentException("No fluid for index " + index);
        }
        return input.left().orElse(FluidStack.EMPTY);
    }

    @Override
    public ChemicalStack getChemical(int index) {
        if (index != 0) {
            throw new IllegalArgumentException("No chemical for index " + index);
        }
        return input.right().orElse(ChemicalStack.EMPTY);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return input.map(FluidStack::isEmpty, ChemicalStack::isEmpty);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RotaryRecipeInput other = (RotaryRecipeInput) o;
        if (input.left().isPresent() && other.input.left().isPresent()) {
            return FluidStack.matches(input.left().get(), other.input.left().get());
        }
        //If at least one has a chemical, just check if the eithers are equal
        return input.equals(other.input);
    }

    @Override
    public int hashCode() {
        return input.map(fluid -> {
            int hash = FluidStack.hashFluidAndComponents(fluid);
            return 31 * hash + fluid.getAmount();
        }, ChemicalStack::hashCode);
    }
}