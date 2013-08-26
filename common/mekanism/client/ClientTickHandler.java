package mekanism.client;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
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
	
	public Minecraft mc = FMLClientHandler.instance().getClient();
	
	public static final String MIKE_CAPE = "https://dl.dropboxusercontent.com/s/ji06yflixnszcby/cape.png";
	public static final String DONATE_CAPE = "https://dl.dropboxusercontent.com/u/90411166/donate.png";
	public static final String AIDAN_CAPE = "https://dl.dropboxusercontent.com/u/90411166/aidan.png";
	
	private Map<String, ThreadDownloadImageData> mikeDownload = new HashMap<String, ThreadDownloadImageData>();
	private Map<String, ThreadDownloadImageData> donateDownload = new HashMap<String, ThreadDownloadImageData>();
	private Map<String, ThreadDownloadImageData> aidanDownload = new HashMap<String, ThreadDownloadImageData>();
	
	private void updateCape(EntityPlayer player, ThreadDownloadImageData newCape)
	{
		if(player.getHideCape())
		{
			try {
				Method m = EntityPlayer.class.getDeclaredMethod("setHideCape", Integer.class, Boolean.class);
				m.invoke(player, 1, false);
			} catch(Exception e) {}
		}
		
		if(MekanismUtils.getPrivateValue(player, AbstractClientPlayer.class, "field_110315_c") != newCape)
		{
			MekanismUtils.setPrivateValue(player, newCape, AbstractClientPlayer.class, "field_110315_c");
		}
	}
	
	private ResourceLocation getCapeResource(EntityPlayer player)
	{
		if(player instanceof AbstractClientPlayer)
		{
			return (ResourceLocation)MekanismUtils.getPrivateValue(player, AbstractClientPlayer.class, "field_110313_e");
		}
		
		return null;
	}
	
	private ThreadDownloadImageData getCape(EntityPlayer player, String cape)
	{
		TextureManager texturemanager = Minecraft.getMinecraft().func_110434_K();
		Object object = texturemanager.func_110581_b(getCapeResource(player));

		if(object == null) 
		{
			object = new ThreadDownloadImageData(cape, getCapeResource(player), new CapeBufferDownload());
			texturemanager.func_110579_a(getCapeResource(player), (TextureObject)object);
		}

		return (ThreadDownloadImageData)object;
	}
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		if(!hasNotified && mc.theWorld != null && Mekanism.latestVersionNumber != null && Mekanism.recentNews != null)
		{
			MekanismUtils.checkForUpdates(mc.thePlayer);
			hasNotified = true;
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
	                    	if(mikeDownload.get(player.username) == null)
	                    	{
	                    		mikeDownload.put(player.username, getCape(player, MIKE_CAPE));
	                    	}
	                    	
	                    	updateCape(player, mikeDownload.get(player.username));
	                    }
	                    else if(StringUtils.stripControlCodes(player.username).equals("aidancbrady"))
	                    {
	                    	if(aidanDownload.get(player.username) == null)
	                    	{
	                    		aidanDownload.put(player.username, getCape(player, AIDAN_CAPE));
	                    	}
	                    	
	                    	updateCape(player, aidanDownload.get(player.username));
	                    }
	                    else if(Mekanism.donators.contains(StringUtils.stripControlCodes(player.username)) || player.username.contains("Player"))
	                    {
	                    	if(donateDownload.get(player.username) == null)
	                    	{
	                    		donateDownload.put(player.username, getCape(player, DONATE_CAPE));
	                    	}
	                    	
	                    	updateCape(player, donateDownload.get(player.username));
	                    }
					}
				}
			}
		}
	}
	
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		if(Mekanism.audioHandler != null)
		{
			synchronized(Mekanism.audioHandler.sounds)
			{
				Mekanism.audioHandler.onTick();
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
