package mekanism.common.recipe.machines;

import mekanism.api.gas.GasStack;
import mekanism.common.recipe.outputs.PressurizedProducts;
import mekanism.common.recipe.inputs.PressurizedReactants;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class PressurizedRecipe extends MachineRecipe<PressurizedReactants, PressurizedProducts>
{
	public double extraEnergy;

	public int ticks;

	public PressurizedRecipe(ItemStack inputSolid, FluidStack inputFluid, GasStack inputGas, ItemStack outputSolid, GasStack outputGas, double energy, int duration)
	{
		this(new PressurizedReactants(inputSolid, inputFluid, inputGas), new PressurizedProducts(outputSolid, outputGas), energy, duration);
	}

	public PressurizedRecipe(PressurizedReactants pressurizedReactants, PressurizedProducts pressurizedProducts, double energy, int duration)
	{
		super(pressurizedReactants, pressurizedProducts);

		extraEnergy = energy;
		ticks = duration;
	}

	public PressurizedRecipe copy()
	{
		return new PressurizedRecipe(getInput().copy(), getOutput().copy(), extraEnergy, ticks);
	}
}
