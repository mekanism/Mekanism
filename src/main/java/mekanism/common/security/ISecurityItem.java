package mekanism.common.security;

import mekanism.common.security.ISecurity.SecurityMode;
import net.minecraft.item.ItemStack;

public interface ISecurityItem 
{
	public String getOwner(ItemStack stack);
	
	public void setOwner(ItemStack stack, String owner);
	
	public SecurityMode getSecurity(ItemStack stack);
	
	public void setSecurity(ItemStack stack, SecurityMode mode);
}
