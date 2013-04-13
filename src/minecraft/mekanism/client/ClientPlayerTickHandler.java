package mekanism.client;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import mekanism.api.EnumColor;
import mekanism.common.EnumPacketType;
import mekanism.common.ItemConfigurator;
import mekanism.common.ItemElectricBow;
import mekanism.common.MekanismUtils;
import mekanism.common.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientPlayerTickHandler implements ITickHandler
{
	public boolean lastTickConfiguratorChange = false;
	public boolean lastTickElectricBowChange = false;
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}
	
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		if(tickData[0] instanceof EntityPlayer)
		{
			EntityPlayer entityPlayer = (EntityPlayer)tickData[0];
			
			if(entityPlayer.getCurrentEquippedItem() != null)
			{
				ItemStack stack = entityPlayer.getCurrentEquippedItem();
				
				if(entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemConfigurator)
				{
					ItemConfigurator item = (ItemConfigurator)entityPlayer.getCurrentEquippedItem().getItem();
					
		    		if(entityPlayer.isSneaking() && Keyboard.isKeyDown(Keyboard.KEY_M))
		    		{
		    			if(!lastTickConfiguratorChange)
		    			{
			    			item.setState(stack, (byte)(item.getState(stack) == 0 ? 1 : (item.getState(stack) == 1 ? 2 : 0)));
			    			PacketHandler.sendPacketDataInt(EnumPacketType.CONFIGURATOR_STATE, item.getState(stack));
			    			entityPlayer.sendChatToPlayer(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + "Configure State: " + (item.getState(stack) == 0 ? (EnumColor.BRIGHT_GREEN + "modify") : (item.getState(stack) == 1 ? (EnumColor.AQUA + "empty") : (EnumColor.YELLOW + "upgrade dump"))));
			    			lastTickConfiguratorChange = true;
		    			}
		    		}
		    		else {
		    			lastTickConfiguratorChange = false;
		    		}
				}
				else if(entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemElectricBow)
				{
					ItemElectricBow item = (ItemElectricBow)entityPlayer.getCurrentEquippedItem().getItem();
					
					if(entityPlayer.isSneaking() && Keyboard.isKeyDown(Keyboard.KEY_M))
					{
						if(!lastTickElectricBowChange)
						{
							item.setFireState(stack, !item.getFireState(stack));
							PacketHandler.sendPacketDataInt(EnumPacketType.ELECTRIC_BOW_STATE, item.getFireState(stack) ? 1 : 0);
							entityPlayer.sendChatToPlayer(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + "Fire Mode: " + (item.getFireState(stack) ? (EnumColor.DARK_GREEN + "ON") : (EnumColor.DARK_RED + "OFF")));
							lastTickElectricBowChange = true;
						}
					}
					else {
						lastTickElectricBowChange = false;
					}
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
		return "MekanismClientPlayer";
	}
}
