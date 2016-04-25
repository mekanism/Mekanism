package mekanism.common.security;

import mekanism.common.security.ISecurityTile.SecurityMode;
import net.minecraft.item.ItemStack;

public interface ISecurityItem extends IOwnerItem
{	
	public SecurityMode getSecurity(ItemStack stack);
	
	public void setSecurity(ItemStack stack, SecurityMode mode);
	
	public boolean hasSecurity(ItemStack stack);
}
