package mekanism.client;

import java.util.EnumSet;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemElectricBow;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemJetpack.JetpackMode;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.network.PacketConfiguratorState;
import mekanism.common.network.PacketElectricBowState;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketJetpackData.PacketType;
import mekanism.common.network.PacketWalkieTalkieState;
import mekanism.common.util.StackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatMessageComponent;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientPlayerTickHandler implements ITickHandler
{	
	public boolean lastTickUpdate = false;
	public Minecraft mc = Minecraft.getMinecraft();
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}
	
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		if(tickData[0] instanceof EntityPlayer)
		{
			EntityPlayer entityPlayer = (EntityPlayer)tickData[0];
			
			ItemStack stack = entityPlayer.getCurrentEquippedItem();
			
			if(entityPlayer.isSneaking() && StackUtils.getItem(entityPlayer.getCurrentEquippedItem()) instanceof ItemConfigurator)
			{
				ItemConfigurator item = (ItemConfigurator)entityPlayer.getCurrentEquippedItem().getItem();
				
	    		if(MekanismKeyHandler.modeSwitchKey.pressed)
	    		{
	    			if(!lastTickUpdate)
	    			{
		    			item.setState(stack, (byte)(item.getState(stack) < 3 ? item.getState(stack)+1 : 0));
		    			PacketHandler.sendPacket(Transmission.SERVER, new PacketConfiguratorState().setParams(item.getState(stack)));
		    			entityPlayer.sendChatToPlayer(ChatMessageComponent.createFromText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + "Configure State: " + item.getColor(item.getState(stack)) + item.getStateDisplay(item.getState(stack))));
		    			lastTickUpdate = true;
	    			}
	    		}
	    		else {
	    			lastTickUpdate = false;
	    		}
			}
			else if(entityPlayer.isSneaking() && StackUtils.getItem(entityPlayer.getCurrentEquippedItem()) instanceof ItemElectricBow)
			{
				ItemElectricBow item = (ItemElectricBow)entityPlayer.getCurrentEquippedItem().getItem();
				
				if(MekanismKeyHandler.modeSwitchKey.pressed)
				{
					if(!lastTickUpdate)
					{
						item.setFireState(stack, !item.getFireState(stack));
						PacketHandler.sendPacket(Transmission.SERVER, new PacketElectricBowState().setParams(item.getFireState(stack)));
						entityPlayer.sendChatToPlayer(ChatMessageComponent.createFromText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + "Fire Mode: " + (item.getFireState(stack) ? (EnumColor.DARK_GREEN + "ON") : (EnumColor.DARK_RED + "OFF"))));
						lastTickUpdate = true;
					}
				}
				else {
					lastTickUpdate = false;
				}
			}
			else if(entityPlayer.isSneaking() && StackUtils.getItem(entityPlayer.getCurrentEquippedItem()) instanceof ItemWalkieTalkie)
			{
				ItemWalkieTalkie item = (ItemWalkieTalkie)entityPlayer.getCurrentEquippedItem().getItem();
				
				if(MekanismKeyHandler.modeSwitchKey.pressed && item.getOn(stack))
				{
					if(!lastTickUpdate)
					{
						int newChan = item.getChannel(stack) < 9 ? item.getChannel(stack)+1 : 1;
						item.setChannel(stack, newChan);
						PacketHandler.sendPacket(Transmission.SERVER, new PacketWalkieTalkieState().setParams(newChan));
						Minecraft.getMinecraft().sndManager.playSoundFX("mekanism:etc.Ding", 1.0F, 1.0F);
						lastTickUpdate = true;
					}
				}
				else {
					lastTickUpdate = false;
				}
			}
			else if(entityPlayer.getCurrentItemOrArmor(3) != null && entityPlayer.getCurrentItemOrArmor(3).getItem() instanceof ItemJetpack)
			{
				ItemStack jetpack = entityPlayer.getCurrentItemOrArmor(3);
				
				if(MekanismKeyHandler.modeSwitchKey.pressed)
				{
					if(!lastTickUpdate)
					{
						((ItemJetpack)jetpack.getItem()).incrementMode(jetpack);
						PacketHandler.sendPacket(Transmission.SERVER, new PacketJetpackData().setParams(PacketType.MODE));
						Minecraft.getMinecraft().sndManager.playSoundFX("mekanism:etc.Hydraulic", 1.0F, 1.0F);
						lastTickUpdate = true;
					}
				}
				else {
					lastTickUpdate = false;
				}
			}
			else {
				lastTickUpdate = false;
			}
			
			if(Mekanism.jetpackOn.contains(entityPlayer) != isJetpackOn(entityPlayer))
			{
				if(isJetpackOn(entityPlayer))
				{
					Mekanism.jetpackOn.add(entityPlayer);
				}
				else {
					Mekanism.jetpackOn.remove(entityPlayer);
				}
				
				PacketHandler.sendPacket(Transmission.SERVER, new PacketJetpackData().setParams(PacketType.UPDATE, entityPlayer, isJetpackOn(entityPlayer)));
			}
			
			for(EntityPlayer entry : Mekanism.jetpackOn)
			{
				Mekanism.proxy.registerSound(entry);
			}
			
			if(entityPlayer.getCurrentItemOrArmor(3) != null && entityPlayer.getCurrentItemOrArmor(3).getItem() instanceof ItemJetpack)
			{
				MekanismClient.updateKey(entityPlayer, Keyboard.KEY_SPACE);
				MekanismClient.updateKey(entityPlayer, Keyboard.KEY_LSHIFT);
			}
			
			if(isJetpackOn(entityPlayer))
			{	
				ItemJetpack jetpack = (ItemJetpack)entityPlayer.getCurrentItemOrArmor(3).getItem();
				
				if(jetpack.getMode(entityPlayer.getCurrentItemOrArmor(3)) == JetpackMode.NORMAL)
				{
					entityPlayer.motionY = Math.min(mc.thePlayer.motionY + 0.15D, 0.5D);
					entityPlayer.fallDistance = 0.0F;
				}
				else if(jetpack.getMode(entityPlayer.getCurrentItemOrArmor(3)) == JetpackMode.HOVER)
				{
					if((!Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) || (Keyboard.isKeyDown(Keyboard.KEY_SPACE) && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)))
					{
						if(entityPlayer.motionY > 0)
						{
							entityPlayer.motionY = Math.max(entityPlayer.motionY - 0.15D, 0);
						}
						else if(entityPlayer.motionY < 0)
						{
							entityPlayer.motionY = Math.min(entityPlayer.motionY + 0.15D, 0);
						}
					}
					else {
						if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
						{
							entityPlayer.motionY = Math.min(mc.thePlayer.motionY + 0.15D, 0.2D);
						}
						else if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
						{
							entityPlayer.motionY = Math.max(mc.thePlayer.motionY - 0.15D, -0.2D);
						}
					}
					
					entityPlayer.fallDistance = 0.0F;
				}
				
				jetpack.useGas(entityPlayer.getCurrentItemOrArmor(3));
			}
		}
	}
	
	private boolean cacheJetpackOn(EntityPlayer player)
	{
		return Mekanism.jetpackOn.contains(player);
	}
	
	public static boolean isJetpackOn(EntityPlayer player)
	{
		ItemStack stack = player.inventory.armorInventory[2];
		
		if(stack != null)
		{
			if(stack.getItem() instanceof ItemJetpack)
			{
				ItemJetpack jetpack = (ItemJetpack)stack.getItem();
				
				if(jetpack.getGas(stack) != null)
				{
					if((Keyboard.isKeyDown(Keyboard.KEY_SPACE) && jetpack.getMode(stack) == JetpackMode.NORMAL))
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
		return "MekanismClientPlayer";
	}
}
