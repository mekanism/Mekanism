package mekanism.common.security;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.frequency.Frequency;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.util.LangUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public interface ISecurity 
{
	public TileComponentSecurity getSecurity();
	
	public enum SecurityMode
	{
		PUBLIC(EnumColor.BRIGHT_GREEN + "security.public"),
		PRIVATE(EnumColor.RED + "security.private"),
		TRUSTED(EnumColor.ORANGE + "security.trusted");
		
		private String display;
		private EnumColor color;

		public String getDisplay()
		{
			return color + LangUtils.localize(display);
		}

		private SecurityMode(String s)
		{
			display = s;
		}
	}
	
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
		
		if(mode == SecurityMode.PUBLIC)
		{
			return true;
		}
		else if(mode == SecurityMode.TRUSTED)
		{
			SecurityFrequency freq = getFrequency(owner);
			
			if(freq == null || freq.trusted.contains(username))
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
