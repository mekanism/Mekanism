package mekanism.api.recipes.vanilla_input;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;

/**
 * Simple implementation of a recipe input of two chemicals.
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public record BiChemicalRecipeInput<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>(STACK left, STACK right) implements
      ChemicalRecipeInput<CHEMICAL, STACK> {

    @Override
    public STACK getChemical(int index) {
        if (index == 0) {
            return left;
        } else if (index == 1) {
            return right;
        }
        throw new IllegalArgumentException("No chemical for index " + index);
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return left.isEmpty() || right.isEmpty();
    }
}