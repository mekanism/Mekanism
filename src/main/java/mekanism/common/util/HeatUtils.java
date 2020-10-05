package mekanism.common.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mekanism.api.heat.HeatAPI;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.Color;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;

public class HeatUtils {

    private HeatUtils() {
    }

    public static final Int2ObjectMap<Color> colorCache = new Int2ObjectOpenHashMap<>();

    public static final double BASE_BOIL_TEMP = TemperatureUnit.CELSIUS.zeroOffset + 100;

    public static final double HEATED_COOLANT_TEMP = 100_000D;

    public static double getWaterThermalEnthalpy() {
        return MekanismConfig.general.maxEnergyPerSteam.get().doubleValue();
    }

    public static double getSteamEnergyEfficiency() {
        return 0.2;
    }

    public static Color getColorFromTemp(double temperature, Color baseColor) {
        double absTemp = temperature + HeatAPI.AMBIENT_TEMP;
        absTemp /= 100;

        if (colorCache.containsKey((int) absTemp)) {
            return colorCache.get((int) absTemp).blendOnto(baseColor);
        }

        double tmpCalc;
        double red, green, blue, alpha;
        double effectiveTemp = absTemp;

        if (effectiveTemp < 10) {
            effectiveTemp = 10;
        }
        if (effectiveTemp > 400) {
            effectiveTemp = 400;
        }

        if (effectiveTemp <= 66) {
            red = 1;
        } else {
            tmpCalc = effectiveTemp - 60;
            tmpCalc = 329.698727446 * Math.pow(tmpCalc, -0.1332047592);
            red = tmpCalc / 255D;
        }

        if (effectiveTemp <= 66) {
            tmpCalc = effectiveTemp;
            tmpCalc = 99.4708025861 * Math.log(tmpCalc) - 161.1195681661;
        } else {
            tmpCalc = effectiveTemp - 60;
            tmpCalc = 288.1221695283 * Math.pow(tmpCalc, -0.0755148492);
        }
        green = tmpCalc / 255D;

        if (effectiveTemp >= 66) {
            blue = 1;
        } else if (effectiveTemp <= 19) {
            blue = 0;
        } else {
            tmpCalc = effectiveTemp - 10;
            tmpCalc = 138.5177312231 * Math.log(tmpCalc) - 305.0447927307;

            blue = tmpCalc / 255D;
        }

        alpha = temperature / 1_000;

        //clamp to 0 <= n >= 1
        red = Math.min(Math.max(red, 0), 1);
        green = Math.min(Math.max(green, 0), 1);
        blue = Math.min(Math.max(blue, 0), 1);
        alpha = Math.min(Math.max(alpha, 0), 1);

        Color colorTemperature = Color.rgbad(red, green, blue, alpha);
        colorCache.put((int) absTemp, colorTemperature);
        return colorTemperature.blendOnto(baseColor);
    }
}