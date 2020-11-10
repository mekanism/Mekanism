package mekanism.common.integration.crafttweaker.content.attribute.gas;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import java.util.function.Supplier;
import mekanism.api.chemical.gas.attribute.GasAttributes;
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
        //TODO: If suppliers aren't the way to go here, maybe we should do something like registry objects?
        validateEnthalpyAndConductivity(thermalEnthalpy, conductivity);
        return new CrTCoolantAttribute(new GasAttributes.CooledCoolant(() -> heatedGas.get().getChemical(), thermalEnthalpy, conductivity));
    }

    @ZenCodeType.Method
    public static CrTCoolantAttribute heated(Supplier<ICrTGas> cooledGas, double thermalEnthalpy, double conductivity) {
        //TODO: If suppliers aren't the way to go here, maybe we should do something like registry objects?
        validateEnthalpyAndConductivity(thermalEnthalpy, conductivity);
        return new CrTCoolantAttribute(new GasAttributes.HeatedCoolant(() -> cooledGas.get().getChemical(), thermalEnthalpy, conductivity));
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
}