package mekanism.api.recipes.vanilla_input;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;

/**
 * Represents a recipe input that has an equal number of fluid and chemical inputs.
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public interface FluidChemicalRecipeInput extends FluidRecipeInput {

    ChemicalStack getChemical(int index);

    @Override
    default boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            if (!getFluid(i).isEmpty() && !getChemical(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}