package mekanism.common.capabilities.chemical;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.merged.ChemicalType;

public class BoxedChemicalHandler {

    public <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> IChemicalHandler<CHEMICAL, STACK> getHandlerFor(ChemicalType chemicalType) {
        //TODO: Implement
        return null;
    }
}