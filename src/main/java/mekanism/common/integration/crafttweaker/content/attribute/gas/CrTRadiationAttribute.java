package mekanism.common.integration.crafttweaker.content.attribute.gas;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.content.attribute.CrTChemicalAttribute;
import mekanism.common.integration.crafttweaker.content.attribute.ICrTChemicalAttribute.ICrTGasAttribute;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_ATTRIBUTE_RADIATION)
public class CrTRadiationAttribute extends CrTChemicalAttribute implements ICrTGasAttribute {

    @ZenCodeType.Method
    public static CrTRadiationAttribute create(double radioactivity) {
        if (radioactivity <= 0) {
            throw new IllegalArgumentException("Radiation attribute should only be used when there actually is radiation! Radioactivity: " + radioactivity);
        }
        return new CrTRadiationAttribute(new GasAttributes.Radiation(radioactivity));
    }

    protected CrTRadiationAttribute(GasAttributes.Radiation attribute) {
        super(attribute);
    }
}