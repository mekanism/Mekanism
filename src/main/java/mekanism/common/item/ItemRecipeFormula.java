package mekanism.common.item;

import net.minecraft.item.ItemStack;

public class ItemRecipeFormula extends ItemMekanism
{
	public ItemRecipeFormula()
	{
		super();
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack)
	{
		return 1;
	}
}
