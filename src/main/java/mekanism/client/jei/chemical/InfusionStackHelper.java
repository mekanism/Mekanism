package mekanism.client.jei.chemical;

import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionStack;

public class InfusionStackHelper extends ChemicalStackHelper<InfuseType, InfusionStack> {

    @Override
    protected InfusionStack getEmptyStack() {
        return InfusionStack.EMPTY;
    }

    @Override
    protected String getType() {
        return "Infuse Type";
    }
}