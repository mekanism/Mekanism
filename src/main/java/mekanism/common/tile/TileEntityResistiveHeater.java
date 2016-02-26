package mekanism.common.tile;

import mekanism.api.IHeatTransfer;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.util.HeatUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityResistiveHeater extends TileEntityElectricBlock implements IHeatTransfer
{
	public double temperature;
	public double heatToAbsorb = 0;
	
	public TileEntityResistiveHeater()
	{
		super("ResistiveHeater", MachineType.RESISTIVE_HEATER.baseEnergy);
	}
	
	@Override
	public void onUpdate()
	{
		simulateHeat();
		applyTemperatureChange();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		temperature = nbtTags.getDouble("temperature");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setDouble("temperature", temperature);
	}

	@Override
	public double getTemp() 
	{
		return temperature;
	}

	@Override
	public double getInverseConductionCoefficient() 
	{
		return 1;
	}

	@Override
	public double getInsulationCoefficient(ForgeDirection side) 
	{
		return 1000;
	}

	@Override
	public void transferHeatTo(double heat)
	{
		heatToAbsorb += heat;
	}

	@Override
	public double[] simulateHeat() 
	{
		return HeatUtils.simulate(this);
	}

	@Override
	public double applyTemperatureChange() 
	{
		temperature += heatToAbsorb;
		heatToAbsorb = 0;
		
		return temperature;
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
