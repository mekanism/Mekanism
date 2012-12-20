package mekanism.client;

import java.io.IOException;

import mekanism.common.CommonProxy;
import mekanism.common.EntityObsidianTNT;
import mekanism.common.Mekanism;
import mekanism.common.TileEntityAdvancedElectricMachine;
import mekanism.common.TileEntityControlPanel;
import mekanism.common.TileEntityElectricMachine;
import mekanism.common.TileEntityEnergyCube;
import mekanism.common.TileEntityGasTank;
import mekanism.common.TileEntityMetallurgicInfuser;
import mekanism.common.TileEntitySmeltingFactory;
import mekanism.common.TileEntityTheoreticalElementizer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.TextureFXManager;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

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
		MinecraftForgeClient.preloadTexture("/resources/mekanism/animate/CrusherFront.png");
		MinecraftForgeClient.preloadTexture("/resources/mekanism/animate/CompressorFront.png");
		MinecraftForgeClient.preloadTexture("/resources/mekanism/animate/CombinerFront.png");
		MinecraftForgeClient.preloadTexture("/resources/mekanism/animate/ElementizerFront.png");
		MinecraftForgeClient.preloadTexture("/resources/mekanism/animate/ElementizerBack.png");
		MinecraftForgeClient.preloadTexture("/resources/mekanism/animate/ElementizerSide.png");
		MinecraftForgeClient.preloadTexture("/resources/mekanism/animate/HydrogenFront.png");
		MinecraftForgeClient.preloadTexture("/resources/mekanism/animate/HydrogenSide.png");
		
		//Register animated TextureFX
		try {
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/resources/mekanism/animate/CrusherFront.png", Mekanism.ANIMATED_TEXTURE_INDEX+1));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/resources/mekanism/animate/CompressorFront.png", Mekanism.ANIMATED_TEXTURE_INDEX+2));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/resources/mekanism/animate/CombinerFront.png", Mekanism.ANIMATED_TEXTURE_INDEX+3));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/resources/mekanism/animate/ElementizerFront.png", Mekanism.ANIMATED_TEXTURE_INDEX+4));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/resources/mekanism/animate/ElementizerBack.png", Mekanism.ANIMATED_TEXTURE_INDEX+5));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/resources/mekanism/animate/ElementizerSide.png", Mekanism.ANIMATED_TEXTURE_INDEX+6));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/resources/mekanism/animate/InfuserFront.png", Mekanism.ANIMATED_TEXTURE_INDEX+7));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/resources/mekanism/animate/InfuserSide.png", Mekanism.ANIMATED_TEXTURE_INDEX+8));
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
			case 11:
				return new GuiSmeltingFactory(player.inventory, (TileEntitySmeltingFactory)tileEntity);
			case 12:
				return new GuiMetallurgicInfuser(player.inventory, (TileEntityMetallurgicInfuser)tileEntity);
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
