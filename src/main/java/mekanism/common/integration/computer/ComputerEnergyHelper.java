package mekanism.common.integration.computer;

import java.util.Locale;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;

public class ComputerEnergyHelper {

    @ComputerMethod(methodDescription = "Convert Mekanism Joules to Forge Energy")
    public static long joulesToFE(long joules) throws ComputerException {
        return convert(EnergyUnit.FORGE_ENERGY, joules, true);
    }

    @ComputerMethod(methodDescription = "Convert Forge Energy to Mekanism Joules")
    public static long feToJoules(long fe) throws ComputerException {
        return convert(EnergyUnit.FORGE_ENERGY, fe, false);
    }

    private static long convert(EnergyUnit type, long energy, boolean to) throws ComputerException {
        if (type.isEnabled()) {
            return to ? type.convertTo(energy) : type.convertFrom(energy);
        }
        String name = type.name().replace('_', ' ').toLowerCase(Locale.ROOT);
        String between = to ? "Joules and " + name : name + " and Joules";
        throw new ComputerException("Conversion between " + between + " is disabled in Mekanism's config or missing required mods.");
    }
}
