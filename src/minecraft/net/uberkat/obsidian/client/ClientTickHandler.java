package net.uberkat.obsidian.client;

import java.util.EnumSet;

import net.minecraft.src.ModLoader;
import net.minecraftforge.common.MinecraftForge;
import net.uberkat.obsidian.common.ObsidianIngotsCore;
import net.uberkat.obsidian.common.ObsidianUtils;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ClientTickHandler implements ITickHandler
{
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		if(ObsidianIngotsCore.ticksPassed == 0 && ModLoader.getMinecraftInstance().theWorld != null && ModLoader.getMinecraftInstance().thePlayer != null)
		{
			ObsidianUtils.checkForUpdates(ModLoader.getMinecraftInstance().thePlayer);
			ObsidianIngotsCore.ticksPassed++;
		}
	}
	
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		
	}

	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.CLIENT, TickType.WORLDLOAD);
	}

	public String getLabel()
	{
		return "ObsidianIngots";
	}
}
