package mekanism.common.integration.crafttweaker.content.attribute;

import mekanism.api.chemical.attribute.ChemicalAttribute;

/**
 * Helper class for storing the internal chemical attribute to slightly reduce duplicate code.
 */
public class CrTChemicalAttribute implements ICrTChemicalAttribute {

    private final ChemicalAttribute attribute;

    protected CrTChemicalAttribute(ChemicalAttribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public ChemicalAttribute getInternal() {
        return attribute;
    }
}