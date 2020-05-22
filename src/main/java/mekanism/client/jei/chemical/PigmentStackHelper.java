package mekanism.client.jei.chemical;

import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;

public class PigmentStackHelper extends ChemicalStackHelper<Pigment, PigmentStack> {

    @Override
    protected PigmentStack getEmptyStack() {
        return PigmentStack.EMPTY;
    }

    @Override
    protected String getType() {
        return "Pigment";
    }
}