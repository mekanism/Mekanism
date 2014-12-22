package mekanism.client.render;

import java.util.HashMap;

import mekanism.api.IHeatTransfer;

import codechicken.lib.colour.ColourRGBA;

public class ColourTemperature extends ColourRGBA
{
	public static HashMap<Integer, ColourTemperature> cache = new HashMap<Integer, ColourTemperature>();

	public double temp;

	public ColourTemperature(double r, double g, double b, double a, double t)
	{
		super(r, g, b, a);
		temp = t;
	}

	public static ColourTemperature fromTemperature(double temperature, ColourRGBA baseColour)
	{
		if(temperature < 0)
		{
			double alphaBlend = -temperature/IHeatTransfer.AMBIENT_TEMP;
			if(alphaBlend < 0)
				alphaBlend = 0;
			if(alphaBlend > 1)
				alphaBlend = 1;
			return new ColourTemperature(1, 1, 1, alphaBlend, temperature).blendOnto(baseColour);
		}
		double absTemp = temperature + IHeatTransfer.AMBIENT_TEMP;
		absTemp /= 100;

		if(cache.containsKey((int)absTemp))
		{
			return cache.get((int)absTemp).blendOnto(baseColour);
		}

		double tmpCalc;
		double red, green, blue, alpha;
		double effectiveTemp = absTemp;

		if(effectiveTemp < 10)
			effectiveTemp = 10;
		if(effectiveTemp > 400)
			effectiveTemp = 400;

		if(effectiveTemp <= 66)
		{
			red = 1;
		}
		else
		{
			tmpCalc = effectiveTemp - 60;
			tmpCalc = 329.698727446 * Math.pow(tmpCalc,-0.1332047592);
			red = tmpCalc/255D;
		}

		if(effectiveTemp <= 66)
		{
			tmpCalc = effectiveTemp;
			tmpCalc = 99.4708025861 * Math.log(tmpCalc) - 161.1195681661;
			green = tmpCalc/255D;
		}
		else
		{
			tmpCalc = effectiveTemp - 60;
			tmpCalc = 288.1221695283 * Math.pow(tmpCalc, -0.0755148492);
			green = tmpCalc/255D;
		}

		if(effectiveTemp >= 66)
		{
			blue = 1;
		}
		else if(effectiveTemp <= 19)
		{
			blue = 0;
		}
		else
		{
			tmpCalc = effectiveTemp - 10;
			tmpCalc = 138.5177312231 * Math.log(tmpCalc) - 305.0447927307;

			blue = tmpCalc / 255D;
		}

		alpha = temperature/1000;

		if(red < 0) red = 0;
		if(red > 1) red = 1;

		if(green < 0) green = 0;
		if(green > 1) green = 1;

		if(blue < 0) blue = 0;
		if(blue > 1) blue = 1;

		if(alpha < 0) alpha = 0;
		if(alpha > 1) alpha = 1;

		ColourTemperature colourTemperature = new ColourTemperature(red, green, blue, alpha, temperature);

		cache.put((int)(absTemp), colourTemperature);

		return colourTemperature.blendOnto(baseColour);
	}

	public ColourTemperature blendOnto(ColourRGBA baseColour)
	{
		double sR = (r & 0xFF)/255D, sG = (g & 0xFF)/255D, sB = (b & 0xFF)/255D, sA = (a & 0xFF)/255D;
		double dR = (baseColour.r & 0xFF)/255D, dG = (baseColour.g & 0xFF)/255D, dB = (baseColour.b & 0xFF)/255D, dA = (baseColour.a & 0xFF)/255D;

		double rR = sR * sA + dR * (1-sA);
		double rG = sG * sA + dG * (1-sA);
		double rB = sB * sA + dB * (1-sA);
		double rA = dA * 1D + sA * (1-dA);

		return new ColourTemperature(rR, rG, rB, rA, temp);
	}
}
