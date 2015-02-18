package mekanism.common.recipe.machines;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.outputs.ChemicalPairOutput;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class SeparatorRecipe extends MachineRecipe<FluidInput, ChemicalPairOutput, SeparatorRecipe>
{
	public double extraEnergy;

	public SeparatorRecipe(FluidInput input, double energy, ChemicalPairOutput output)
	{
		super(input, output);
		extraEnergy = energy;
	}

	public SeparatorRecipe(FluidStack input, double energy, GasStack left, GasStack right)
	{
		this(new FluidInput(input), energy, new ChemicalPairOutput(left, right));
	}

	@Override
	public SeparatorRecipe copy()
	{
		return new SeparatorRecipe(getInput().copy(), extraEnergy, getOutput().copy());
	}

	public boolean canOperate(FluidTank fluidTank, GasTank leftTank, GasTank rightTank)
	{
		return getInput().useFluid(fluidTank, false, 1) && getOutput().applyOutputs(leftTank, rightTank, false);
	}

	public void operate(FluidTank fluidTank, GasTank leftTank, GasTank rightTank)
	{
		if(getInput().useFluid(fluidTank, true, 1))
		{
			getOutput().applyOutputs(leftTank, rightTank, true);
		}
	}
}
