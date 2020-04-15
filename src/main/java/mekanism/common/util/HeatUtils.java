package mekanism.common.util;

import mekanism.common.config.MekanismConfig;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;

public class HeatUtils {

    public static final double BASE_BOIL_TEMP = TemperatureUnit.CELSIUS.zeroOffset + 100;

    public static double getVaporizationEnthalpy() {
        return MekanismConfig.general.maxEnergyPerSteam.get().doubleValue();
    }
}