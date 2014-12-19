package mekanism.api;

import net.minecraftforge.common.util.ForgeDirection;

public interface IHeatTransfer
{
	public double getTemp();

	public double getInverseConductionCoefficient();

	public void transferHeatTo(double heat);

	public double[] simulateHeat();

	public double applyTemperatureChange();

	public boolean isInsulated(ForgeDirection side);
}
