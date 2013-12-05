package mekanism.common;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import mekanism.common.PacketHandler.Transmission;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemJetpack.JetpackMode;
import mekanism.common.network.PacketStatusUpdate;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class CommonPlayerTickHandler implements ITickHandler
{
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}

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
			    				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketStatusUpdate().setParams(2), player);
		    				}
	    				}
	    				else {
		    				if(item.getStatus(itemstack) != 1)
		    				{
			    				item.setStatus(itemstack, 1);
			    				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketStatusUpdate().setParams(1), player);
		    				}
	    				}
	    				return;
	    			}
	    			else if(Mekanism.teleporters.get(teleCode).size() > 2)
	    			{
	    				if(item.getStatus(itemstack) != 3)
	    				{
	    					item.setStatus(itemstack, 3);
	    					PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketStatusUpdate().setParams(3), player);
	    				}
	    				return;
	    			}
	    			else {
		    			if(item.getStatus(itemstack) != 4)
			    		{
			    			item.setStatus(itemstack, 4);
			    			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketStatusUpdate().setParams(4), player);
			    		}
		    			return;
	    			}
	    		}
	    		else {
	    			if(item.getStatus(itemstack) != 4)
		    		{
		    			item.setStatus(itemstack, 4);
		    			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketStatusUpdate().setParams(4), player);
		    		}
	    			return;
	    		}
			}
			
			if(isJetpackOn(player))
			{
				ItemJetpack jetpack = (ItemJetpack)player.getCurrentItemOrArmor(3).getItem();
				
				if(jetpack.getMode(player.getCurrentItemOrArmor(3)) == JetpackMode.NORMAL)
				{
					player.motionY = Math.min(player.motionY + 0.15D, 0.5D);
					player.fallDistance = 0.0F;
				}
				else if(jetpack.getMode(player.getCurrentItemOrArmor(3)) == JetpackMode.HOVER)
				{
					if((!Mekanism.keyMap.has(player, KeySync.SPACE) && !Mekanism.keyMap.has(player, KeySync.LSHIFT)) || (Mekanism.keyMap.has(player, KeySync.SPACE) && Mekanism.keyMap.has(player, KeySync.LSHIFT)))
					{
						if(player.motionY > 0)
						{
							player.motionY = Math.max(player.motionY - 0.15D, 0);
						}
						else if(player.motionY < 0)
						{
							player.motionY = Math.min(player.motionY + 0.15D, 0);
						}
					}
					else {
						if(Mekanism.keyMap.has(player, KeySync.SPACE))
						{
							player.motionY = Math.min(player.motionY + 0.15D, 0.2D);
						}
						else if(Mekanism.keyMap.has(player, KeySync.LSHIFT))
						{
							player.motionY = Math.max(player.motionY - 0.15D, -0.2D);
						}
					}
					
					player.fallDistance = 0.0F;
				}
				
				jetpack.useGas(player.getCurrentItemOrArmor(3));
			}
		}
	}
	
	public boolean isJetpackOn(EntityPlayer player)
	{
		ItemStack stack = player.inventory.armorInventory[2];
		
		if(stack != null)
		{
			if(stack.getItem() instanceof ItemJetpack)
			{
				ItemJetpack jetpack = (ItemJetpack)stack.getItem();
				
				if(jetpack.getGas(stack) != null)
				{
					if((Mekanism.keyMap.has(player, KeySync.SPACE) && jetpack.getMode(stack) == JetpackMode.NORMAL))
					{
						return true;
					}
					else if(jetpack.getMode(stack) == JetpackMode.HOVER)
					{
						return true;
					}
				}
			}
		}
		
		return false;
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
