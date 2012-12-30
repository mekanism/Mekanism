package mekanism.client;

import java.util.EnumSet;

import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import net.minecraft.src.ModLoader;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

/**
 * Client-side tick handler for Mekanism. Used mainly for the update check upon startup.
 * @author AidanBrady
 *
 */
public class ClientTickHandler implements ITickHandler
{
	public boolean hasNotified = false;
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		if(!hasNotified && ModLoader.getMinecraftInstance().theWorld != null && ModLoader.getMinecraftInstance().thePlayer != null && Mekanism.latestVersionNumber != null && Mekanism.recentNews != null)
		{
			MekanismUtils.checkForUpdates(ModLoader.getMinecraftInstance().thePlayer);
			hasNotified = true;
		}
	}
	
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		synchronized(Mekanism.audioHandler.sounds)
		{
			Mekanism.audioHandler.onTick();
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
		return "Mekanism";
	}
}
