package mekanism.common.integration.crafttweaker.content.attribute.gas;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.content.attribute.CrTChemicalAttribute;
import mekanism.common.integration.crafttweaker.content.attribute.ICrTChemicalAttribute.ICrTGasAttribute;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_ATTRIBUTE_FUEL)
public class CrTFuelAttribute extends CrTChemicalAttribute implements ICrTGasAttribute {

    @ZenCodeType.Method
    public static CrTFuelAttribute create(int burnTicks, String energyDensity) {
        if (burnTicks <= 0) {
            throw new IllegalArgumentException("Fuel attributes must burn for at least one tick! Burn Ticks: " + burnTicks);
        }
        FloatingLong density = FloatingLong.parseFloatingLong(energyDensity, true);
        if (density.isZero()) {
            throw new IllegalArgumentException("Fuel attributes must have an energy density greater than zero!");
        }
        //Note: We don't allow suppliers from CrT as there is no real reason to allow them to change at runtime
        // from the context of CrT, the only real reason the values are suppliers is so that they can be adjusted
        // via configs
        return new CrTFuelAttribute(new GasAttributes.Fuel(() -> burnTicks, () -> density));
    }

    protected CrTFuelAttribute(GasAttributes.Fuel attribute) {
        super(attribute);
    }
}