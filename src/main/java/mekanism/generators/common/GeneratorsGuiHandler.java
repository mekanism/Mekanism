package mekanism.generators.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

/**
 * Client and server GUI hander for Mekanism.
 * Uses CommonProxy to get the server GUI and ClientProxy for the client GUI.
 * @author AidanBrady
 *
 */
public class GeneratorsGuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return MekanismGenerators.proxy.getServerGui(ID, player, world, x, y, z);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return MekanismGenerators.proxy.getClientGui(ID, player, world, x, y, z);
	}
}
