package mekanism.common.util;

import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.frequency.Frequency;
import mekanism.common.security.IOwnerItem;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.security.SecurityData;
import mekanism.common.security.SecurityFrequency;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import cpw.mods.fml.relauncher.Side;

public final class SecurityUtils 
{
	public static boolean canAccess(EntityPlayer player, ItemStack stack)
	{
		if(!(stack.getItem() instanceof ISecurityItem) && stack.getItem() instanceof IOwnerItem)
		{
			String owner = ((IOwnerItem)stack.getItem()).getOwner(stack);
			
			return owner == null || owner.equals(player.getCommandSenderName());
		}
		
		if(stack == null || !(stack.getItem() instanceof ISecurityItem))
		{
			return true;
		}
		
		ISecurityItem security = (ISecurityItem)stack.getItem();
		
		if(MekanismUtils.isOp(player))
		{
			return true;
		}
		
		return canAccess(security.getSecurity(stack), player.getCommandSenderName(), security.getOwner(stack));
	}
	
	public static boolean canAccess(EntityPlayer player, TileEntity tile)
	{
		if(tile == null || !(tile instanceof ISecurityTile))
		{
			return true;
		}
		
		ISecurityTile security = (ISecurityTile)tile;
		
		if(MekanismUtils.isOp(player))
		{
			return true;
		}
		
		return canAccess(security.getSecurity().getMode(), player.getCommandSenderName(), security.getSecurity().getOwner());
	}
	
	private static boolean canAccess(SecurityMode mode, String username, String owner)
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
		if(owner != null)
		{
			for(Frequency f : Mekanism.securityFrequencies.getFrequencies())
			{
				if(f instanceof SecurityFrequency && f.owner.equals(owner))
				{
					return (SecurityFrequency)f;
				}
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
	
	public static SecurityMode getSecurity(ISecurityTile security, Side side)
	{
		if(side == Side.SERVER)
		{
			SecurityFrequency freq = security.getSecurity().getFrequency();
			
			if(freq != null && freq.override)
			{
				return freq.securityMode;
			}
		}
		else if(side == Side.CLIENT)
		{
			SecurityData data = MekanismClient.clientSecurityMap.get(security.getSecurity().getOwner());
			
			if(data != null && data.override)
			{
				return data.mode;
			}
		}
		
		return security.getSecurity().getMode();
	}
	
	public static String getSecurityDisplay(ItemStack stack, Side side)
	{
		ISecurityItem security = (ISecurityItem)stack.getItem();
		SecurityMode mode = security.getSecurity(stack);
		
		if(security.getOwner(stack) != null)
		{
			if(side == Side.SERVER)
			{
				SecurityFrequency freq = getFrequency(security.getOwner(stack));
				
				if(freq != null && freq.override)
				{
					mode = freq.securityMode;
				}
			}
			else if(side == Side.CLIENT)
			{
				SecurityData data = MekanismClient.clientSecurityMap.get(security.getOwner(stack));
				
				if(data != null && data.override)
				{
					mode = data.mode;
				}
			}
		}
		
		return mode.getDisplay();
	}
	
	public static String getSecurityDisplay(TileEntity tile, Side side)
	{
		ISecurityTile security = (ISecurityTile)tile;
		SecurityMode mode = security.getSecurity().getMode();
		
		if(security.getSecurity().getOwner() != null)
		{
			if(side == Side.SERVER)
			{
				SecurityFrequency freq = getFrequency(security.getSecurity().getOwner());
				
				if(freq != null && freq.override)
				{
					mode = freq.securityMode;
				}
			}
			else if(side == Side.CLIENT)
			{
				SecurityData data = MekanismClient.clientSecurityMap.get(security.getSecurity().getOwner());
				
				if(data != null && data.override)
				{
					mode = data.mode;
				}
			}
		}
		
		return mode.getDisplay();
	}
	
	public static boolean isOverridden(ItemStack stack, Side side)
	{
		ISecurityItem security = (ISecurityItem)stack.getItem();
		
		if(security.getOwner(stack) == null)
		{
			return false;
		}
		
		if(side == Side.SERVER)
		{
			SecurityFrequency freq = getFrequency(security.getOwner(stack));
			
			return freq != null && freq.override;
		}
		else {
			SecurityData data = MekanismClient.clientSecurityMap.get(security.getOwner(stack));
			
			return data != null && data.override;
		}
	}
	
	public static boolean isOverridden(TileEntity tile, Side side)
	{
		ISecurityTile security = (ISecurityTile)tile;
		
		if(security.getSecurity().getOwner() == null)
		{
			return false;
		}
		
		if(side == Side.SERVER)
		{
			SecurityFrequency freq = getFrequency(security.getSecurity().getOwner());
			
			return freq != null && freq.override;
		}
		else {
			SecurityData data = MekanismClient.clientSecurityMap.get(security.getSecurity().getOwner());
			
			return data != null && data.override;
		}
	}
}
