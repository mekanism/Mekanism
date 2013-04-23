package mekanism.common;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class CommonPlayerTickHandler implements ITickHandler
{
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		if(tickData[0] instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)tickData[0];
			if(player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof ItemPortableTeleporter)
			{
				ItemPortableTeleporter item = (ItemPortableTeleporter)player.getCurrentEquippedItem().getItem();
				ItemStack itemstack = player.getCurrentEquippedItem();
				
	    		Teleporter.Code teleCode = new Teleporter.Code(item.getDigit(itemstack, 0), item.getDigit(itemstack, 1), item.getDigit(itemstack, 2), item.getDigit(itemstack, 3));
	    		
	    		if(Mekanism.teleporters.containsKey(teleCode))
	    		{
	    			if(Mekanism.teleporters.get(teleCode).size() > 0 && Mekanism.teleporters.get(teleCode).size() <= 2)
	    			{
	    				int energyNeeded = item.calculateEnergyCost(player, MekanismUtils.getClosestCoords(teleCode, player));
	    				
	    				if(item.getEnergy(itemstack) < energyNeeded)
	    				{
		    				if(item.getStatus(itemstack) != 2)
		    				{
			    				item.setStatus(itemstack, 2);
			    				PacketHandler.sendStatusUpdate(player, 2);
		    				}
	    				}
	    				else {
		    				if(item.getStatus(itemstack) != 1)
		    				{
			    				item.setStatus(itemstack, 1);
			    				PacketHandler.sendStatusUpdate(player, 1);
		    				}
	    				}
	    				return;
	    			}
	    			else if(Mekanism.teleporters.get(teleCode).size() > 2)
	    			{
	    				if(item.getStatus(itemstack) != 3)
	    				{
	    					item.setStatus(itemstack, 3);
	    					PacketHandler.sendStatusUpdate(player, 3);
	    				}
	    				return;
	    			}
	    			else {
		    			if(item.getStatus(itemstack) != 4)
			    		{
			    			item.setStatus(itemstack, 4);
			    			PacketHandler.sendStatusUpdate(player, 4);
			    		}
		    			return;
	    			}
	    		}
	    		else {
	    			if(item.getStatus(itemstack) != 4)
		    		{
		    			item.setStatus(itemstack, 4);
		    			PacketHandler.sendStatusUpdate(player, 4);
		    		}
	    			return;
	    		}
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.PLAYER);
	}

	@Override
	public String getLabel()
	{
		return "MekanismCommonPlayer";
	}
}
