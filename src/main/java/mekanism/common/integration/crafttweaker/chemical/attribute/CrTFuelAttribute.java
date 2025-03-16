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
        throw new UnsupportedOperationException("Legacy fuel method no longer supported");
    }

    /**
     * Gets the max mb to burn per tick
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("burnTicks")
    public static int getBurnTicks(ChemicalAttributes.Fuel _this) {
        return 1;
    }
}