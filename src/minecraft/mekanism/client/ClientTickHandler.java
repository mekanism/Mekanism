package mekanism.client;

import java.util.EnumSet;

import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;

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
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		if(!hasNotified && FMLClientHandler.instance().getClient().theWorld != null && FMLClientHandler.instance().getClient().thePlayer != null && Mekanism.latestVersionNumber != null && Mekanism.recentNews != null)
		{
			MekanismUtils.checkForUpdates(FMLClientHandler.instance().getClient().thePlayer);
			hasNotified = true;
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
