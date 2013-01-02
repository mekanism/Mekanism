package mekanism.api;

import net.minecraft.item.ItemStack;

public class InfusionOutput 
{
	public InfusionInput infusionInput;
	public ItemStack resource;
	
	public InfusionOutput(InfusionInput input, ItemStack itemstack)
	{
		infusionInput = input;
		resource = itemstack;
	}
	
	public static InfusionOutput getInfusion(InfusionInput input, ItemStack itemstack)
	{
		return new InfusionOutput(input, itemstack);
	}
	
	public int getInfuseRequired()
	{
		return infusionInput.infuseStored;
	}
	
	public InfusionOutput copy()
	{
		return new InfusionOutput(infusionInput, resource);
	}
}
