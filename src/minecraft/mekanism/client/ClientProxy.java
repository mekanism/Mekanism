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
import mekanism.common.EntityObsidianTNT;
import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import mekanism.common.TileEntityAdvancedElectricMachine;
import mekanism.common.TileEntityCombiner;
import mekanism.common.TileEntityControlPanel;
import mekanism.common.TileEntityCrusher;
import mekanism.common.TileEntityElectricMachine;
import mekanism.common.TileEntityEnrichmentChamber;
import mekanism.common.TileEntityGasTank;
import mekanism.common.TileEntityPlatinumCompressor;
import mekanism.common.TileEntityEnergyCube;
import mekanism.common.TileEntityTheoreticalElementizer;
import net.minecraft.client.Minecraft;
import net.minecraft.src.*;
import net.minecraftforge.client.MinecraftForgeClient;

/**
 * Client proxy for the Mekanism mod.
 * @author AidanBrady
 *
 */
public class ClientProxy extends CommonProxy
{
	public static int RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
	
	@Override
	public int getArmorIndex(String string)
	{
		return RenderingRegistry.addNewArmourRendererPrefix(string);
	}
	
	@Override
	public void registerRenderInformation()
	{
		//Preload block/item textures
		MinecraftForgeClient.preloadTexture("/resources/mekanism/textures/items.png");
		MinecraftForgeClient.preloadTexture("/resources/mekanism/textures/terrain.png");
		
		//Preload animated textures
		MinecraftForgeClient.preloadTexture("/resources/mekanism/animate/CompressorFront.png");
		MinecraftForgeClient.preloadTexture("/resources/mekanism/animate/CombinerFront.png");
		MinecraftForgeClient.preloadTexture("/resources/mekanism/animate/ElementizerFront.png");
		MinecraftForgeClient.preloadTexture("/resources/mekanism/animate/ElementizerBack.png");
		MinecraftForgeClient.preloadTexture("/resources/mekanism/animate/ElementizerSide.png");
		
		//Register animated TextureFX
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
		
		//Register item handler
		MinecraftForgeClient.registerItemRenderer(Mekanism.energyCubeID, new ItemRenderingHandler());
		
		System.out.println("[Mekanism] Render registrations complete.");
	}
	
	@Override
	public World getClientWorld()
	{
		return FMLClientHandler.instance().getClient().theWorld;
	}
	
	@Override
	public void loadUtilities()
	{
		if(FMLClientHandler.instance().getClient().gameSettings.snooperEnabled)
		{
			new ThreadSendData();
		}
		
		System.out.println("[Mekanism] Utility initiative complete.");
	}
	
	@Override
	public GuiScreen getClientGui(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		
		switch(ID)
		{
			case 0:
				return new GuiStopwatch(player);
			case 1:
				return new GuiCredits();
			case 2:
				return new GuiWeatherOrb(player);
			case 3:
				return new GuiElectricMachine(player.inventory, (TileEntityElectricMachine)tileEntity);
			case 4:
				return new GuiAdvancedElectricMachine(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 5:
				return new GuiAdvancedElectricMachine(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 6:
				return new GuiElectricMachine(player.inventory, (TileEntityElectricMachine)tileEntity);
			case 7:
				return new GuiTheoreticalElementizer(player.inventory, (TileEntityTheoreticalElementizer)tileEntity);
			case 8:
				return new GuiEnergyCube(player.inventory, (TileEntityEnergyCube)tileEntity);
			case 9:
				return new GuiControlPanel((TileEntityControlPanel)tileEntity, player, world);
			case 10:
				return new GuiGasTank(player.inventory, (TileEntityGasTank)tileEntity);
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
