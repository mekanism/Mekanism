package mekanism.api.recipes.vanilla_input;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;

/**
 * Simple implementation of a recipe input of one chemical.
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public record SingleChemicalRecipeInput<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>(STACK chemical) implements
      ChemicalRecipeInput<CHEMICAL, STACK> {

    @Override
    public STACK getChemical(int index) {
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