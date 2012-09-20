package net.uberkat.obsidian.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.network.IGuiHandler;

/**
 * Common proxy for Obsidian Ingots mod.
 * @author AidanBrady
 *
 */
public class CommonProxy
{
	/**
	 * Register and load client-only render information.
	 */
	public void registerRenderInformation() {}
	
	/**
	 * Gets the world the client is using from ClientProxy.
	 * @return client world
	 */
	public World getClientWorld() 
	{
		return null;
	}
	
	/**
	 * Gets the armor index number from ClientProxy.
	 * @param armor indicator
	 * @return armor index number
	 */
	public int getArmorIndex(String string) 
	{
		return 0;
	}
	
	/**
	 * Set and load the mod's common configuration properties.
	 */
	public void loadConfiguration()
	{
		ObsidianIngots.configuration.load();
		ObsidianIngots.multiBlockID = ObsidianIngots.configuration.getOrCreateBlockIdProperty("MultiBlock", 200).getInt();
	  	ObsidianIngots.obsidianTNTID = ObsidianIngots.configuration.getOrCreateBlockIdProperty("ObsidianTNT", 201).getInt();
	  	ObsidianIngots.elementizerID = ObsidianIngots.configuration.getOrCreateBlockIdProperty("TheoreticalElementizer", 202).getInt();
	  	ObsidianIngots.enrichmentChamberID = ObsidianIngots.configuration.getOrCreateBlockIdProperty("EnrichmentChamber", 203).getInt();
	  	ObsidianIngots.platinumCompressorID = ObsidianIngots.configuration.getOrCreateBlockIdProperty("PlatinumCompressor", 204).getInt();
	  	ObsidianIngots.combinerID = ObsidianIngots.configuration.getOrCreateBlockIdProperty("Combiner", 205).getInt();
	  	ObsidianIngots.crusherID = ObsidianIngots.configuration.getOrCreateBlockIdProperty("Crusher", 206).getInt();
	  	ObsidianIngots.extrasEnabled = ObsidianIngots.configuration.getOrCreateBooleanProperty("ExtrasEnabled", Configuration.CATEGORY_GENERAL, true).getBoolean(true);
	  	ObsidianIngots.oreGenerationEnabled = ObsidianIngots.configuration.getOrCreateBooleanProperty("OreGenerationEnabled", Configuration.CATEGORY_GENERAL, true).getBoolean(true);
	  	ObsidianIngots.configuration.save();
	}
	
	/**
	 * Load and initiate utilities for the mod's proxy.
	 */
	public void loadUtilities() {}
	
	/**
	 * Set up and load the client-only tick handler.
	 */
	public void loadTickHandler() {}
	
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
	public Object getClientGui(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		return null;
	}
	
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
			case 25:
				TileEntityTheoreticalElementizer tileentity4 = (TileEntityTheoreticalElementizer)world.getBlockTileEntity(x, y, z);
				return new ContainerTheoreticalElementizer(player.inventory, tileentity4);
		}
		return null;
	}
}
