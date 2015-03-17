package mekanism.common.recipe.inputs;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class FluidInput extends MachineInput<FluidInput>
{
	public FluidStack ingredient;

	public FluidInput(FluidStack stack)
	{
		ingredient = stack;
	}
	
	public FluidInput() {}
	
	@Override
	public void load(NBTTagCompound nbtTags)
	{
		ingredient = FluidStack.loadFluidStackFromNBT(nbtTags.getCompoundTag("input"));
	}

	@Override
	public FluidInput copy()
	{
		return new FluidInput(ingredient.copy());
	}

	@Override
	public boolean isValid()
	{
		return ingredient != null;
	}

	public boolean useFluid(FluidTank fluidTank, boolean deplete, int scale)
	{
		if(fluidTank.getFluid() != null && fluidTank.getFluid().containsFluid(ingredient))
		{
			fluidTank.drain(ingredient.amount*scale, deplete);
			return true;
		}
		
		return false;
	}

	@Override
	public int hashIngredients()
	{
		return ingredient.hashCode();
	}

	@Override
	public boolean testEquality(FluidInput other)
	{
		if(!isValid())
		{
			return !other.isValid();
		}
		return ingredient.equals(other.ingredient);
	}

	@Override
	public boolean isInstance(Object other)
	{
		return other instanceof FluidInput;
	}
}
