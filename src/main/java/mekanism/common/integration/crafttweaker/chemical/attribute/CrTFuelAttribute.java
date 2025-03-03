package mekanism.common.integration.crafttweaker.chemical.attribute;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import mekanism.common.integration.crafttweaker.CrTConstants;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Defines a fuel which can be processed by a Gas-Burning Generator to produce energy. Fuels have two primary values: 'burn ticks', defining how many ticks one mB of fuel
 * can be burned for before being depleted, and 'energyDensity', defining how much energy is stored in one mB of fuel.
 */
@ZenRegister
@SuppressWarnings("removal")
@NativeTypeRegistration(value = ChemicalAttributes.Fuel.class, zenCodeName = CrTConstants.CLASS_ATTRIBUTE_FUEL)
public class CrTFuelAttribute {

    private CrTFuelAttribute() {
    }

    /**
     * Defines a fuel which can be processed by a Gas-Burning Generator to produce energy.
     *
     * @param burnTicks     The number of ticks one mB of fuel can be burned for before being depleted; must be greater than zero.
     * @param energyDensity The energy density in one mB of fuel; must be greater than zero.
     *
     * @return Attribute representing the stats a substance has as a fuel.
     */
    @ZenCodeType.StaticExpansionMethod
    @Deprecated(forRemoval = true, since = "10.7.11")
    public static ChemicalAttributes.Fuel create(int burnTicks, long energyDensity) {
        //Note: We don't allow suppliers from CrT as there is no real reason to allow them to change at runtime from the
        // context of CrT, the only real reason the values are suppliers is so that they can be adjusted via configs
        return new ChemicalAttributes.Fuel(burnTicks, energyDensity);
    }

    /**
     * Gets the number of ticks this fuel burns for.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("burnTicks")
    public static int getBurnTicks(ChemicalAttributes.Fuel _this) {
        return _this.getBurnTicks();
    }

    /**
     * Gets the amount of energy produced per tick of this fuel.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("energyPerTick")
    public static long getEnergyPerTick(ChemicalAttributes.Fuel _this) {
        return _this.getEnergyPerTick();
    }
}