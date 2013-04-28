package mekanism.generators.client;

import mekanism.generators.common.GeneratorsCommonProxy;
import mekanism.generators.common.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.TileEntityBioGenerator;
import mekanism.generators.common.TileEntityElectrolyticSeparator;
import mekanism.generators.common.TileEntityHeatGenerator;
import mekanism.generators.common.TileEntityHydrogenGenerator;
import mekanism.generators.common.TileEntitySolarGenerator;
import mekanism.generators.common.TileEntityWindTurbine;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GeneratorsClientProxy extends GeneratorsCommonProxy
{
	public static int RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
	
	@Override
	public void registerSpecialTileEntities() 
	{
		ClientRegistry.registerTileEntity(TileEntityAdvancedSolarGenerator.class, "AdvancedSolarGenerator", new RenderAdvancedSolarGenerator());
		ClientRegistry.registerTileEntity(TileEntityBioGenerator.class, "BioGenerator", new RenderBioGenerator());
		ClientRegistry.registerTileEntity(TileEntityHeatGenerator.class, "HeatGenerator", new RenderHeatGenerator());
		ClientRegistry.registerTileEntity(TileEntityHydrogenGenerator.class, "HydrogenGenerator", new RenderHydrogenGenerator());
		ClientRegistry.registerTileEntity(TileEntityElectrolyticSeparator.class, "ElectrolyticSeparator", new RenderElectrolyticSeparator());
		ClientRegistry.registerTileEntity(TileEntityWindTurbine.class, "WindTurbine", new RenderWindTurbine());
	}
	
	@Override
	public void registerRenderInformation()
	{
		//Register block handler
		RenderingRegistry.registerBlockHandler(new BlockRenderingHandler());
		
		System.out.println("[MekanismGenerators] Render registrations complete.");
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
			case 5:
				return new GuiWindTurbine(player.inventory, (TileEntityWindTurbine)tileEntity);
		}
		return null;
	}
}
