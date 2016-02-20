package mekanism.common.recipe.inputs;

import mekanism.api.util.StackUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class MachineInput<INPUT extends MachineInput<INPUT>>
{
	public abstract boolean isValid();

	public abstract INPUT copy();

	public abstract int hashIngredients();
	
	public abstract void load(NBTTagCompound nbtTags);

	/**
	 * Test equality to another input.
	 * This should return true if the input matches this one,
	 * IGNORING AMOUNTS.
	 * Allows usage of HashMap optimisation to get recipes.
	 * @param other
	 * @return
	 */
	public abstract boolean testEquality(INPUT other);
	
	public static boolean inputContains(ItemStack container, ItemStack contained)
	{
		if(container.stackSize >= contained.stackSize)
		{
			if(MekanismUtils.getOreDictName(container).contains("treeSapling"))
			{
				return StackUtils.equalsWildcard(contained, container);
			}
			
			return StackUtils.equalsWildcardWithNBT(contained, container) && container.stackSize >= contained.stackSize;
		}
		
		return false;
	}

	@Override
	public int hashCode()
	{
		return hashIngredients();
	}

	@Override
	public boolean equals(Object other)
	{
		if(isInstance(other))
		{
			return testEquality((INPUT)other);
		}
		
		return false;
	}

	public abstract boolean isInstance(Object other);
}
