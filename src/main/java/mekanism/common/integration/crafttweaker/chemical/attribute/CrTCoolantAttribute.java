package mekanism.common.integration.crafttweaker.chemical.attribute;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.function.Supplier;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.LazyChemicalProvider;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Defines the root data of a coolant, for use in Fission Reactors. Coolants have two primary properties: 'thermal enthalpy' and 'conductivity'. Thermal Enthalpy defines
 * how much energy one mB of the chemical can store; as such, lower values will cause reactors to require more of the chemical to stay cool. 'Conductivity' defines the
 * proportion of a reactor's available heat that can be used at an instant to convert this coolant's cool variant to its heated variant.
 */
@ZenRegister
@SuppressWarnings("removal")
@NativeTypeRegistration(value = ChemicalAttributes.Coolant.class, zenCodeName = CrTConstants.CLASS_ATTRIBUTE_COOLANT)
public class CrTCoolantAttribute {

    private CrTCoolantAttribute() {
    }

    /**
     * Defines a 'cooled' variant of a coolant for use in Fission Reactors.
     *
     * @param heatedChemical       Supplier to the heated variant of this chemical.
     * @param thermalEnthalpy Defines how much energy one mB of the chemical can store; lower values will cause reactors to require more of the chemical to stay cool.
     *                        Must be greater than zero.
     * @param conductivity    Defines the proportion of a reactor's available heat that can be used at an instant to convert this coolant's cool variant to its heated
     *                        variant. This value should be greater than zero, and at most one.
     *
     * @return Attribute representing a 'cooled' variant of a coolant.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalAttributes.CooledCoolant cooled(Supplier<Chemical> heatedChemical, double thermalEnthalpy, double conductivity) {
        return new ChemicalAttributes.CooledCoolant(new LazyChemicalProvider(heatedChemical), thermalEnthalpy, conductivity);
    }

    /**
     * Defines the 'heated' variant of a coolant for use in Fission Reactors.
     *
     * @param cooledChemical       Supplier to the cooled variant of this chemical.
     * @param thermalEnthalpy Defines how much energy one mB of the chemical can store; lower values will cause reactors to require more of the chemical to stay cool.
     *                        Must be greater than zero.
     * @param conductivity    Defines the proportion of a reactor's available heat that can be used at an instant to convert this coolant's cool variant to its heated
     *                        variant. This value should be greater than zero, and at most one.
     *
     * @return Attribute representing the 'heated' variant of a coolant.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalAttributes.HeatedCoolant heated(Supplier<Chemical> cooledChemical, double thermalEnthalpy, double conductivity) {
        return new ChemicalAttributes.HeatedCoolant(new LazyChemicalProvider(cooledChemical), thermalEnthalpy, conductivity);
    }

    /**
     * Gets the thermal enthalpy of this coolant. Thermal Enthalpy defines how much energy one mB of the chemical can store.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("thermalEnthalpy")
    public static double getThermalEnthalpy(ChemicalAttributes.Coolant _this) {
        return _this.getThermalEnthalpy();
    }

    /**
     * Gets the conductivity of this coolant. 'Conductivity' defines the proportion of a reactor's available heat that can be used at an instant to convert this coolant's
     * cool variant to its heated variant.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("conductivity")
    public static double getConductivity(ChemicalAttributes.Coolant _this) {
        return _this.getConductivity();
    }

    /**
     * Defines the 'cooled' variant of a coolant - the heated variant must be supplied in this class.
     */
    @ZenRegister
    @NativeTypeRegistration(value = ChemicalAttributes.CooledCoolant.class, zenCodeName = CrTConstants.CLASS_ATTRIBUTE_COOLED_COOLANT)
    public static class CrTCooledCoolantAttribute {

        private CrTCooledCoolantAttribute() {
        }

        /**
         * Gets the heated version of this coolant.
         */
        @ZenCodeType.Method
        public static Chemical getHeatedChemical(ChemicalAttributes.CooledCoolant _this) {
            return _this.getHeatedChemical();
        }
    }

    /**
     * Defines the 'heated' variant of a coolant - the cooled variant must be supplied in this class.
     */
    @ZenRegister
    @NativeTypeRegistration(value = ChemicalAttributes.HeatedCoolant.class, zenCodeName = CrTConstants.CLASS_ATTRIBUTE_HEATED_COOLANT)
    public static class CrTHeatedCoolantAttribute {

        private CrTHeatedCoolantAttribute() {
        }

        /**
         * Gets the heated version of this coolant.
         */
        @ZenCodeType.Method
        public static Chemical getCooledChemical(ChemicalAttributes.HeatedCoolant _this) {
            return _this.getCooledChemical();
        }
    }
}