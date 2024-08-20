package mekanism.api.recipes.basic;

import mekanism.api.chemical.ChemicalStack;

public interface IBasicChemicalOutput {

    /**
     * For Serializer use. DO NOT MODIFY RETURN VALUE.
     *
     * @return the uncopied basic output
     */
    ChemicalStack getOutputRaw();
}