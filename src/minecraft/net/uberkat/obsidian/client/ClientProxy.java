package net.uberkat.obsidian.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.TickRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.uberkat.obsidian.common.CommonProxy;
import net.uberkat.obsidian.common.EntityKnife;
import net.uberkat.obsidian.common.EntityObsidianArrow;
import net.uberkat.obsidian.common.EntityObsidianTNT;
import net.uberkat.obsidian.common.ObsidianIngots;
import net.uberkat.obsidian.common.ObsidianUtils;
import net.uberkat.obsidian.common.TileEntityCombiner;
import net.uberkat.obsidian.common.TileEntityCrusher;
import net.uberkat.obsidian.common.TileEntityEnrichmentChamber;
import net.uberkat.obsidian.common.TileEntityPlatinumCompressor;

/**
 * Client proxy for Obsidian Ingots mod.
 * @author AidanBrady
 *
 */
public class ClientProxy extends CommonProxy
{
	@Override
	public void registerRenderInformation()
	{
		System.out.println("[ObsidianIngots] Beginning render initiative...");
		//Preload block/item textures
		MinecraftForgeClient.preloadTexture("/obsidian/items.png");
		MinecraftForgeClient.preloadTexture("/obsidian/terrain.png");
		MinecraftForgeClient.preloadTexture("/obsidian/Compressor.png");
		MinecraftForgeClient.preloadTexture("/obsidian/Combiner.png");
		
		//Register entity rendering handlers
		RenderingRegistry.registerEntityRenderingHandler(EntityObsidianTNT.class, new RenderObsidianTNT());
		RenderingRegistry.registerEntityRenderingHandler(EntityObsidianArrow.class, new RenderObsidianArrow());
		RenderingRegistry.registerEntityRenderingHandler(EntityKnife.class, new RenderKnife());
		System.out.println("[ObsidianIngots] Render initiative complete.");
	}
	
	public void setProperties()
	{
		Properties properties = new Properties();
		try
		{
			File config = new File(new StringBuilder().append(Minecraft.getMinecraftDir()).append("/config/ObsidianIngots.txt").toString());
			if(config.exists())
			{
				properties.load(new FileInputStream(config));
				ObsidianIngots.platinumOreID = Integer.parseInt(properties.getProperty("platinumOreID"));
			  	ObsidianIngots.platinumBlockID = Integer.parseInt(properties.getProperty("platinumBlockID"));
			  	ObsidianIngots.redstoneBlockID = Integer.parseInt(properties.getProperty("redstoneBlockID"));
			  	ObsidianIngots.obsidianTNTID = Integer.parseInt(properties.getProperty("obsidianTNTID"));
			  	ObsidianIngots.refinedObsidianID = Integer.parseInt(properties.getProperty("refinedObsidianID"));
			  	ObsidianIngots.lifeBlockID = Integer.parseInt(properties.getProperty("lifeBlockID"));
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
				properties.setProperty("lifeBlockID", Integer.toString(205));
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
	
	public void loadUtilities()
	{
		System.out.println("[ObsidianIngots] Beginning utility initiative...");
		ObsidianIngots.latestVersionNumber = ObsidianUtils.getLatestVersion();
		ObsidianIngots.recentNews = ObsidianUtils.getRecentNews();
		ObsidianUtils.sendServerData();
		System.out.println("[ObsidianIngots] Utility initiative complete.");
	}
	
	@Override
	public GuiScreen getClientGui(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		switch(ID)
		{
			case 18:
				return new GuiStopwatch(player);
			case 19:
				return new GuiCredits();
			case 20:
				return new GuiWeatherOrb(player);
			case 21:
				TileEntityEnrichmentChamber tileentity = (TileEntityEnrichmentChamber)world.getBlockTileEntity(x, y, z);
				return new GuiEnrichmentChamber(player.inventory, tileentity);
			case 22:
				TileEntityPlatinumCompressor tileentity1 = (TileEntityPlatinumCompressor)world.getBlockTileEntity(x, y, z);
				return new GuiPlatinumCompressor(player.inventory, tileentity1);
			case 23:
				TileEntityCombiner tileentity2 = (TileEntityCombiner)world.getBlockTileEntity(x, y, z);
				return new GuiCombiner(player.inventory, tileentity2);
			case 24:
				TileEntityCrusher tileentity3 = (TileEntityCrusher)world.getBlockTileEntity(x, y, z);
				return new GuiCrusher(player.inventory, tileentity3);
		}
		return null;
	}
	
	public void loadTickHandler()
	{
		TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
	}
}
