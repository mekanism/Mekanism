package mekanism.api;

import net.minecraft.item.ItemStack;

public class Infusion 
{
	public InfusionType type;
	public ItemStack resource;
	
	public Infusion(InfusionType infusiontype, ItemStack itemstack)
	{
		type = infusiontype;
		resource = itemstack;
	}
	
	public static Infusion getInfusion(InfusionType type, ItemStack itemstack)
	{
		return new Infusion(type, itemstack);
	}
}
