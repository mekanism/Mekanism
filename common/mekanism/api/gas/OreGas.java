package mekanism.api.gas;

import net.minecraft.item.ItemStack;

public class OreGas extends Gas
{
	public ItemStack oreStack;
	
	public OreGas(String s, ItemStack stack)
	{
		super(s);
		
		oreStack = stack;
	}
	
	public String getOreName()
	{
		return oreStack.getDisplayName();
	}
}
