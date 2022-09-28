package mekanism.common.integration.computer;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;

public class ComputerEnergyHelper {

    public static Map<String, BoundComputerMethod> getMethods() {
        //Linked map to ensure that the order is persisted
        Map<String, BoundComputerMethod> boundMethods = new LinkedHashMap<>();
        //Get all static computer methods of this class
        ComputerMethodMapper.INSTANCE.getAndBindToHandler(ComputerEnergyHelper.class, null, boundMethods);
        return boundMethods;
    }

    @ComputerMethod
    private static FloatingLong joulesToFE(FloatingLong joules) throws ComputerException {
        return convert(EnergyUnit.FORGE_ENERGY, joules, true);
    }

    @ComputerMethod
    private static FloatingLong feToJoules(FloatingLong fe) throws ComputerException {
        return convert(EnergyUnit.FORGE_ENERGY, fe, false);
    }

    @ComputerMethod(requiredMods = MekanismHooks.IC2_MOD_ID)
    private static FloatingLong joulesToEU(FloatingLong joules) throws ComputerException {
        return convert(EnergyUnit.ELECTRICAL_UNITS, joules, true);
    }

    @ComputerMethod(requiredMods = MekanismHooks.IC2_MOD_ID)
    private static FloatingLong euToJoules(FloatingLong eu) throws ComputerException {
        return convert(EnergyUnit.ELECTRICAL_UNITS, eu, false);
    }

    private static FloatingLong convert(EnergyUnit type, FloatingLong energy, boolean to) throws ComputerException {
        if (type.isEnabled()) {
            return to ? type.convertTo(energy) : type.convertFrom(energy);
        }
        String name = type.name().replace('_', ' ').toLowerCase(Locale.ROOT);
        String between = to ? "Joules and " + name : name + " and Joules";
        throw new ComputerException("Conversion between " + between + " is disabled in Mekanism's config or missing required mods.");
    }
}