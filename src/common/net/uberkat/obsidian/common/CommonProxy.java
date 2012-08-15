package net.uberkat.obsidian.common;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.IGuiHandler;

/**
 * Common proxy for Obsidian Ingots mod.
 * @author AidanBrady
 *
 */
public abstract class CommonProxy
{
	/**
	 * Register and load client-only render information.
	 */
	public abstract void registerRenderInformation();
	
	/**
	 * Set and load the default properties for the mod.
	 */
	public abstract void setProperties();
	
	/**
	 * Load and initiate utilities for the mod.
	 */
	public abstract void loadUtilities();
	
	/**
	 * Set up and load the client-only tick handler.
	 */
	public abstract void loadTickHandler();
	
	/**
	 * Get the actual interface for a GUI.  Client-only.
	 * @param ID - gui ID
	 * @param player - player that opened the GUI
	 * @param world - world the GUI was opened in
	 * @param x - gui's x position
	 * @param y - gui's y position
	 * @param z - gui's z position
	 * @return the GuiScreen of the interface
	 */
	public abstract Object getClientGui(int ID, EntityPlayer player, World world, int x, int y, int z);
	
	/**
	 * Get the container for a GUI.  Common.
	 * @param ID - gui ID
	 * @param player - player that opened the GUI
	 * @param world - world the GUI was opened in
	 * @param x - gui's x position
	 * @param y - gui's y position
	 * @param z - gui's z position
	 * @return the Container of the interface
	 */
	public Container getServerGui(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		switch(ID)
		{
			case 21:
				TileEntityEnrichmentChamber tileentity = (TileEntityEnrichmentChamber)world.getBlockTileEntity(x, y, z);
				return new ContainerEnrichmentChamber(player.inventory, tileentity);
			case 22:
				TileEntityPlatinumCompressor tileentity1 = (TileEntityPlatinumCompressor)world.getBlockTileEntity(x, y, z);
				return new ContainerPlatinumCompressor(player.inventory, tileentity1);
			case 23:
				TileEntityCombiner tileentity2 = (TileEntityCombiner)world.getBlockTileEntity(x, y, z);
				return new ContainerCombiner(player.inventory, tileentity2);
			case 24:
				TileEntityCrusher tileentity3 = (TileEntityCrusher)world.getBlockTileEntity(x, y, z);
				return new ContainerCrusher(player.inventory, tileentity3);
		}
		return null;
	}
}
