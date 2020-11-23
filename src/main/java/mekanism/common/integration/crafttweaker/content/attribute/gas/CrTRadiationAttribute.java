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

    /**
     * Creates an attribute representing the radioactivity of a chemical. This attribute <i>requires validation</i>, meaning chemical containers won't be able to accept
     * chemicals with this attribute by default.
     *
     * @param radioactivity Radioactivity of the chemical measured in Sv/h, must be greater than zero.
     *
     * @return Attribute representing the radioactivity of a substance.
     */
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