package mekanism.common.integration.crafttweaker.content.attribute;

import mekanism.api.chemical.attribute.ChemicalAttribute;

//TODO: Figure out if this class needs to be registered to CrT, my suspicion is not
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