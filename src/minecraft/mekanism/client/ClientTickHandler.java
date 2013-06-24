package mekanism.client;

import java.util.EnumSet;
import java.util.List;

import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import net.minecraft.client.Minecraft;
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
	
	public Minecraft mc = FMLClientHandler.instance().getClient();
	
	public static String MIKE_CAPE = "https://dl.dropboxusercontent.com/s/ji06yflixnszcby/cape.png";
	public static String DONATE_CAPE = "https://dl.dropboxusercontent.com/u/90411166/donate.png";
	public static String AIDAN_CAPE = "https://dl.dropboxusercontent.com/u/90411166/aidan.png";
	
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
			for(EntityPlayer player : (List<EntityPlayer>)mc.theWorld.playerEntities)
			{
				String oldCloak = player.cloakUrl;
                
				if(player != null && player.cloakUrl != null)
				{
	                if(player.cloakUrl.startsWith("http://skins.minecraft.net/MinecraftCloaks/"))
	                {
	                    if(StringUtils.stripControlCodes(player.username).equals("mikeacttck"))
	                    {
	                        player.cloakUrl = MIKE_CAPE;
	                    }
	                    else if(StringUtils.stripControlCodes(player.username).equals("aidancbrady"))
	                    {
	                    	player.cloakUrl = AIDAN_CAPE;
	                    }
	                    else if(Mekanism.donators.contains(StringUtils.stripControlCodes(player.username)))
	                    {
	                    	player.cloakUrl = DONATE_CAPE;
	                    }
	                    
	                    if(!oldCloak.equals(player.cloakUrl))
	                    {
	                        mc.renderEngine.obtainImageData(player.cloakUrl, new CapeBufferDownload());
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
