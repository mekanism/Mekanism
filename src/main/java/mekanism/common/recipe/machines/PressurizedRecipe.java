package mekanism.common.recipe.machines;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.recipe.inputs.PressurizedInput;
import mekanism.common.recipe.outputs.PressurizedOutput;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class PressurizedRecipe extends MachineRecipe<PressurizedInput, PressurizedOutput, PressurizedRecipe>
{
	public double extraEnergy;

	public int ticks;

	public PressurizedRecipe(ItemStack inputSolid, FluidStack inputFluid, GasStack inputGas, ItemStack outputSolid, GasStack outputGas, double energy, int duration)
	{
		this(new PressurizedInput(inputSolid, inputFluid, inputGas), new PressurizedOutput(outputSolid, outputGas), energy, duration);
	}

	public PressurizedRecipe(PressurizedInput pressurizedInput, PressurizedOutput pressurizedProducts, double energy, int duration)
	{
		super(pressurizedInput, pressurizedProducts);

		extraEnergy = energy;
		ticks = duration;
	}

	@Override
	public PressurizedRecipe copy()
	{
		return new PressurizedRecipe(getInput().copy(), getOutput().copy(), extraEnergy, ticks);
	}

	public boolean canOperate(ItemStack[] inventory, FluidTank inputFluidTank, GasTank inputGasTank, GasTank outputGasTank)
	{
		return getInput().use(inventory, 0, inputFluidTank, inputGasTank, false) &&	getOutput().applyOutputs(inventory, 2, outputGasTank, false);
	}

	public void operate(ItemStack[] inventory, FluidTank inputFluidTank, GasTank inputGasTank, GasTank outputGasTank)
	{
		if(getInput().use(inventory, 0, inputFluidTank, inputGasTank, true))
		{
			getOutput().applyOutputs(inventory, 2, outputGasTank, true);
		}
	}
}
