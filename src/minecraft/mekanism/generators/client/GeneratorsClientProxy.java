package mekanism.generators.client;

import java.io.IOException;

import net.minecraft.src.*;

import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.client.TextureFXManager;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

import mekanism.client.TextureAnimatedFX;
import mekanism.common.Mekanism;
import mekanism.generators.common.*;

public class GeneratorsClientProxy extends GeneratorsCommonProxy
{
	public static int RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
	
	@Override
	public void registerSpecialTileEntities() 
	{
		ClientRegistry.registerTileEntity(TileEntityAdvancedSolarGenerator.class, "AdvancedSolarGenerator", new RenderAdvancedSolarGenerator(new ModelAdvancedSolarGenerator()));
		ClientRegistry.registerTileEntity(TileEntityBioGenerator.class, "BioGenerator", new RenderBioGenerator());
	}
	
	@Override
	public void registerRenderInformation()
	{
		System.out.println("[MekanismGenerators] Beginning render initiative...");
		
		//Preload block/item textures
		MinecraftForgeClient.preloadTexture("/resources/mekanism/textures/generators/items.png");
		MinecraftForgeClient.preloadTexture("/resources/mekanism/textures/generators/terrain.png");
		
		//Preload animated textures
		MinecraftForgeClient.preloadTexture("/resources/mekanism/animate/HydrogenFront.png");
		MinecraftForgeClient.preloadTexture("/resources/mekanism/animate/HydrogenSide.png");
		
		//Register animated TextureFX
		try {
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/resources/mekanism/animate/HydrogenFront.png", Mekanism.ANIMATED_TEXTURE_INDEX+5));
			TextureFXManager.instance().addAnimation(new TextureAnimatedFX("/resources/mekanism/animate/HydrogenSide.png", Mekanism.ANIMATED_TEXTURE_INDEX+6));
		} catch (IOException e) {
			System.err.println("[Mekanism] Error registering animation with FML: " + e.getMessage());
		}
		
		//Register block handler
		RenderingRegistry.registerBlockHandler(new BlockRenderingHandler());
		
		System.out.println("[MekanismGenerators] Render initiative complete.");
	}
	
	@Override
	public GuiScreen getClientGui(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		
		switch(ID)
		{
			case 0:
				return new GuiHeatGenerator(player.inventory, (TileEntityHeatGenerator)tileEntity);
			case 1:
				return new GuiSolarGenerator(player.inventory, (TileEntitySolarGenerator)tileEntity);
			case 2:
				return new GuiElectrolyticSeparator(player.inventory, (TileEntityElectrolyticSeparator)tileEntity);
			case 3:
				return new GuiHydrogenGenerator(player.inventory, (TileEntityHydrogenGenerator)tileEntity);
			case 4:
				return new GuiBioGenerator(player.inventory, (TileEntityBioGenerator)tileEntity);
		}
		return null;
	}
}
