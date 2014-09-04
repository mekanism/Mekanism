package mekanism.common.recipe.inputs;

import mekanism.api.util.StackUtils;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ItemStackInput extends MachineInput
{
	public ItemStack ingredient;

	public ItemStackInput(ItemStack stack)
	{
		ingredient = stack;
	}

	public ItemStackInput wildCopy()
	{
		return new ItemStackInput(new ItemStack(ingredient.getItem(), ingredient.stackSize, OreDictionary.WILDCARD_VALUE));
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
