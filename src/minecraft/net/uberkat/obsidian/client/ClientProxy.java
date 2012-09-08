package net.uberkat.obsidian.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import cpw.mods.fml.client.FMLClientHandler;
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
import net.uberkat.obsidian.common.ContainerTheoreticalElementizer;
import net.uberkat.obsidian.common.EntityKnife;
import net.uberkat.obsidian.common.EntityObsidianArrow;
import net.uberkat.obsidian.common.EntityObsidianTNT;
import net.uberkat.obsidian.common.ObsidianIngots;
import net.uberkat.obsidian.common.ObsidianUtils;
import net.uberkat.obsidian.common.TileEntityCombiner;
import net.uberkat.obsidian.common.TileEntityCrusher;
import net.uberkat.obsidian.common.TileEntityEnrichmentChamber;
import net.uberkat.obsidian.common.TileEntityPlatinumCompressor;
import net.uberkat.obsidian.common.TileEntityTheoreticalElementizer;

/**
 * Client proxy for Obsidian Ingots mod.
 * @author AidanBrady
 *
 */
public class ClientProxy extends CommonProxy
{
	@Override
	public int getArmorIndex(String string)
	{
		return RenderingRegistry.addNewArmourRendererPrefix(string);
	}
	
	public void registerRenderInformation()
	{
		System.out.println("[ObsidianIngots] Beginning render initiative...");
		//Preload block/item textures
		MinecraftForgeClient.preloadTexture("/obsidian/items.png");
		MinecraftForgeClient.preloadTexture("/obsidian/terrain.png");
		MinecraftForgeClient.preloadTexture("/obsidian/Compressor.png");
		MinecraftForgeClient.preloadTexture("/obsidian/Combiner.png");
		MinecraftForgeClient.preloadTexture("/obsidian/Elementizer.png");
		
		//Register entity rendering handlers
		RenderingRegistry.registerEntityRenderingHandler(EntityObsidianTNT.class, new RenderObsidianTNT());
		RenderingRegistry.registerEntityRenderingHandler(EntityObsidianArrow.class, new RenderObsidianArrow());
		RenderingRegistry.registerEntityRenderingHandler(EntityKnife.class, new RenderKnife());
		System.out.println("[ObsidianIngots] Render initiative complete.");
	}
	
	public World getClientWorld()
	{
		return FMLClientHandler.instance().getClient().theWorld;
	}
	
	public void loadUtilities()
	{
		System.out.println("[ObsidianIngots] Beginning utility initiative...");
		new ThreadSendData();
		System.out.println("[ObsidianIngots] Utility initiative complete.");
	}
	
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
			case 25:
				TileEntityTheoreticalElementizer tileentity4 = (TileEntityTheoreticalElementizer)world.getBlockTileEntity(x, y, z);
				return new GuiTheoreticalElementizer(player.inventory, tileentity4);
		}
		return null;
	}
	
	public void loadTickHandler()
	{
		TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
	}
}
