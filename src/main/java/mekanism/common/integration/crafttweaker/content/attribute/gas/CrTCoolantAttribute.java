package mekanism.common.integration.crafttweaker.content.attribute.gas;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.providers.IGasProvider;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTGas;
import mekanism.common.integration.crafttweaker.content.attribute.CrTChemicalAttribute;
import mekanism.common.integration.crafttweaker.content.attribute.ICrTChemicalAttribute.ICrTGasAttribute;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_ATTRIBUTE_COOLANT)
public class CrTCoolantAttribute extends CrTChemicalAttribute implements ICrTGasAttribute {

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
    @ZenCodeType.Method
    public static CrTCoolantAttribute cooled(Supplier<ICrTGas> heatedGas, double thermalEnthalpy, double conductivity) {
        validateEnthalpyAndConductivity(thermalEnthalpy, conductivity);
        return new CrTCoolantAttribute(new GasAttributes.CooledCoolant(new CachingCrTGasProvider(heatedGas), thermalEnthalpy, conductivity));
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
    @ZenCodeType.Method
    public static CrTCoolantAttribute heated(Supplier<ICrTGas> cooledGas, double thermalEnthalpy, double conductivity) {
        validateEnthalpyAndConductivity(thermalEnthalpy, conductivity);
        return new CrTCoolantAttribute(new GasAttributes.HeatedCoolant(new CachingCrTGasProvider(cooledGas), thermalEnthalpy, conductivity));
    }

    private static void validateEnthalpyAndConductivity(double thermalEnthalpy, double conductivity) {
        if (thermalEnthalpy <= 0) {
            throw new IllegalArgumentException("Coolant attributes must have a thermal enthalpy greater than zero! Thermal Enthalpy: " + thermalEnthalpy);
        }
        if (conductivity <= 0 || conductivity > 1) {
            throw new IllegalArgumentException("Coolant attributes must have a conductivity greater than zero and at most one! Conductivity: " + conductivity);
        }
    }

    protected CrTCoolantAttribute(GasAttributes.Coolant attribute) {
        super(attribute);
    }

    /**
     * Helper class to cache the result of the {@link ICrTGas} supplier, so that we don't have to do registry lookups once it has properly been added to the registry.
     */
    private static class CachingCrTGasProvider implements IGasProvider {

        private Supplier<ICrTGas> gasSupplier;
        private Gas gas = MekanismAPI.EMPTY_GAS;

        private CachingCrTGasProvider(Supplier<ICrTGas> gasSupplier) {
            this.gasSupplier = gasSupplier;
        }

        @Nonnull
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