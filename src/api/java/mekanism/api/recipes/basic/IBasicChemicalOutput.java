package mekanism.api.recipes.basic;

import mekanism.api.chemical.ChemicalStackTemplate;

public interface IBasicChemicalOutput {

    /**
     * For Serializer use.
     *
     * @return the uncopied basic output
     */
    ChemicalStackTemplate getOutputRaw();
}