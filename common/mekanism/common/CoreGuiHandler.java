package mekanism.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

/**
 * Client and server GUI hander for Mekanism.
 * Uses CommonProxy to get the server GUI and ClientProxy for the client GUI.
 * @author AidanBrady
 *
 */
public class CoreGuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return Mekanism.proxy.getServerGui(ID, player, world, x, y, z);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return Mekanism.proxy.getClientGui(ID, player, world, x, y, z);
	}
}
