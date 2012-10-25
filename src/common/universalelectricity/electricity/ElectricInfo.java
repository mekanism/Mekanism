package universalelectricity.electricity;

/**
 * An easy way to display information on electricity.
 * 
 * @author Calclavia
 */

public class ElectricInfo
{
	public static enum ElectricUnit
	{
		AMPERE("Amp", "I"), AMP_HOUR("Amp Hour", "Ah"), VOLTAGE("Volt", "V"), WATT("Watt", "W"), WATT_HOUR("Watt Hour", "Wh"), RESISTANCE("Ohm", "R"), CONDUCTANCE("Siemen", "S"), JOULES("Joule", "J");

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

	public static enum MeasurementUnit
	{
		MICRO("Micro", "mi", 0.000001), MILLI("Milli", "m", 0.001), KILO("Kilo", "k", 1000), MEGA("Mega", "M", 1000000);

		public String name;
		public String symbol;
		public double process;

		private MeasurementUnit(String name, String symbol, double process)
		{
			this.name = name;
			this.symbol = symbol;
			this.process = process;
		}

		public String getName(boolean isSymbol)
		{
			if (isSymbol)
			{
				return symbol;
			}
			else
			{
				return name;
			}
		}

		public double process(double value)
		{
			return value / this.process;
		}
	}

	public static double getJoules(double watts, double seconds)
	{
		return watts * seconds;
	}

	public static double getJoules(double amps, double voltage, double seconds)
	{
		return amps * voltage * seconds;
	}

	public static double getWattsFromJoules(double joules, double seconds)
	{
		return joules / seconds;
	}

	public static double getAmps(double watts, double voltage)
	{
		return watts / voltage;
	}

	public static double getAmps(double ampHours)
	{
		return ampHours * 3600;
	}

	public static double getAmpsFromWattHours(double wattHours, double voltage)
	{
		return getWatts(wattHours) / voltage;
	}

	public static double getWattHoursFromAmpHours(double ampHours, double voltage)
	{
		return ampHours * voltage;
	}

	public static double getAmpHours(double amps)
	{
		return amps / 3600;
	}

	public static double getWatts(double amps, double voltage)
	{
		return amps * voltage;
	}

	public static double getWatts(double wattHours)
	{
		return wattHours * 3600;
	}

	public static double getWattHours(double watts)
	{
		return watts / 3600;
	}

	public static double getWattHours(double amps, double voltage)
	{
		return getWattHours(getWatts(amps, voltage));
	}

	public static double getResistance(double amps, double voltage)
	{
		return voltage / amps;
	}

	public static double getConductance(double amps, double voltage)
	{
		return amps / voltage;
	}

	/**
	 * Displays the unit as text. Works only for positive numbers.
	 */
	public static String getDisplay(double value, ElectricUnit unit, int significantFigures, boolean isShort)
	{
		String unitName = unit.name;

		if (isShort)
		{
			unitName = unit.symbol;
		}
		else if (value > 1)
		{
			unitName = unit.getPlural();
		}

		if (value == 0) { return value + " " + unitName; }

		if (value <= MeasurementUnit.MILLI.process) { return roundDecimals(MeasurementUnit.MICRO.process(value), significantFigures) + " " + MeasurementUnit.MICRO.getName(isShort) + unitName; }

		if (value < 1) { return roundDecimals(MeasurementUnit.MILLI.process(value), significantFigures) + " " + MeasurementUnit.MILLI.getName(isShort) + unitName; }

		if (value > MeasurementUnit.KILO.process) { return roundDecimals(MeasurementUnit.KILO.process(value), significantFigures) + " " + MeasurementUnit.KILO.getName(isShort) + unitName; }

		if (value > MeasurementUnit.MEGA.process) { return roundDecimals(MeasurementUnit.MEGA.process(value), significantFigures) + " " + MeasurementUnit.MEGA.getName(isShort) + unitName; }

		return roundDecimals(value, significantFigures) + " " + unitName;
	}

	public static String getDisplayShort(double value, ElectricUnit unit)
	{
		return getDisplay(value, unit, 2, true);
	}

	public static String getDisplay(double value, ElectricUnit unit)
	{
		return getDisplay(value, unit, 2, false);
	}

	public static String getDisplaySimple(double value, ElectricUnit unit, int significantFigures)
	{
		if (value > 1)
		{
			if (significantFigures < 1) { return (int) value + " " + unit.getPlural(); }

			return roundDecimals(value, significantFigures) + " " + unit.getPlural();
		}

		if (significantFigures < 1) { return (int) value + " " + unit.name; }

		return roundDecimals(value, significantFigures) + " " + unit.name;
	}

	/**
	 * Rounds a number to a specific number place places
	 * 
	 * @param The
	 *            number
	 * @return The rounded number
	 */
	public static double roundDecimals(double d, int significantFigures)
	{
		int j = (int) (d * Math.pow(10, significantFigures));
		return j / (double) Math.pow(10, significantFigures);
	}

	public static double roundDecimals(double d)
	{
		return roundDecimals(d, 2);
	}
}
