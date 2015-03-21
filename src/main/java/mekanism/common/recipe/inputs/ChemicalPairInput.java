package mekanism.common.recipe.inputs;

import net.minecraft.nbt.NBTTagCompound;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;

/**
 * An input of gasses for recipe use.
 * @author aidancbrady
 *
 */
public class ChemicalPairInput extends MachineInput<ChemicalPairInput>
{
	/** The left gas of this chemical input */
	public GasStack leftGas;

	/** The right gas of this chemical input */
	public GasStack rightGas;

	/**
	 * Creates a chemical input with two defined gasses.
	 * @param left - left gas
	 * @param right - right gas
	 */
	public ChemicalPairInput(GasStack left, GasStack right)
	{
		leftGas = left;
		rightGas = right;
	}
	
	@Override
	public void load(NBTTagCompound nbtTags)
	{
		leftGas = GasStack.readFromNBT(nbtTags.getCompoundTag("leftInput"));
		rightGas = GasStack.readFromNBT(nbtTags.getCompoundTag("rightInput"));
	}
	
	public ChemicalPairInput() {}

	public boolean useGas(GasTank leftTank, GasTank rightTank, boolean deplete, int scale)
	{
		int leftAmount = leftGas.amount * scale;
		int rightAmount = rightGas.amount * scale;
		
		if(leftTank.canDraw(leftGas.getGas()) && rightTank.canDraw(rightGas.getGas()))
		{
			if(leftTank.getStored() >= leftAmount && rightTank.getStored() >= rightAmount)
			{
				leftTank.draw(leftAmount, deplete);
				rightTank.draw(rightAmount, deplete);
				
				return true;
			}
		} 
		else if(leftTank.canDraw(rightGas.getGas()) && rightTank.canDraw(leftGas.getGas()))
		{
			if(leftTank.getStored() >= rightAmount && rightTank.getStored() >= leftAmount)
			{
				leftTank.draw(rightAmount, deplete);
				rightTank.draw(leftAmount, deplete);
				
				return true;
			}
		}
		
		return false;
	}

	/**
	 * If this is a valid ChemicalPair
	 * @return
	 */
	@Override
	public boolean isValid()
	{
		return leftGas != null && rightGas != null;
	}

	/**
	 * Whether or not the defined input contains the same gasses and at least the required amount of the defined gasses as this input.
	 * @param input - input to check
	 * @return if the input meets this input's requirements
	 */
	public boolean meetsInput(ChemicalPairInput input)
	{
		return meets(input) || meets(input.swap());
	}

	/**
	 * Swaps the right gas and left gas of this input.
	 * @return a swapped ChemicalInput
	 */
	public ChemicalPairInput swap()
	{
		return new ChemicalPairInput(rightGas, leftGas);
	}

	/**
	 * Draws the needed amount of gas from each tank.
	 * @param leftTank - left tank to draw from
	 * @param rightTank - right tank to draw from
	 */
	public void draw(GasTank leftTank, GasTank rightTank)
	{
		if(meets(new ChemicalPairInput(leftTank.getGas(), rightTank.getGas())))
		{
			leftTank.draw(leftGas.amount, true);
			rightTank.draw(rightGas.amount, true);
		}
		else if(meets(new ChemicalPairInput(rightTank.getGas(), leftTank.getGas())))
		{
			leftTank.draw(rightGas.amount, true);
			rightTank.draw(leftGas.amount, true);
		}
	}

	/**
	 * Whether or not one of this ChemicalInput's GasStack entry's gas type is equal to the gas type of the given gas.
	 * @param stack - stack to check
	 * @return if the stack's gas type is contained in this ChemicalInput
	 */
	public boolean containsType(GasStack stack)
	{
		if(stack == null || stack.amount == 0)
		{
			return false;
		}

		return stack.isGasEqual(leftGas) || stack.isGasEqual(rightGas);
	}

	/**
	 * Actual implementation of meetsInput(), performs the checks.
	 * @param input - input to check
	 * @return if the input meets this input's requirements
	 */
	private boolean meets(ChemicalPairInput input)
	{
		if(input == null || !input.isValid())
		{
			return false;
		}

		if(input.leftGas.getGas() != leftGas.getGas() || input.rightGas.getGas() != rightGas.getGas())
		{
			return false;
		}

		return input.leftGas.amount >= leftGas.amount && input.rightGas.amount >= rightGas.amount;
	}

	@Override
	public ChemicalPairInput copy()
	{
		return new ChemicalPairInput(leftGas.copy(), rightGas.copy());
	}

	@Override
	public int hashIngredients()
	{
		return (leftGas.hashCode() << 8 | rightGas.hashCode()) + (rightGas.hashCode() << 8 | leftGas.hashCode());
	}

	@Override
	public boolean testEquality(ChemicalPairInput other)
	{
		if(!isValid())
		{
			return !other.isValid();
		}
		return (other.leftGas.hashCode() == leftGas.hashCode() && other.rightGas.hashCode() == rightGas.hashCode())
			|| (other.leftGas.hashCode() == rightGas.hashCode() && other.rightGas.hashCode() == leftGas.hashCode());
	}

	@Override
	public boolean isInstance(Object other)
	{
		return other instanceof ChemicalPairInput;
	}
}
