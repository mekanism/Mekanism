package mekanism.common.recipe.inputs;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * An input of a gas, a fluid and an item for the pressurized reaction chamber
 */
public class PressurizedInput extends MachineInput<PressurizedInput>
{
	private ItemStack theSolid;
	private FluidStack theFluid;
	private GasStack theGas;

	public PressurizedInput(ItemStack solid, FluidStack fluid, GasStack gas)
	{
		theSolid = solid;
		theFluid = fluid;
		theGas = gas;
	}
	
	public PressurizedInput() {}
	
	@Override
	public void load(NBTTagCompound nbtTags)
	{
		theSolid = ItemStack.loadItemStackFromNBT(nbtTags.getCompoundTag("itemInput"));
		theFluid = FluidStack.loadFluidStackFromNBT(nbtTags.getCompoundTag("fluidInput"));
		theGas = GasStack.readFromNBT(nbtTags.getCompoundTag("gasInput"));
	}

	/**
	 * If this is a valid PressurizedReactants
	 */
	@Override
	public boolean isValid()
	{
		return theSolid != null && theFluid != null && theGas != null;
	}

	public boolean use(ItemStack[] inventory, int index, FluidTank fluidTank, GasTank gasTank, boolean deplete)
	{
		if(meets(new PressurizedInput(inventory[index], fluidTank.getFluid(), gasTank.getGas())))
		{
			if(deplete)
			{
				inventory[index] = StackUtils.subtract(inventory[index], theSolid);
				fluidTank.drain(theFluid.amount, true);
				gasTank.draw(theGas.amount, true);
			}
			return true;
		}
		return false;
	}

	/**
	 * Whether or not this PressurizedReactants's ItemStack entry's item type is equal to the item type of the given item.
	 * @param stack - stack to check
	 * @return if the stack's item type is contained in this PressurizedReactants
	 */
	public boolean containsType(ItemStack stack)
	{
		if(stack == null || stack.stackSize == 0)
		{
			return false;
		}

		return StackUtils.equalsWildcard(stack, theSolid);
	}

	/**
	 * Whether or not this PressurizedReactants's FluidStack entry's fluid type is equal to the fluid type of the given fluid.
	 * @param stack - stack to check
	 * @return if the stack's fluid type is contained in this PressurizedReactants
	 */
	public boolean containsType(FluidStack stack)
	{
		if(stack == null || stack.amount == 0)
		{
			return false;
		}

		return stack.isFluidEqual(theFluid);
	}

	/**
	 * Whether or not this PressurizedReactants's GasStack entry's gas type is equal to the gas type of the given gas.
	 * @param stack - stack to check
	 * @return if the stack's gas type is contained in this PressurizedReactants
	 */
	public boolean containsType(GasStack stack)
	{
		if(stack == null || stack.amount == 0)
		{
			return false;
		}

		return stack.isGasEqual(theGas);
	}

	/**
	 * Actual implementation of meetsInput(), performs the checks.
	 * @param input - input to check
	 * @return if the input meets this input's requirements
	 */
	public boolean meets(PressurizedInput input)
	{
		if(input == null || !input.isValid())
		{
			return false;
		}

		if(!(StackUtils.equalsWildcard(input.theSolid, theSolid) && input.theFluid.isFluidEqual(theFluid) && input.theGas.isGasEqual(theGas)))
		{
			return false;
		}

		return input.theSolid.stackSize >= theSolid.stackSize && input.theFluid.amount >= theFluid.amount && input.theGas.amount >= theGas.amount;
	}

	@Override
	public PressurizedInput copy()
	{
		return new PressurizedInput(theSolid.copy(), theFluid.copy(), theGas.copy());
	}
	
	public ItemStack getSolid()
	{
		return theSolid;
	}
	
	public FluidStack getFluid()
	{
		return theFluid;
	}
	
	public GasStack getGas()
	{
		return theGas;
	}

	@Override
	public int hashIngredients()
	{
		return StackUtils.hashItemStack(theSolid) << 16 | theFluid.hashCode() << 8 | theGas.hashCode();
	}

	@Override
	public boolean testEquality(PressurizedInput other)
	{
		return other.containsType(theSolid) && other.containsType(theFluid) && other.containsType(theGas);
	}

	@Override
	public boolean isInstance(Object other)
	{
		return other instanceof PressurizedInput;
	}
}
