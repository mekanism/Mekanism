package mekanism.common.recipe.inputs;

import net.minecraft.nbt.NBTTagCompound;

public class IntegerInput extends MachineInput<IntegerInput>
{
	public int ingredient;

	public IntegerInput(int integer)
	{
		ingredient = integer;
	}
	
	public IntegerInput() {}
	
	@Override
	public void load(NBTTagCompound nbtTags)
	{
		ingredient = nbtTags.getInteger("input");
	}

	@Override
	public IntegerInput copy()
	{
		return new IntegerInput(ingredient);
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public int hashIngredients()
	{
		return ingredient;
	}

	@Override
	public boolean testEquality(IntegerInput other)
	{
		return ingredient == other.ingredient;
	}

	@Override
	public boolean isInstance(Object other)
	{
		return other instanceof IntegerInput;
	}
}
