package mekanism.client.render;

import java.util.HashMap;

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

	public static ColourTemperature fromTemperature(double temperature)
	{
		temperature += 300;
		temperature = temperature / 100;

		if(cache.containsKey((int)temperature))
		{
			return cache.get((int)temperature);
		}

		double tmpCalc;
		double red, green, blue, alpha;

		if(temperature < 10)
			temperature = 10;
		if(temperature > 400)
			temperature = 400;

		if(temperature <= 66)
		{
			red = 1;
		}
		else
		{
			tmpCalc = temperature - 60;
			tmpCalc = 329.698727446 * Math.pow(tmpCalc,-0.1332047592);
			red = tmpCalc/255D;
		}

		if(temperature <= 66)
		{
			tmpCalc = temperature;
			tmpCalc = 99.4708025861 * Math.log(tmpCalc) - 161.1195681661;
			green = tmpCalc/255D;
		}
		else
		{
			tmpCalc = temperature - 60;
			tmpCalc = 288.1221695283 * Math.pow(tmpCalc, -0.0755148492);
			green = tmpCalc/255D;
		}

		if(temperature >= 66)
		{
			blue = 1;
		}
		else if(temperature <= 19)
		{
			blue = 0;
		}
		else
		{
			tmpCalc = temperature - 10;
			tmpCalc = 138.5177312231 * Math.log(tmpCalc) - 305.0447927307;

			blue = tmpCalc / 255D;
		}

		alpha = (temperature - 3)/10;

		if(red < 0) red = 0;
		if(red > 1) red = 1;

		if(green < 0) green = 0;
		if(green > 1) green = 1;

		if(blue < 0) blue = 0;
		if(blue > 1) blue = 1;

		if(alpha < 0) alpha = 0;
		if(alpha > 1) alpha = 1;

		ColourTemperature colourTemperature = new ColourTemperature(red, green, blue, alpha, temperature*100-300);

		cache.put((int)(temperature), colourTemperature);

		return colourTemperature;
	}
}
