package mekanism.common.integration.crafttweaker.content.attribute.gas;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTFloatingLong;
import mekanism.common.integration.crafttweaker.content.attribute.CrTChemicalAttribute;
import mekanism.common.integration.crafttweaker.content.attribute.ICrTChemicalAttribute.ICrTGasAttribute;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_ATTRIBUTE_FUEL)
public class CrTFuelAttribute extends CrTChemicalAttribute implements ICrTGasAttribute {

    /**
     * Defines a fuel which can be processed by a Gas-Burning Generator to produce energy.
     *
     * @param burnTicks     The number of ticks one mB of fuel can be burned for before being depleted; must be greater than zero.
     * @param energyDensity The energy density in one mB of fuel; must be greater than zero.
     *
     * @return Attribute representing the stats a substance has as a fuel.
     */
    @ZenCodeType.Method
    public static CrTFuelAttribute create(int burnTicks, CrTFloatingLong energyDensity) {
        if (burnTicks <= 0) {
            throw new IllegalArgumentException("Fuel attributes must burn for at least one tick! Burn Ticks: " + burnTicks);
        }
        FloatingLong density = energyDensity.getInternalAsConst();
        if (density.isZero()) {
            throw new IllegalArgumentException("Fuel attributes must have an energy density greater than zero!");
        }
        //Note: We don't allow suppliers from CrT as there is no real reason to allow them to change at runtime from the
        // context of CrT, the only real reason the values are suppliers is so that they can be adjusted via configs
        return new CrTFuelAttribute(new GasAttributes.Fuel(() -> burnTicks, () -> density));
    }

    protected CrTFuelAttribute(GasAttributes.Fuel attribute) {
        super(attribute);
    }
}