package mekanism.api.recipes.basic;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;

public interface IBasicChemicalOutput<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> {

    /**
     * For Serializer use. DO NOT MODIFY RETURN VALUE.
     *
     * @return the uncopied basic output
     */
    STACK getOutputRaw();
}