package mekanism.common.recipe.machines;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.inputs.GasInput;
import mekanism.common.recipe.outputs.GasOutput;
import mekanism.common.tile.TileEntityChemicalWasher;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class WasherRecipe extends MachineRecipe<GasInput, GasOutput, WasherRecipe>
{
	public FluidInput waterInput = new FluidInput(new FluidStack(FluidRegistry.WATER, TileEntityChemicalWasher.WATER_USAGE));

	public WasherRecipe(GasInput input, GasOutput output)
	{
		super(input, output);
	}

	public WasherRecipe(GasStack input, GasStack output)
	{
		this(new GasInput(input), new GasOutput(output));
	}

	@Override
	public WasherRecipe copy()
	{
		return new WasherRecipe(getInput().copy(), getOutput().copy());
	}

	public boolean canOperate(GasTank inputTank, FluidTank fluidTank, GasTank outputTank)
	{
		return getInput().useGas(inputTank, false, 1) && waterInput.useFluid(fluidTank, false, 1) && getOutput().applyOutputs(outputTank, false, 1);
	}

	public void operate(GasTank inputTank, FluidTank fluidTank, GasTank outputTank, int scale)
	{
		if(getInput().useGas(inputTank, true, scale) && waterInput.useFluid(fluidTank, true, scale))
		{
			getOutput().applyOutputs(outputTank, true, scale);
		}
	}
}
