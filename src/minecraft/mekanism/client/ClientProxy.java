package mekanism.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.TextureFXManager;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.TickRegistry;
import mekanism.common.CommonProxy;
import mekanism.common.EntityKnife;
import mekanism.common.EntityObsidianTNT;
import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import mekanism.common.TileEntityCombiner;
import mekanism.common.TileEntityCrusher;
import mekanism.common.TileEntityEnrichmentChamber;
import mekanism.common.TileEntityGenerator;
import mekanism.common.TileEntityPlatinumCompressor;
import mekanism.common.TileEntityPowerUnit;
import mekanism.common.TileEntityTheoreticalElementizer;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.World;
import net.minecraftforge.client.MinecraftForgeClient;

/**
 * Client proxy for the Mekanism mod.
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
	
	@Override
	public void registerRenderInformation()
	{
		System.out.println("[Mekanism] Beginning render initiative...");
		
		//Preload block/item textures
		MinecraftForgeClient.preloadTexture("/resources/mekanism/textures/items.png");
		MinecraftForgeClient.preloadTexture("/resources/mekanism/textures/terrain.png");
		
		//Register animated TextureFX for machines
		try {
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/resources/mekanism/animate/CompressorFront.png", Mekanism.ANIMATED_TEXTURE_INDEX));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/resources/mekanism/animate/CombinerFront.png", Mekanism.ANIMATED_TEXTURE_INDEX+1));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/resources/mekanism/animate/ElementizerFront.png", Mekanism.ANIMATED_TEXTURE_INDEX+2));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/resources/mekanism/animate/ElementizerBack.png", Mekanism.ANIMATED_TEXTURE_INDEX+3));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/resources/mekanism/animate/ElementizerSide.png", Mekanism.ANIMATED_TEXTURE_INDEX+4));
		} catch (IOException e) {
			System.err.println("[Mekanism] Error registering animation with FML: " + e.getMessage());
		}
		
		//Register entity rendering handlers
		RenderingRegistry.registerEntityRenderingHandler(EntityObsidianTNT.class, new RenderObsidianTNT());
		RenderingRegistry.registerEntityRenderingHandler(EntityKnife.class, new RenderKnife());
		
		System.out.println("[Mekanism] Render initiative complete.");
	}
	
	@Override
	public World getClientWorld()
	{
		return FMLClientHandler.instance().getClient().theWorld;
	}
	
	@Override
	public void loadUtilities()
	{
		System.out.println("[Mekanism] Beginning utility initiative...");
		new ThreadSendData();
		System.out.println("[Mekanism] Utility initiative complete.");
	}
	
	@Override
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
			case 9:
				TileEntityGenerator tileentity6 = (TileEntityGenerator)world.getBlockTileEntity(x, y, z);
				return new GuiGenerator(player.inventory, tileentity6);
			case 10:
				return new GuiControlPanel(player, world);
		}
		return null;
	}
	
	@Override
	public void loadTickHandler()
	{
		TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
	}
	
	@Override
	public void loadSoundHandler()
	{
		Mekanism.audioHandler = new SoundHandler();
	}
}
