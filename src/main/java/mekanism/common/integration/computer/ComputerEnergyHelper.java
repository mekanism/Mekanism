package mekanism.common.integration.computer;

import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.integration.energy.EnergyCompatUtils.EnergyType;

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
        if (MekanismConfig.general.blacklistForge.get()) {
            throw new ComputerException("Conversion between Joules and Forge Energy is disabled in Mekanism's config.");
        }
        return EnergyType.FORGE.convertToAsFloatingLong(joules);
    }

    @ComputerMethod
    private static FloatingLong feToJoules(FloatingLong fe) throws ComputerException {
        if (MekanismConfig.general.blacklistForge.get()) {
            throw new ComputerException("Conversion between Forge Energy and Joules is disabled in Mekanism's config.");
        }
        return EnergyType.FORGE.convertFrom(fe);
    }

    @ComputerMethod(requiredMods = MekanismHooks.IC2_MOD_ID)
    private static FloatingLong joulesToEU(FloatingLong joules) throws ComputerException {
        if (!EnergyCompatUtils.useIC2()) {
            throw new ComputerException("Conversion between Joules and Electrical Units is either disabled in Mekanism's config.");
        }
        return EnergyType.EU.convertToAsFloatingLong(joules);
    }

    @ComputerMethod(requiredMods = MekanismHooks.IC2_MOD_ID)
    private static FloatingLong euToJoules(FloatingLong eu) throws ComputerException {
        if (!EnergyCompatUtils.useIC2()) {
            throw new ComputerException("Conversion between Electrical Units and Joules is either disabled in Mekanism's config.");
        }
        return EnergyType.EU.convertFrom(eu);
    }
}