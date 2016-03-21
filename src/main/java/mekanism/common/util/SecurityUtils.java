package mekanism.common.util;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.frequency.Frequency;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.security.SecurityFrequency;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;

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
		if(tile == null || !(tile instanceof ISecurityTile))
		{
			return true;
		}
		
		ISecurityTile security = (ISecurityTile)tile;
		
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
	
	public static void displayNoAccess(EntityPlayer player)
	{
		player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.RED + LangUtils.localize("gui.noAccessDesc")));
	}
	
	public static SecurityMode getSecurity(ISecurityTile security)
	{
		SecurityFrequency freq = security.getSecurity().getFrequency();
		
		if(freq != null && freq.override)
		{
			return freq.securityMode;
		}
		
		return security.getSecurity().getMode();
	}
	
	public static String getSecurityDisplay(ItemStack stack)
	{
		ISecurityItem security = (ISecurityItem)stack.getItem();
		SecurityMode mode = security.getSecurity(stack);
		
		if(security.getOwner(stack) != null)
		{
			SecurityFrequency freq = getFrequency(security.getOwner(stack));
			
			if(freq != null && freq.override)
			{
				mode = freq.securityMode;
			}
		}
		
		return mode.getDisplay();
	}
	
	public static String getSecurityDisplay(TileEntity tile)
	{
		ISecurityTile security = (ISecurityTile)tile;
		SecurityMode mode = security.getSecurity().getMode();
		
		if(security.getSecurity().getOwner() != null)
		{
			SecurityFrequency freq = getFrequency(security.getSecurity().getOwner());
			
			if(freq != null && freq.override)
			{
				mode = freq.securityMode;
			}
		}
		
		return mode.getDisplay();
	}
	
	public static boolean isOverridden(ItemStack stack)
	{
		ISecurityItem security = (ISecurityItem)stack.getItem();
		
		if(security.getOwner(stack) == null)
		{
			return false;
		}
		
		SecurityFrequency freq = getFrequency(security.getOwner(stack));
		
		return freq != null && freq.override;
	}
	
	public static boolean isOverridden(TileEntity tile)
	{
		ISecurityTile security = (ISecurityTile)tile;
		
		if(security.getSecurity().getOwner() == null)
		{
			return false;
		}
		
		SecurityFrequency freq = getFrequency(security.getSecurity().getOwner());
		
		return freq != null && freq.override;
	}
}
