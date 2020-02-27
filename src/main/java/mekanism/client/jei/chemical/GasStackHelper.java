package mekanism.client.jei.chemical;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;

public class GasStackHelper extends ChemicalStackHelper<Gas, GasStack> {

    @Override
    protected GasStack getEmptyStack() {
        return GasStack.EMPTY;
    }

    @Override
    protected String getType() {
        return "Gas";
    }
}