package mekanism.client;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.IClientTicker;
import mekanism.common.Mekanism;
import mekanism.common.ObfuscatedNames;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StringUtils;
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
	
	public Minecraft mc = FMLClientHandler.instance().getClient();
	
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
		}
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
