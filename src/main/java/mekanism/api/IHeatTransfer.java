package mekanism.api;

import net.minecraft.util.EnumFacing;

public interface IHeatTransfer
{
	/**The value of the zero point of our temperature scale in kelvin*/
	public static final double AMBIENT_TEMP = 300;

	/**The heat transfer coefficient for air*/
	public static final double AIR_INVERSE_COEFFICIENT = 10000;

	public double getTemp();

	public double getInverseConductionCoefficient();

	public double getInsulationCoefficient(EnumFacing side);

	public void transferHeatTo(double heat);

	public double[] simulateHeat();

	public double applyTemperatureChange();

	public boolean canConnectHeat(EnumFacing side);

	public IHeatTransfer getAdjacent(EnumFacing side);
}
