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
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		if(Mekanism.ticksPassed == 0 && ModLoader.getMinecraftInstance().theWorld != null && ModLoader.getMinecraftInstance().thePlayer != null)
		{
			MekanismUtils.checkForUpdates(ModLoader.getMinecraftInstance().thePlayer);
			Mekanism.ticksPassed++;
		}
	}
	
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		
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
