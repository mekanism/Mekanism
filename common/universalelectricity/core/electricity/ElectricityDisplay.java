package universalelectricity.core.electricity;

/**
 * An easy way to display information on electricity for the client.
 * 
 * @author Calclavia
 */
public class ElectricityDisplay
{
	/**
	 * Universal Electricity's units are in KILOJOULES, KILOWATTS and KILOVOLTS. Try to make your
	 * energy ratio as close to real life as possible.
	 */
	public static enum ElectricUnit
	{
		AMPERE("Amp", "I"), AMP_HOUR("Amp Hour", "Ah"), VOLTAGE("Volt", "V"), WATT("Watt", "W"),
		WATT_HOUR("Watt Hour", "Wh"), RESISTANCE("Ohm", "R"), CONDUCTANCE("Siemen", "S"),
		JOULES("Joule", "J");

		public String name;
		public String symbol;

		private ElectricUnit(String name, String symbol)
		{
			this.name = name;
			this.symbol = symbol;
		}

		public String getPlural()
		{
			return this.name + "s";
		}
	}

	/** Metric system of measurement. */
	public static enum MeasurementUnit
	{
		MICRO("Micro", "u", 0.000001f), MILLI("Milli", "m", 0.001f), BASE("", "", 1),
		KILO("Kilo", "k", 1000f), MEGA("Mega", "M", 1000000f), GIGA("Giga", "G", 1000000000f),
		TERA("Tera", "T", 1000000000000f), PETA("Peta", "P", 1000000000000000f),
		EXA("Exa", "E", 1000000000000000000f), ZETTA("Zetta", "Z", 1000000000000000000000f),
		YOTTA("Yotta", "Y", 1000000000000000000000000f);

		/** long name for the unit */
		public String name;
		/** short unit version of the unit */
		public String symbol;
		/** Point by which a number is consider to be of this unit */
		public float value;

		private MeasurementUnit(String name, String symbol, float value)
		{
			this.name = name;
			this.symbol = symbol;
			this.value = value;
		}

		public String getName(boolean getShort)
		{
			if (getShort)
			{
				return symbol;
			}
			else
			{
				return name;
			}
		}

		/** Divides the value by the unit value start */
		public double process(double value)
		{
			return value / this.value;
		}

		/** Checks if a value is above the unit value start */
		public boolean isAbove(float value)
		{
			return value > this.value;
		}

		/** Checks if a value is lower than the unit value start */
		public boolean isBellow(float value)
		{
			return value < this.value;
		}
	}

	/** By default, mods should store energy in Kilo-Joules, hence a multiplier of 1/1000. */
	public static String getDisplay(float value, ElectricUnit unit, int decimalPlaces, boolean isShort)
	{
		return getDisplay(value, unit, decimalPlaces, isShort, 1000);
	}

	/**
	 * Displays the unit as text. Does handle negative numbers, and will place a negative sign in
	 * front of the output string showing this. Use string.replace to remove the negative sign if
	 * unwanted
	 */
	public static String getDisplay(float value, ElectricUnit unit, int decimalPlaces, boolean isShort, float multiplier)
	{
		String unitName = unit.name;
		String prefix = "";
		if (value < 0)
		{
			value = Math.abs(value);
			prefix = "-";
		}
		value *= multiplier;

		if (isShort)
		{
			unitName = unit.symbol;
		}
		else if (value > 1)
		{
			unitName = unit.getPlural();
		}

		if (value == 0)
		{
			return value + " " + unitName;
		}
		else
		{
			for (int i = 0; i < MeasurementUnit.values().length; i++)
			{
				MeasurementUnit lowerMeasure = MeasurementUnit.values()[i];
				if (lowerMeasure.isBellow(value) && lowerMeasure.ordinal() == 0)
				{
					return prefix + roundDecimals(lowerMeasure.process(value), decimalPlaces) + " " + lowerMeasure.getName(isShort) + unitName;
				}
				if (lowerMeasure.ordinal() + 1 >= MeasurementUnit.values().length)
				{
					return prefix + roundDecimals(lowerMeasure.process(value), decimalPlaces) + " " + lowerMeasure.getName(isShort) + unitName;
				}
				MeasurementUnit upperMeasure = MeasurementUnit.values()[i + 1];
				if ((lowerMeasure.isAbove(value) && upperMeasure.isBellow(value)) || lowerMeasure.value == value)
				{
					return prefix + roundDecimals(lowerMeasure.process(value), decimalPlaces) + " " + lowerMeasure.getName(isShort) + unitName;
				}
			}
		}

		return prefix + roundDecimals(value, decimalPlaces) + " " + unitName;
	}

	public static String getDisplay(float value, ElectricUnit unit)
	{
		return getDisplay(value, unit, 2, false);
	}

	public static String getDisplayShort(float value, ElectricUnit unit)
	{
		return getDisplay(value, unit, 2, true);
	}

	public static String getDisplayShort(float value, ElectricUnit unit, int decimalPlaces)
	{
		return getDisplay(value, unit, decimalPlaces, true);
	}

	public static String getDisplaySimple(float value, ElectricUnit unit, int decimalPlaces)
	{
		if (value > 1)
		{
			if (decimalPlaces < 1)
			{
				return (int) value + " " + unit.getPlural();
			}

			return roundDecimals(value, decimalPlaces) + " " + unit.getPlural();
		}

		if (decimalPlaces < 1)
		{
			return (int) value + " " + unit.name;
		}

		return roundDecimals(value, decimalPlaces) + " " + unit.name;
	}

	/**
	 * Rounds a number to a specific number place places
	 * 
	 * @param The number
	 * @return The rounded number
	 */
	public static double roundDecimals(double d, int decimalPlaces)
	{
		int j = (int) (d * Math.pow(10, decimalPlaces));
		return j / Math.pow(10, decimalPlaces);
	}

	public static double roundDecimals(double d)
	{
		return roundDecimals(d, 2);
	}
}
