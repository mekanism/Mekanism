package mekanism.api.recipes.vanilla_input;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;

/**
 * Simple implementation of a recipe input of one chemical.
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public record SingleChemicalRecipeInput(ChemicalStack chemical) implements ChemicalRecipeInput {

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
        return chemical.isEmpty();
    }
}