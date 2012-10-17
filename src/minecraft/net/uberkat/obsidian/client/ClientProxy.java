package net.uberkat.obsidian.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.TextureFXManager;
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
import net.uberkat.obsidian.common.TileEntityPowerUnit;
import net.uberkat.obsidian.common.TileEntityTheoreticalElementizer;

/**
 * Client proxy for Obsidian Ingots mod.
 * @author AidanBrady
 *
 */
public class ClientProxy extends CommonProxy
{
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
		MinecraftForgeClient.preloadTexture("/obsidian/CompressorFront.png");
		MinecraftForgeClient.preloadTexture("/obsidian/CombinerFront.png");
		MinecraftForgeClient.preloadTexture("/obsidian/ElementizerFront.png");
		MinecraftForgeClient.preloadTexture("/obsidian/ElementizerBack.png");
		MinecraftForgeClient.preloadTexture("/obsidian/ElementizerSide.png");
		
		//Register animated TextureFX for machines
		try {
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/obsidian/CompressorFront.png", ObsidianIngots.ANIMATED_TEXTURE_INDEX));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/obsidian/CombinerFront.png", ObsidianIngots.ANIMATED_TEXTURE_INDEX+1));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/obsidian/ElementizerFront.png", ObsidianIngots.ANIMATED_TEXTURE_INDEX+2));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/obsidian/ElementizerBack.png", ObsidianIngots.ANIMATED_TEXTURE_INDEX+3));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/obsidian/ElementizerSide.png", ObsidianIngots.ANIMATED_TEXTURE_INDEX+4));
		} catch (IOException e) {
			System.err.println("[ObsidianIngots] Error registering animation with FML: " + e.getMessage());
		}
		
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
			case 0:
				return new GuiStopwatch(player);
			case 1:
				return new GuiCredits();
			case 2:
				return new GuiWeatherOrb(player);
			case 3:
				TileEntityEnrichmentChamber tileentity = (TileEntityEnrichmentChamber)world.getBlockTileEntity(x, y, z);
				return new GuiElectricMachine(player.inventory, tileentity);
			case 4:
				TileEntityPlatinumCompressor tileentity1 = (TileEntityPlatinumCompressor)world.getBlockTileEntity(x, y, z);
				return new GuiAdvancedElectricMachine(player.inventory, tileentity1);
			case 5:
				TileEntityCombiner tileentity2 = (TileEntityCombiner)world.getBlockTileEntity(x, y, z);
				return new GuiAdvancedElectricMachine(player.inventory, tileentity2);
			case 6:
				TileEntityCrusher tileentity3 = (TileEntityCrusher)world.getBlockTileEntity(x, y, z);
				return new GuiElectricMachine(player.inventory, tileentity3);
			case 7:
				TileEntityTheoreticalElementizer tileentity4 = (TileEntityTheoreticalElementizer)world.getBlockTileEntity(x, y, z);
				return new GuiTheoreticalElementizer(player.inventory, tileentity4);
			case 8:
				TileEntityPowerUnit tileentity5 = (TileEntityPowerUnit)world.getBlockTileEntity(x, y, z);
				return new GuiPowerUnit(player.inventory, tileentity5);
		}
		return null;
	}
	
	public void loadTickHandler()
	{
		TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
	}
}
