package mekanism.client.render;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mekanism.api.heat.HeatAPI;
import mekanism.common.Color;

public class ColorTemperature extends Color {

    public static Int2ObjectMap<ColorTemperature> cache = new Int2ObjectOpenHashMap<>();

    public double temp;

    public ColorTemperature(double r, double g, double b, double a, double t) {
        super(r, g, b, a);
        temp = t;
    }

    public static ColorTemperature fromTemperature(double temperature, Color baseColor) {
        double absTemp = temperature + HeatAPI.AMBIENT_TEMP;
        absTemp /= 100;

        if (cache.containsKey((int) absTemp)) {
            return cache.get((int) absTemp).blendOnto(baseColor);
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

        ColorTemperature colorTemperature = new ColorTemperature(red, green, blue, alpha, temperature);
        cache.put((int) absTemp, colorTemperature);
        return colorTemperature.blendOnto(baseColor);
    }

    public ColorTemperature blendOnto(Color baseColor) {
        double sR = (r & 0xFF) / 255D, sG = (g & 0xFF) / 255D, sB = (b & 0xFF) / 255D, sA = (a & 0xFF) / 255D;
        double dR = (baseColor.r & 0xFF) / 255D, dG = (baseColor.g & 0xFF) / 255D, dB = (baseColor.b & 0xFF) / 255D, dA = (baseColor.a & 0xFF) / 255D;

        double rR = sR * sA + dR * (1 - sA);
        double rG = sG * sA + dG * (1 - sA);
        double rB = sB * sA + dB * (1 - sA);
        double rA = dA * 1D + sA * (1 - dA);
        return new ColorTemperature(rR, rG, rB, rA, temp);
    }
}