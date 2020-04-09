package mekanism.common.util;

import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;

public class HeatUtils {

    public static FloatingLong getVaporizationEnthalpy() {
        return MekanismConfig.general.maxEnergyPerSteam.get();
    }
}