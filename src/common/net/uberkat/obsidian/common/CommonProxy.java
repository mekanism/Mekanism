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
	public void registerRenderInformation()
	{
		
	}
	
	public int getArmorIndex(String string)
	{
		return 0;
	}
	
	/**
	 * Set and load the mod's common properties.
	 */
	public void setProperties()
	{
		Properties properties = new Properties();
		try
		{
			File config = ObsidianIngots.configuration;
			if(config.exists())
			{
				properties.load(new FileInputStream(config));
				ObsidianIngots.platinumOreID = Integer.parseInt(properties.getProperty("platinumOreID"));
			  	ObsidianIngots.platinumBlockID = Integer.parseInt(properties.getProperty("platinumBlockID"));
			  	ObsidianIngots.redstoneBlockID = Integer.parseInt(properties.getProperty("redstoneBlockID"));
			  	ObsidianIngots.obsidianTNTID = Integer.parseInt(properties.getProperty("obsidianTNTID"));
			  	ObsidianIngots.refinedObsidianID = Integer.parseInt(properties.getProperty("refinedObsidianID"));
			  	ObsidianIngots.elementizerID = Integer.parseInt(properties.getProperty("elementizerID"));
			  	ObsidianIngots.enrichmentChamberID = Integer.parseInt(properties.getProperty("enrichmentChamberID"));
			  	ObsidianIngots.platinumCompressorID = Integer.parseInt(properties.getProperty("platinumCompressorID"));
			  	ObsidianIngots.combinerID = Integer.parseInt(properties.getProperty("combinerID"));
			  	ObsidianIngots.crusherID = Integer.parseInt(properties.getProperty("crusherID"));
			  	ObsidianIngots.coalBlockID = Integer.parseInt(properties.getProperty("coalBlockID"));
			  	ObsidianIngots.refinedGlowstoneID = Integer.parseInt(properties.getProperty("refinedGlowstoneID"));
			  	ObsidianIngots.extrasEnabled = Boolean.parseBoolean(properties.getProperty("extrasEnabled"));
			  	ObsidianIngots.oreGenerationEnabled = Boolean.parseBoolean(properties.getProperty("oreGenerationEnabled"));
			  	ObsidianIngots.logger.info("[ObsidianIngots] Data loaded.");
			}
			else {
				config.createNewFile();
				ObsidianIngots.logger.info("[ObsidianIngots] Created 'ObsidianIngots.txt' configuration file.");
				FileOutputStream fileoutputstream = new FileOutputStream(config);
				properties.setProperty("platinumOreID", Integer.toString(200));
				properties.setProperty("platinumBlockID", Integer.toString(201));
				properties.setProperty("redstoneBlockID", Integer.toString(202));
				properties.setProperty("obsidianTNTID", Integer.toString(203));
				properties.setProperty("refinedObsidianID", Integer.toString(204));
				properties.setProperty("elementizerID", Integer.toString(205));
				properties.setProperty("enrichmentChamberID", Integer.toString(206));
				properties.setProperty("platinumCompressorID", Integer.toString(207));
				properties.setProperty("combinerID", Integer.toString(208));
				properties.setProperty("crusherID", Integer.toString(209));
				properties.setProperty("coalBlockID", Integer.toString(210));
				properties.setProperty("refinedGlowstoneID", Integer.toString(211));
				properties.setProperty("extrasEnabled", Boolean.toString(true));
				properties.setProperty("oreGenerationEnabled", Boolean.toString(true));
				properties.store(fileoutputstream, "Official Obsidian Ingots Configuration.");
				fileoutputstream.close();
			}
			
		} catch (IOException ioexception)
	  	{
			System.err.println("[ObsidianIngots] An error occured while reading from configuration file.");
		  	ioexception.printStackTrace();
	  	}
	}
	
	/**
	 * Load and initiate utilities for the mod.
	 */
	public void loadUtilities()
	{
		
	}
	
	/**
	 * Set up and load the client-only tick handler.
	 */
	public void loadTickHandler()
	{
		
	}
	
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
