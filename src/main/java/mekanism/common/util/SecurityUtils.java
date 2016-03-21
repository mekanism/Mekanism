package mekanism.common.util;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.frequency.Frequency;
import mekanism.common.security.ISecurity;
import mekanism.common.security.ISecurity.SecurityMode;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.SecurityFrequency;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public final class SecurityUtils 
{
	public static boolean canAccess(EntityPlayer player, ItemStack stack)
	{
		if(stack == null || !(stack.getItem() instanceof ISecurityItem))
		{
			return true;
		}
		
		ISecurityItem security = (ISecurityItem)stack.getItem();
		
		return canAccess(security.getSecurity(stack), player.getCommandSenderName(), security.getOwner(stack));
	}
	
	public static boolean canAccess(EntityPlayer player, TileEntity tile)
	{
		if(tile == null || !(tile instanceof ISecurity))
		{
			return true;
		}
		
		ISecurity security = (ISecurity)tile;
		
		return canAccess(security.getSecurity().getMode(), player.getCommandSenderName(), security.getSecurity().getOwner());
	}
	
	public static boolean canAccess(SecurityMode mode, String username, String owner)
	{
		if(owner == null || username.equals(owner))
		{
			return true;
		}
		
		SecurityFrequency freq = getFrequency(owner);
		
		if(freq == null)
		{
			return true;
		}
		
		if(freq.override)
		{
			mode = freq.securityMode;
		}
		
		if(mode == SecurityMode.PUBLIC)
		{
			return true;
		}
		else if(mode == SecurityMode.TRUSTED)
		{
			if(freq.trusted.contains(username))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static SecurityFrequency getFrequency(String owner)
	{
		for(Frequency f : Mekanism.securityFrequencies.getFrequencies())
		{
			if(f instanceof SecurityFrequency && f.owner.equals(owner))
			{
				return (SecurityFrequency)f;
			}
		}
		
		return null;
	}
	
	public static String getOwnerDisplay(String user, String owner)
	{
		if(owner == null)
		{
			return EnumColor.RED + LangUtils.localize("gui.noOwner");
		}
		
		return EnumColor.GREY + LangUtils.localize("gui.owner") + ": " + (user.equals(owner) ? EnumColor.BRIGHT_GREEN : EnumColor.RED) + owner;
	}
}
