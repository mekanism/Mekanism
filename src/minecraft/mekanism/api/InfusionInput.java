package mekanism.api;

import net.minecraft.item.ItemStack;

public class InfusionInput
{
	public InfusionType infusionType;
	public int infuseStored;
	public ItemStack inputSlot;
	
	public InfusionInput(InfusionType infusiontype, int required, ItemStack itemstack)
	{
		infusionType = infusiontype;
		infuseStored = required;
		inputSlot = itemstack;
	}
	
	public static InfusionInput getInfusion(InfusionType type, int stored, ItemStack itemstack)
	{
		return new InfusionInput(type, stored, itemstack);
	}
}
