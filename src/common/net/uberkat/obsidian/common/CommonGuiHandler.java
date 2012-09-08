package net.uberkat.obsidian.common;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.IGuiHandler;

/**
 * Client and server GUI hander for Obsidian Ingots. 
 * Uses CommonProxy to get the server GUI and ClientProxy for the client GUI.
 * @author AidanBrady
 *
 */
public class CommonGuiHandler implements IGuiHandler
{
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		return ObsidianIngots.proxy.getServerGui(ID, player, world, x, y, z);
	}

	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		return ObsidianIngots.proxy.getClientGui(ID, player, world, x, y, z);
	}
}
