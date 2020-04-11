package mekanism.common.util;

import mekanism.common.config.MekanismConfig;

public class HeatUtils {

    public static double getVaporizationEnthalpy() {
        return MekanismConfig.general.maxEnergyPerSteam.get().doubleValue();
    }
}