package mekanism.common.recipe.inputs;

import mekanism.api.util.StackUtils;

import net.minecraft.item.ItemStack;

public class ItemStackInput extends MachineInput
{
	public ItemStack ingredient;

	public ItemStackInput(ItemStack stack)
	{
		ingredient = stack;
	}

	@Override
	public int hashIngredients()
	{
		return StackUtils.hashItemStack(ingredient);
	}

	@Override
	public boolean testEquality(MachineInput other)
	{
		return other instanceof ItemStackInput && StackUtils.equalsWildcardWithNBT(ingredient, ((ItemStackInput)other).ingredient);
	}
}
