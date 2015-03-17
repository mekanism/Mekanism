package mekanism.common.recipe.outputs;

import net.minecraft.nbt.NBTTagCompound;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;

public class GasOutput extends MachineOutput<GasOutput>
{
	public GasStack output;

	public GasOutput(GasStack stack)
	{
		output = stack;
	}
	
	public GasOutput() {}
	
	@Override
	public void load(NBTTagCompound nbtTags)
	{
		output = GasStack.readFromNBT(nbtTags.getCompoundTag("output"));
	}

	@Override
	public GasOutput copy()
	{
		return new GasOutput(output.copy());
	}

	public boolean applyOutputs(GasTank gasTank, boolean doEmit, int scale)
	{
		if(gasTank.canReceive(output.getGas()) && gasTank.getNeeded() >= output.amount*scale)
		{
			gasTank.receive(output.copy().withAmount(output.amount*scale), doEmit);
			
			return true;
		}
		
		return false;
	}
}
