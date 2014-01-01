package mekanism.api;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * An input of one fluid and one gas for the Chemical Combiner.
 * @author unpairedbracket
 *
 */
public class ChemicalCombinerInput
{
	/** The gas of this chemical combiner input */
	public GasStack gas;

	/** The fluid of this chemical combiner input */
	public FluidStack fluid;

	/**
	 * Creates a chemical input with two defined gasses of the Chemical Infuser.
	 * @param gas - gas
	 * @param fluid - fluid
	 */
	public ChemicalCombinerInput(GasStack gas, FluidStack fluid)
	{
		this.gas = gas;
		this.fluid = fluid;
	}

	/**
	 * If this is a valid
	 * @return
	 */
	public boolean isValid()
	{
		return gas != null && fluid != null;
	}

	/**
	 * Whether or not the defined input contains the same chemicals and at least the required amount of the defined chemicals as this input.
	 * @param input - input to check
	 * @return if the input meets this input's requirements
	 */
	public boolean meetsInput(ChemicalCombinerInput input)
	{
		return meets(input);
	}

	/**
	 * Draws the needed amount of gas from each tank.
	 * @param gasTank - left tank to draw from
	 * @param fluidTank - right tank to draw from
	 */
	public void draw(GasTank gasTank, FluidTank fluidTank)
	{
		if(meets(new ChemicalCombinerInput(gasTank.getGas(), fluidTank.getFluid())))
		{
			gasTank.draw(gas.amount, true);
			fluidTank.drain(fluid.amount, true);
		}
	}

	/**
	 * Actual implementation of meetsInput(), performs the checks.
	 * @param input - input to check
	 * @return if the input meets this input's requirements
	 */
	private boolean meets(ChemicalCombinerInput input)
	{
		if(input == null || !input.isValid())
		{
			return false;
		}

		if(input.gas.getGas() != gas.getGas() || input.fluid.getFluid() != fluid.getFluid())
		{
			return false;
		}

		return input.gas.amount >= gas.amount && input.fluid.amount >= fluid.amount;
	}
}
