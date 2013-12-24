package mekanism.client;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.EnumColor;
import mekanism.api.IClientTicker;
import mekanism.client.sound.GasMaskSound;
import mekanism.client.sound.JetpackSound;
import mekanism.common.HolidayManager;
import mekanism.common.Mekanism;
import mekanism.common.ObfuscatedNames;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemElectricBow;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemJetpack.JetpackMode;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.network.PacketConfiguratorState;
import mekanism.common.network.PacketElectricBowState;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketJetpackData.JetpackPacket;
import mekanism.common.network.PacketScubaTankData;
import mekanism.common.network.PacketScubaTankData.ScubaTankPacket;
import mekanism.common.network.PacketWalkieTalkieState;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.StringUtils;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Client-side tick handler for Mekanism. Used mainly for the update check upon startup.
 * @author AidanBrady
 *
 */
@SideOnly(Side.CLIENT)
public class ClientTickHandler implements ITickHandler
{
	public boolean hasNotified = false;
	public boolean initHoliday = false;
	
	public boolean preloadedSounds = false;
	
	public boolean lastTickUpdate;
	
	public static Minecraft mc = FMLClientHandler.instance().getClient();
	
	public static final String MIKE_CAPE = "https://dl.dropboxusercontent.com/s/ji06yflixnszcby/cape.png";
	public static final String DONATE_CAPE = "https://dl.dropboxusercontent.com/u/90411166/donate.png";
	public static final String AIDAN_CAPE = "https://dl.dropboxusercontent.com/u/90411166/aidan.png";
	
	private Map<String, CapeBufferDownload> mikeDownload = new HashMap<String, CapeBufferDownload>();
	private Map<String, CapeBufferDownload> donateDownload = new HashMap<String, CapeBufferDownload>();
	private Map<String, CapeBufferDownload> aidanDownload = new HashMap<String, CapeBufferDownload>();
	
	public static Set<IClientTicker> tickingSet = new HashSet<IClientTicker>();
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		if(!preloadedSounds && mc.sndManager.sndSystem != null && MekanismClient.enableSounds)
		{
			new Thread(new Runnable() {
				@Override
				public void run()
				{
					preloadedSounds = true;
					MekanismClient.audioHandler.preloadSounds();
				}
			}).start();
		}
		
		MekanismClient.ticksPassed++;
		
		if(!hasNotified && mc.theWorld != null && Mekanism.latestVersionNumber != null && Mekanism.recentNews != null)
		{
			MekanismUtils.checkForUpdates(mc.thePlayer);
			hasNotified = true;
		}
		
		if(!Mekanism.proxy.isPaused())
		{
			for(Iterator<IClientTicker> iter = tickingSet.iterator(); iter.hasNext();)
			{
				IClientTicker ticker = iter.next();
				
				if(ticker.needsTicks())
				{
					ticker.clientTick();
				}
				else {
					iter.remove();
				}
			}
		}
		
		if(mc.theWorld != null)
		{
			if((!initHoliday || MekanismClient.ticksPassed % 1200 == 0) && mc.thePlayer != null)
			{
				HolidayManager.check();
				initHoliday = true;
			}
			
			for(EntityPlayer entityPlayer : (List<EntityPlayer>)mc.theWorld.playerEntities)
			{
				if(entityPlayer instanceof AbstractClientPlayer)
				{
					AbstractClientPlayer player = (AbstractClientPlayer)entityPlayer;
	                
					if(player != null)
					{
	                    if(StringUtils.stripControlCodes(player.username).equals("mikeacttck"))
	                    {
	                    	CapeBufferDownload download = mikeDownload.get(player.username);
	                    	
	                    	if(download == null)
	                    	{
	                    		download = new CapeBufferDownload(player.username, MIKE_CAPE);
	                    		mikeDownload.put(player.username, download);
	                    		
	                    		download.start();
	                    	}
	                    	else {
	                    		if(!download.downloaded)
	                    		{
	                    			continue;
	                    		}
	                    		
	                    		MekanismUtils.setPrivateValue(player, download.getImage(), AbstractClientPlayer.class, ObfuscatedNames.AbstractClientPlayer_downloadImageCape);
	                    		MekanismUtils.setPrivateValue(player, download.getResourceLocation(), AbstractClientPlayer.class, ObfuscatedNames.AbstractClientPlayer_locationCape);
	                    	}
	                    }
	                    else if(StringUtils.stripControlCodes(player.username).equals("aidancbrady"))
	                    {
	                    	CapeBufferDownload download = aidanDownload.get(player.username);
	                    	
	                    	if(download == null)
	                    	{
	                    		download = new CapeBufferDownload(player.username, AIDAN_CAPE);
	                    		aidanDownload.put(player.username, download);
	                    		
	                    		download.start();
	                    	}
	                    	else {
	                    		if(!download.downloaded)
	                    		{
	                    			continue;
	                    		}
	                    		
	                    		MekanismUtils.setPrivateValue(player, download.getImage(), AbstractClientPlayer.class, ObfuscatedNames.AbstractClientPlayer_downloadImageCape);
	                    		MekanismUtils.setPrivateValue(player, download.getResourceLocation(), AbstractClientPlayer.class, ObfuscatedNames.AbstractClientPlayer_locationCape);
	                    	}
	                    }
	                    else if(Mekanism.donators.contains(StringUtils.stripControlCodes(player.username)))
	                    {
	                    	CapeBufferDownload download = donateDownload.get(player.username);
	                    	
	                    	if(download == null)
	                    	{
	                    		download = new CapeBufferDownload(player.username, DONATE_CAPE);
	                    		donateDownload.put(player.username, download);
	                    		
	                    		download.start();
	                    	}
	                    	else {
	                    		if(!download.downloaded)
	                    		{
	                    			continue;
	                    		}
	                    		
	                    		MekanismUtils.setPrivateValue(player, download.getImage(), AbstractClientPlayer.class, ObfuscatedNames.AbstractClientPlayer_downloadImageCape);
	                    		MekanismUtils.setPrivateValue(player, download.getResourceLocation(), AbstractClientPlayer.class, ObfuscatedNames.AbstractClientPlayer_locationCape);
	                    	}
	                    }
					}
				}
			}
			
			ItemStack stack = mc.thePlayer.getCurrentEquippedItem();
			
			if(mc.currentScreen == null)
			{
				if(mc.thePlayer.isSneaking() && StackUtils.getItem(mc.thePlayer.getCurrentEquippedItem()) instanceof ItemConfigurator)
				{
					ItemConfigurator item = (ItemConfigurator)mc.thePlayer.getCurrentEquippedItem().getItem();
					
		    		if(MekanismKeyHandler.modeSwitchKey.pressed)
		    		{
		    			if(!lastTickUpdate)
		    			{
			    			item.setState(stack, (byte)(item.getState(stack) < 3 ? item.getState(stack)+1 : 0));
			    			PacketHandler.sendPacket(Transmission.SERVER, new PacketConfiguratorState().setParams(item.getState(stack)));
			    			mc.thePlayer.sendChatToPlayer(ChatMessageComponent.createFromText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + "Configure State: " + item.getColor(item.getState(stack)) + item.getStateDisplay(item.getState(stack))));
			    			lastTickUpdate = true;
		    			}
		    		}
		    		else {
		    			lastTickUpdate = false;
		    		}
				}
				else if(mc.thePlayer.isSneaking() && StackUtils.getItem(mc.thePlayer.getCurrentEquippedItem()) instanceof ItemElectricBow)
				{
					ItemElectricBow item = (ItemElectricBow)mc.thePlayer.getCurrentEquippedItem().getItem();
					
					if(MekanismKeyHandler.modeSwitchKey.pressed)
					{
						if(!lastTickUpdate)
						{
							item.setFireState(stack, !item.getFireState(stack));
							PacketHandler.sendPacket(Transmission.SERVER, new PacketElectricBowState().setParams(item.getFireState(stack)));
							mc.thePlayer.sendChatToPlayer(ChatMessageComponent.createFromText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + "Fire Mode: " + (item.getFireState(stack) ? (EnumColor.DARK_GREEN + "ON") : (EnumColor.DARK_RED + "OFF"))));
							lastTickUpdate = true;
						}
					}
					else {
						lastTickUpdate = false;
					}
				}
				else if(mc.thePlayer.isSneaking() && StackUtils.getItem(mc.thePlayer.getCurrentEquippedItem()) instanceof ItemWalkieTalkie)
				{
					ItemWalkieTalkie item = (ItemWalkieTalkie)mc.thePlayer.getCurrentEquippedItem().getItem();
					
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
				else if(mc.thePlayer.getCurrentItemOrArmor(3) != null && mc.thePlayer.getCurrentItemOrArmor(3).getItem() instanceof ItemJetpack)
				{
					ItemStack jetpack = mc.thePlayer.getCurrentItemOrArmor(3);
					
					if(MekanismKeyHandler.modeSwitchKey.pressed)
					{
						if(!lastTickUpdate)
						{
							((ItemJetpack)jetpack.getItem()).incrementMode(jetpack);
							PacketHandler.sendPacket(Transmission.SERVER, new PacketJetpackData().setParams(JetpackPacket.MODE));
							Minecraft.getMinecraft().sndManager.playSoundFX("mekanism:etc.Hydraulic", 1.0F, 1.0F);
							lastTickUpdate = true;
						}
					}
					else {
						lastTickUpdate = false;
					}
				}
				else if(mc.thePlayer.getCurrentItemOrArmor(3) != null && mc.thePlayer.getCurrentItemOrArmor(3).getItem() instanceof ItemScubaTank)
				{
					ItemStack scubaTank = mc.thePlayer.getCurrentItemOrArmor(3);
					
					if(MekanismKeyHandler.modeSwitchKey.pressed)
					{
						if(!lastTickUpdate)
						{
							((ItemScubaTank)scubaTank.getItem()).toggleFlowing(scubaTank);
							PacketHandler.sendPacket(Transmission.SERVER, new PacketScubaTankData().setParams(ScubaTankPacket.MODE));
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
			}
			
			if(Mekanism.jetpackOn.contains(mc.thePlayer.username) != isJetpackOn(mc.thePlayer))
			{
				if(isJetpackOn(mc.thePlayer))
				{
					Mekanism.jetpackOn.add(mc.thePlayer.username);
				}
				else {
					Mekanism.jetpackOn.remove(mc.thePlayer.username);
				}
				
				PacketHandler.sendPacket(Transmission.SERVER, new PacketJetpackData().setParams(JetpackPacket.UPDATE, mc.thePlayer.username, isJetpackOn(mc.thePlayer)));
			}
			
			if(Mekanism.gasmaskOn.contains(mc.thePlayer.username) != isGasMaskOn(mc.thePlayer))
			{
				if(isGasMaskOn(mc.thePlayer) && mc.currentScreen == null)
				{
					System.out.println("on");
					Mekanism.gasmaskOn.add(mc.thePlayer.username);
				}
				else {
					Mekanism.gasmaskOn.remove(mc.thePlayer.username);
				}
				
				PacketHandler.sendPacket(Transmission.SERVER, new PacketScubaTankData().setParams(ScubaTankPacket.UPDATE, mc.thePlayer.username, isGasMaskOn(mc.thePlayer)));
			}
			
			if(MekanismClient.audioHandler != null)
			{
				for(String username : Mekanism.jetpackOn)
				{
					if(mc.theWorld.getPlayerEntityByName(username) != null)
					{
						if(MekanismClient.audioHandler.getFrom(mc.theWorld.getPlayerEntityByName(username)) == null)
						{
							new JetpackSound(MekanismClient.audioHandler.getIdentifier(), mc.theWorld.getPlayerEntityByName(username));
						}
					}
				}
				
				for(String username : Mekanism.gasmaskOn)
				{
					if(mc.theWorld.getPlayerEntityByName(username) != null)
					{
						if(MekanismClient.audioHandler.getFrom(mc.theWorld.getPlayerEntityByName(username)) == null)
						{
							new GasMaskSound(MekanismClient.audioHandler.getIdentifier(), mc.theWorld.getPlayerEntityByName(username));
						}
					}
				}
			}
			
			if(mc.thePlayer.getCurrentItemOrArmor(3) != null && mc.thePlayer.getCurrentItemOrArmor(3).getItem() instanceof ItemJetpack)
			{
				MekanismClient.updateKey(Keyboard.KEY_SPACE);
				MekanismClient.updateKey(Keyboard.KEY_LSHIFT);
			}
			
			if(isJetpackOn(mc.thePlayer))
			{
				ItemJetpack jetpack = (ItemJetpack)mc.thePlayer.getCurrentItemOrArmor(3).getItem();
				
				if(jetpack.getMode(mc.thePlayer.getCurrentItemOrArmor(3)) == JetpackMode.NORMAL)
				{
					mc.thePlayer.motionY = Math.min(mc.thePlayer.motionY + 0.15D, 0.5D);
					mc.thePlayer.fallDistance = 0.0F;
				}
				else if(jetpack.getMode(mc.thePlayer.getCurrentItemOrArmor(3)) == JetpackMode.HOVER)
				{
					if((!Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) || (Keyboard.isKeyDown(Keyboard.KEY_SPACE) && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) || mc.currentScreen != null)
					{
						if(mc.thePlayer.motionY > 0)
						{
							mc.thePlayer.motionY = Math.max(mc.thePlayer.motionY - 0.15D, 0);
						}
						else if(mc.thePlayer.motionY < 0)
						{
							mc.thePlayer.motionY = Math.min(mc.thePlayer.motionY + 0.15D, 0);
						}
					}
					else {
						if(Keyboard.isKeyDown(Keyboard.KEY_SPACE) && mc.currentScreen == null)
						{
							mc.thePlayer.motionY = Math.min(mc.thePlayer.motionY + 0.15D, 0.2D);
						}
						else if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && mc.currentScreen == null)
						{
							mc.thePlayer.motionY = Math.max(mc.thePlayer.motionY - 0.15D, -0.2D);
						}
					}
					
					mc.thePlayer.fallDistance = 0.0F;
				}
				
				jetpack.useGas(mc.thePlayer.getCurrentItemOrArmor(3));
			}
			
			if(isGasMaskOn(mc.thePlayer))
			{
				ItemScubaTank tank = (ItemScubaTank)mc.thePlayer.getCurrentItemOrArmor(3).getItem();
				
				tank.useGas(mc.thePlayer.getCurrentItemOrArmor(3));
				mc.thePlayer.setAir(300);
				mc.thePlayer.clearActivePotions();
			}
		}
	}
	
	public static void killDeadNetworks()
	{
		for(Iterator<IClientTicker> iter = tickingSet.iterator(); iter.hasNext();)
		{
			if(!iter.next().needsTicks())
			{
				iter.remove();
			}
		}
	}
	
	public static boolean isJetpackOn(EntityPlayer player)
	{
		if(player != mc.thePlayer)
		{
			return Mekanism.jetpackOn.contains(player.username);
		}
		
		ItemStack stack = player.inventory.armorInventory[2];
		
		if(stack != null)
		{
			if(stack.getItem() instanceof ItemJetpack)
			{
				ItemJetpack jetpack = (ItemJetpack)stack.getItem();
				
				if(jetpack.getGas(stack) != null)
				{
					if((Keyboard.isKeyDown(Keyboard.KEY_SPACE) && jetpack.getMode(stack) == JetpackMode.NORMAL) && mc.currentScreen == null)
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
	
	public static boolean isGasMaskOn(EntityPlayer player)
	{
		if(player != mc.thePlayer)
		{
			return Mekanism.gasmaskOn.contains(player.username);
		}
		
		ItemStack tank = player.inventory.armorInventory[2];
		ItemStack mask = player.inventory.armorInventory[3];
		
		if(tank != null && mask != null)
		{
			if(tank.getItem() instanceof ItemScubaTank && mask.getItem() instanceof ItemGasMask)
			{
				ItemScubaTank scubaTank = (ItemScubaTank)tank.getItem();
				
				if(scubaTank.getGas(tank) != null)
				{
					if(scubaTank.getFlowing(tank))
					{
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		if(MekanismClient.audioHandler != null)
		{
			synchronized(MekanismClient.audioHandler.sounds)
			{
				MekanismClient.audioHandler.onTick();
			}
		}
	}
	
	@Override
	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.CLIENT);
	}

	@Override
	public String getLabel()
	{
		return "MekanismClient";
	}
}
