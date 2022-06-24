package mekanism.common.integration.crafttweaker.chemical.attribute.gas;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.providers.IGasProvider;
import mekanism.common.integration.crafttweaker.CrTConstants;
import org.jetbrains.annotations.NotNull;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Defines the root data of a coolant, for use in Fission Reactors. Coolants have two primary properties: 'thermal enthalpy' and 'conductivity'. Thermal Enthalpy defines
 * how much energy one mB of the chemical can store; as such, lower values will cause reactors to require more of the chemical to stay cool. 'Conductivity' defines the
 * proportion of a reactor's available heat that can be used at an instant to convert this coolant's cool variant to its heated variant.
 */
@ZenRegister
@NativeTypeRegistration(value = GasAttributes.Coolant.class, zenCodeName = CrTConstants.CLASS_ATTRIBUTE_COOLANT)
public class CrTCoolantAttribute {

    private CrTCoolantAttribute() {
    }

    /**
     * Defines a 'cooled' variant of a coolant for use in Fission Reactors.
     *
     * @param heatedGas       Supplier to the heated variant of this chemical.
     * @param thermalEnthalpy Defines how much energy one mB of the chemical can store; lower values will cause reactors to require more of the chemical to stay cool.
     *                        Must be greater than zero.
     * @param conductivity    Defines the proportion of a reactor's available heat that can be used at an instant to convert this coolant's cool variant to its heated
     *                        variant. This value should be greater than zero, and at most one.
     *
     * @return Attribute representing a 'cooled' variant of a coolant.
     */
    @ZenCodeType.StaticExpansionMethod
    public static GasAttributes.CooledCoolant cooled(Supplier<Gas> heatedGas, double thermalEnthalpy, double conductivity) {
        validateEnthalpyAndConductivity(thermalEnthalpy, conductivity);
        return new GasAttributes.CooledCoolant(new CachingCrTGasProvider(heatedGas), thermalEnthalpy, conductivity);
    }

    /**
     * Defines the 'heated' variant of a coolant for use in Fission Reactors.
     *
     * @param cooledGas       Supplier to the cooled variant of this chemical.
     * @param thermalEnthalpy Defines how much energy one mB of the chemical can store; lower values will cause reactors to require more of the chemical to stay cool.
     *                        Must be greater than zero.
     * @param conductivity    Defines the proportion of a reactor's available heat that can be used at an instant to convert this coolant's cool variant to its heated
     *                        variant. This value should be greater than zero, and at most one.
     *
     * @return Attribute representing the 'heated' variant of a coolant.
     */
    @ZenCodeType.StaticExpansionMethod
    public static GasAttributes.HeatedCoolant heated(Supplier<Gas> cooledGas, double thermalEnthalpy, double conductivity) {
        validateEnthalpyAndConductivity(thermalEnthalpy, conductivity);
        return new GasAttributes.HeatedCoolant(new CachingCrTGasProvider(cooledGas), thermalEnthalpy, conductivity);
    }

    private static void validateEnthalpyAndConductivity(double thermalEnthalpy, double conductivity) {
        if (thermalEnthalpy <= 0) {
            throw new IllegalArgumentException("Coolant attributes must have a thermal enthalpy greater than zero! Thermal Enthalpy: " + thermalEnthalpy);
        }
        if (conductivity <= 0 || conductivity > 1) {
            throw new IllegalArgumentException("Coolant attributes must have a conductivity greater than zero and at most one! Conductivity: " + conductivity);
        }
    }

    /**
     * Gets the thermal enthalpy of this coolant. Thermal Enthalpy defines how much energy one mB of the chemical can store.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("thermalEnthalpy")
    public static double getThermalEnthalpy(GasAttributes.Coolant _this) {
        return _this.getThermalEnthalpy();
    }

    /**
     * Gets the conductivity of this coolant. 'Conductivity' defines the proportion of a reactor's available heat that can be used at an instant to convert this coolant's
     * cool variant to its heated variant.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("conductivity")
    public static double getConductivity(GasAttributes.Coolant _this) {
        return _this.getConductivity();
    }

    /**
     * Defines the 'cooled' variant of a coolant- the heated variant must be supplied in this class.
     */
    @ZenRegister
    @NativeTypeRegistration(value = GasAttributes.CooledCoolant.class, zenCodeName = CrTConstants.CLASS_ATTRIBUTE_COOLED_COOLANT)
    public static class CrTCooledCoolantAttribute {

        private CrTCooledCoolantAttribute() {
        }

        /**
         * Gets the heated version of this coolant.
         */
        @ZenCodeType.Method
        public static Gas getHeatedGas(GasAttributes.CooledCoolant _this) {
            return _this.getHeatedGas();
        }
    }

    /**
     * Defines the 'heated' variant of a coolant- the cooled variant must be supplied in this class.
     */
    @ZenRegister
    @NativeTypeRegistration(value = GasAttributes.HeatedCoolant.class, zenCodeName = CrTConstants.CLASS_ATTRIBUTE_HEATED_COOLANT)
    public static class CrTHeatedCoolantAttribute {

        private CrTHeatedCoolantAttribute() {
        }

        /**
         * Gets the heated version of this coolant.
         */
        @ZenCodeType.Method
        public static Gas getCooledGas(GasAttributes.HeatedCoolant _this) {
            return _this.getCooledGas();
        }
    }

    /**
     * Helper class to cache the result of the {@link Gas} supplier, so that we don't have to do registry lookups once it has properly been added to the registry.
     */
    private static class CachingCrTGasProvider implements IGasProvider {

        private Supplier<Gas> gasSupplier;
        private Gas gas = MekanismAPI.EMPTY_GAS;

        private CachingCrTGasProvider(Supplier<Gas> gasSupplier) {
            this.gasSupplier = gasSupplier;
        }

        @NotNull
        @Override
        public Gas getChemical() {
            if (gas.isEmptyType()) {
                //If our gas hasn't actually been set yet, set it from the gas supplier we have
                gas = gasSupplier.get().getChemical();
                if (gas.isEmptyType()) {
                    //If it is still empty (because the supplier was for an empty gas which we couldn't
                    // evaluate initially, throw an illegal state exception)
                    throw new IllegalStateException("Empty gas used for coolant attribute via a CraftTweaker Script.");
                }
                //Free memory of the supplier
                gasSupplier = null;
            }
            return gas;
        }
    }
}