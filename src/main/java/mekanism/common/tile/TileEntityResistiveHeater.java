package mekanism.common.tile;

import mekanism.api.IHeatTransfer;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.util.HeatUtils;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityResistiveHeater extends TileEntityElectricBlock implements IHeatTransfer
{
	public TileEntityResistiveHeater()
	{
		super("ResistiveHeater", MachineType.RESISTIVE_HEATER.baseEnergy);
	}

	@Override
	public double getTemp() 
	{
		return 0;
	}

	@Override
	public double getInverseConductionCoefficient() 
	{
		return 1;
	}

	@Override
	public double getInsulationCoefficient(ForgeDirection side) 
	{
		return 0;
	}

	@Override
	public void transferHeatTo(double heat) 
	{
		
	}

	@Override
	public double[] simulateHeat() 
	{
		return HeatUtils.simulate(this);
	}

	@Override
	public double applyTemperatureChange() 
	{
		return 0;
	}

	@Override
	public boolean canConnectHeat(ForgeDirection side) 
	{
		return false;
	}

	@Override
	public IHeatTransfer getAdjacent(ForgeDirection side) 
	{
		return null;
	}
}
