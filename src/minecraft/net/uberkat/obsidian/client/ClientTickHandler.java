package net.uberkat.obsidian.client;

import java.util.EnumSet;

import net.minecraft.src.ModLoader;
import net.minecraftforge.common.MinecraftForge;
import net.uberkat.obsidian.common.ObsidianIngots;
import net.uberkat.obsidian.common.ObsidianUtils;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

/**
 * Client-side tick handler for Obsidian Ingots. Used mainly for the update check upon startup.
 * @author AidanBrady
 *
 */
public class ClientTickHandler implements ITickHandler
{
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		if(ObsidianIngots.ticksPassed == 0 && ModLoader.getMinecraftInstance().theWorld != null && ModLoader.getMinecraftInstance().thePlayer != null)
		{
			ObsidianUtils.checkForUpdates(ModLoader.getMinecraftInstance().thePlayer);
			ObsidianIngots.ticksPassed++;
		}
	}
	
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		
	}

	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.CLIENT);
	}

	public String getLabel()
	{
		return "ObsidianIngots";
	}
}
