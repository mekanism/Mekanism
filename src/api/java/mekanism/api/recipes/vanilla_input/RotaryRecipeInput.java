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
}