package mekanism.common;

import net.minecraft.command.ServerCommandManager;
import cpw.mods.fml.common.FMLCommonHandler;

/**
 * Handler to handle all incoming Mekanism commands.
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
			manager.registerCommand(new CommandMekanism());
		}
	}
}
