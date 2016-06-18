package mekanism.common.security;

import net.minecraft.item.ItemStack;

public interface IOwnerItem
{
	public String getOwner(ItemStack stack);
	
	public void setOwner(ItemStack stack, String owner);
	
	public boolean hasOwner(ItemStack stack);
}
