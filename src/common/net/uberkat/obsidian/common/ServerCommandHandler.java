package net.uberkat.obsidian.common;

import net.minecraft.src.ServerCommandManager;
import cpw.mods.fml.common.FMLCommonHandler;

/**
 * Handler to handle all incoming Obsidian Ingots commands.
 * @author AidanBrady
 *
 */
public class ServerCommandHandler 
{
	public static boolean initialized = false;
	
	public static void initialize()
	{
		if(!initialized)
		{
			initialized = true;
			
			ServerCommandManager manager = (ServerCommandManager)FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
			manager.registerCommand(new CommandOI());
		}
	}
}
