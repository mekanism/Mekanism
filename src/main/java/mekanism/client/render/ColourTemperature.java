package mekanism.client.render;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mekanism.api.IHeatTransfer;
import mekanism.common.ColourRGBA;

public class ColourTemperature extends ColourRGBA {

    public static Int2ObjectMap<ColourTemperature> cache = new Int2ObjectOpenHashMap<>();

    public double temp;

    public ColourTemperature(double r, double g, double b, double a, double t) {
        super(r, g, b, a);
        temp = t;
    }

    public static ColourTemperature fromTemperature(double temperature, ColourRGBA baseColour) {
        if (temperature < 0) {
            double alphaBlend = -temperature / IHeatTransfer.AMBIENT_TEMP;
            if (alphaBlend < 0) {
                alphaBlend = 0;
            }
            if (alphaBlend > 1) {
                alphaBlend = 1;
            }
            return new ColourTemperature(1, 1, 1, alphaBlend, temperature).blendOnto(baseColour);
        }

        double absTemp = temperature + IHeatTransfer.AMBIENT_TEMP;
        absTemp /= 100;

        if (cache.containsKey((int) absTemp)) {
            return cache.get((int) absTemp).blendOnto(baseColour);
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

        alpha = temperature / 1000;

        //clamp to 0 <= n >= 1
        red = Math.min(Math.max(red, 0), 1);
        green = Math.min(Math.max(green, 0), 1);
        blue = Math.min(Math.max(blue, 0), 1);
        alpha = Math.min(Math.max(alpha, 0), 1);

        ColourTemperature colourTemperature = new ColourTemperature(red, green, blue, alpha, temperature);
        cache.put((int) absTemp, colourTemperature);
        return colourTemperature.blendOnto(baseColour);
    }

    public ColourTemperature blendOnto(ColourRGBA baseColour) {
        double sR = (valR & 0xFF) / 255D, sG = (valG & 0xFF) / 255D, sB = (valB & 0xFF) / 255D, sA = (valA & 0xFF) / 255D;
        double dR = (baseColour.valR & 0xFF) / 255D, dG = (baseColour.valG & 0xFF) / 255D, dB = (baseColour.valB & 0xFF) / 255D, dA = (baseColour.valA & 0xFF) / 255D;

        double rR = sR * sA + dR * (1 - sA);
        double rG = sG * sA + dG * (1 - sA);
        double rB = sB * sA + dB * (1 - sA);
        double rA = dA * 1D + sA * (1 - dA);
        return new ColourTemperature(rR, rG, rB, rA, temp);
    }
}