package mekanism.common.util;

import mekanism.common.config.MekanismConfig;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;

public class HeatUtils {

    public static final double BASE_BOIL_TEMP = TemperatureUnit.CELSIUS.zeroOffset + 100;

    public static final double HEATED_COOLANT_TEMP = 100_000D;

    public static double getWaterThermalEnthalpy() {
        return MekanismConfig.general.maxEnergyPerSteam.get().doubleValue();
    }

    public static double getSteamEnergyEfficiency() {
        return 0.2;
    }
}