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

    @ZenCodeType.Method
    public static CrTCoolantAttribute cooled(Supplier<ICrTGas> heatedGas, double thermalEnthalpy, double conductivity) {
        validateEnthalpyAndConductivity(thermalEnthalpy, conductivity);
        return new CrTCoolantAttribute(new GasAttributes.CooledCoolant(new CachingCrTGasProvider(heatedGas), thermalEnthalpy, conductivity));
    }

    @ZenCodeType.Method
    public static CrTCoolantAttribute heated(Supplier<ICrTGas> cooledGas, double thermalEnthalpy, double conductivity) {
        validateEnthalpyAndConductivity(thermalEnthalpy, conductivity);
        return new CrTCoolantAttribute(new GasAttributes.HeatedCoolant(new CachingCrTGasProvider(cooledGas), thermalEnthalpy, conductivity));
    }

    private static void validateEnthalpyAndConductivity(double thermalEnthalpy, double conductivity) {
        if (thermalEnthalpy <= 0) {
            //TODO: Figure out what bounds we actually want/can allow
            throw new IllegalArgumentException("Coolant attributes must have a thermal enthalpy greater than zero! Thermal Enthalpy: " + thermalEnthalpy);
        }
        if (conductivity <= 0) {
            //TODO: Figure out what bounds we actually want/can allow
            throw new IllegalArgumentException("Coolant attributes must have a conductivity greater than zero! Conductivity: " + conductivity);
        }
    }

    protected CrTCoolantAttribute(GasAttributes.Coolant attribute) {
        super(attribute);
    }

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