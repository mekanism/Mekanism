package mekanism.common.recipe.machines;

import mekanism.api.gas.GasStack;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.outputs.ChemicalPairOutput;

import net.minecraftforge.fluids.FluidStack;

public class SeparatorRecipe extends MachineRecipe<FluidInput, ChemicalPairOutput>
{
	public SeparatorRecipe(FluidStack input, GasStack left, GasStack right)
	{
		super(new FluidInput(input), new ChemicalPairOutput(left, right));
	}
}
