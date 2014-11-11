package mekanism.generators.client;

import mekanism.generators.client.gui.GuiBioGenerator;
import mekanism.generators.client.gui.GuiGasGenerator;
import mekanism.generators.client.gui.GuiHeatGenerator;
import mekanism.generators.client.gui.GuiSolarGenerator;
import mekanism.generators.client.gui.GuiWindTurbine;
import mekanism.generators.client.render.RenderAdvancedSolarGenerator;
import mekanism.generators.client.render.RenderBioGenerator;
import mekanism.generators.client.render.RenderGasGenerator;
import mekanism.generators.client.render.RenderHeatGenerator;
import mekanism.generators.client.render.RenderSolarGenerator;
import mekanism.generators.client.render.RenderWindTurbine;
import mekanism.generators.common.GeneratorsCommonProxy;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindTurbine;

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
	public static int GENERATOR_RENDER_ID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public void registerSpecialTileEntities()
	{
		ClientRegistry.registerTileEntity(TileEntityAdvancedSolarGenerator.class, "AdvancedSolarGenerator", new RenderAdvancedSolarGenerator());
		ClientRegistry.registerTileEntity(TileEntitySolarGenerator.class, "SolarGenerator", new RenderSolarGenerator());
		ClientRegistry.registerTileEntity(TileEntityBioGenerator.class, "BioGenerator", new RenderBioGenerator());
		ClientRegistry.registerTileEntity(TileEntityHeatGenerator.class, "HeatGenerator", new RenderHeatGenerator());
		ClientRegistry.registerTileEntity(TileEntityGasGenerator.class, "GasGenerator", new RenderGasGenerator());
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
		TileEntity tileEntity = world.getTileEntity(x, y, z);

		switch(ID)
		{
			case 0:
				return new GuiHeatGenerator(player.inventory, (TileEntityHeatGenerator)tileEntity);
			case 1:
				return new GuiSolarGenerator(player.inventory, (TileEntitySolarGenerator)tileEntity);
			case 3:
				return new GuiGasGenerator(player.inventory, (TileEntityGasGenerator)tileEntity);
			case 4:
				return new GuiBioGenerator(player.inventory, (TileEntityBioGenerator)tileEntity);
			case 5:
				return new GuiWindTurbine(player.inventory, (TileEntityWindTurbine)tileEntity);
		}
		
		return null;
	}
}
