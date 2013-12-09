package mekanism.client;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.IClientTicker;
import mekanism.client.sound.GasMaskSound;
import mekanism.client.sound.JetpackSound;
import mekanism.common.Mekanism;
import mekanism.common.ObfuscatedNames;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemJetpack.JetpackMode;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketJetpackData.PacketType;
import mekanism.common.network.PacketScubaTankData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
	
	public boolean preloadedSounds = false;
	
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
				
				ticker.clientTick();
				
				if(!ticker.needsTicks())
				{
					iter.remove();
				}
			}
		}
		
		if(mc.theWorld != null)
		{
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
			
			if(Mekanism.jetpackOn.contains(mc.thePlayer) != isJetpackOn(mc.thePlayer))
			{
				if(isJetpackOn(mc.thePlayer))
				{
					Mekanism.jetpackOn.add(mc.thePlayer);
				}
				else {
					Mekanism.jetpackOn.remove(mc.thePlayer);
				}
				
				PacketHandler.sendPacket(Transmission.SERVER, new PacketJetpackData().setParams(PacketType.UPDATE, mc.thePlayer, isJetpackOn(mc.thePlayer)));
			}
			
			if(Mekanism.gasmaskOn.contains(mc.thePlayer) != isGasMaskOn(mc.thePlayer))
			{
				if(isGasMaskOn(mc.thePlayer) && mc.currentScreen == null)
				{
					Mekanism.gasmaskOn.add(mc.thePlayer);
				}
				else {
					Mekanism.gasmaskOn.remove(mc.thePlayer);
				}
				
				PacketHandler.sendPacket(Transmission.SERVER, new PacketScubaTankData().setParams(PacketType.UPDATE, mc.thePlayer, isGasMaskOn(mc.thePlayer)));
			}
			
			if(MekanismClient.audioHandler != null)
			{
				for(EntityPlayer entry : Mekanism.jetpackOn)
				{
					if(MekanismClient.audioHandler.getFrom(entry) == null)
					{
						new JetpackSound(MekanismClient.audioHandler.getIdentifier(), entry);
					}
				}
				
				for(EntityPlayer entry : Mekanism.gasmaskOn)
				{
					if(MekanismClient.audioHandler.getFrom(entry) == null)
					{
						new GasMaskSound(MekanismClient.audioHandler.getIdentifier(), entry);
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
