package mekanism.generators.common;

import mekanism.api.MekanismConfig.generators;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.generators.common.inventory.container.ContainerBioGenerator;
import mekanism.generators.common.inventory.container.ContainerGasGenerator;
import mekanism.generators.common.inventory.container.ContainerHeatGenerator;
import mekanism.generators.common.inventory.container.ContainerNeutronCapture;
import mekanism.generators.common.inventory.container.ContainerReactorController;
import mekanism.generators.common.inventory.container.ContainerSolarGenerator;
import mekanism.generators.common.inventory.container.ContainerWindTurbine;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindTurbine;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorFrame;
import mekanism.generators.common.tile.reactor.TileEntityReactorGlass;
import mekanism.generators.common.tile.reactor.TileEntityReactorLaserFocusMatrix;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorNeutronCapture;
import mekanism.generators.common.tile.reactor.TileEntityReactorPort;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Common proxy for the Mekanism Generators module.
 * @author AidanBrady
 *
 */
public class GeneratorsCommonProxy
{
	public static int GENERATOR_RENDER_ID = RenderingRegistry.getNextAvailableRenderId();

	/**
	 * Register normal tile entities
	 */
	public void registerRegularTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntityReactorFrame.class, "ReactorFrame");
		GameRegistry.registerTileEntity(TileEntityReactorGlass.class, "ReactorGlass");
		GameRegistry.registerTileEntity(TileEntityReactorLaserFocusMatrix.class, "ReactorLaserFocus");
		GameRegistry.registerTileEntity(TileEntityReactorNeutronCapture.class, "ReactorNeutronCapture");
		GameRegistry.registerTileEntity(TileEntityReactorPort.class, "ReactorPort");
		GameRegistry.registerTileEntity(TileEntityReactorLogicAdapter.class, "ReactorLogicAdapter");
	}

	/**
	 * Register tile entities that have special models. Overwritten in client to register TESRs.
	 */
	public void registerSpecialTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntityAdvancedSolarGenerator.class, "AdvancedSolarGenerator");
		GameRegistry.registerTileEntity(TileEntitySolarGenerator.class, "SolarGenerator");
		GameRegistry.registerTileEntity(TileEntityBioGenerator.class, "BioGenerator");
		GameRegistry.registerTileEntity(TileEntityHeatGenerator.class, "HeatGenerator");
		GameRegistry.registerTileEntity(TileEntityGasGenerator.class, "GasGenerator");
		GameRegistry.registerTileEntity(TileEntityWindTurbine.class, "WindTurbine");
		GameRegistry.registerTileEntity(TileEntityReactorController.class, "ReactorController");
	}

	/**
	 * Register and load client-only render information.
	 */
	public void registerRenderInformation() {}

	/**
	 * Set and load the mod's common configuration properties.
	 */
	public void loadConfiguration()
	{
		generators.advancedSolarGeneration = Mekanism.configuration.get("generation", "AdvancedSolarGeneration", 300D).getDouble(300D);
		generators.bioGeneration = Mekanism.configuration.get("generation", "BioGeneration", 350D).getDouble(350D);
		generators.heatGeneration = Mekanism.configuration.get("generation", "HeatGeneration", 150D).getDouble(150D);
		generators.heatGenerationLava = Mekanism.configuration.get("generation", "HeatGenerationLava", 5D).getDouble(5D);
		generators.heatGenerationNether = Mekanism.configuration.get("generation", "HeatGenerationNether", 100D).getDouble(100D);
		generators.solarGeneration = Mekanism.configuration.get("generation", "SolarGeneration", 50D).getDouble(50D);
		
		loadWindConfiguration();

		if(Mekanism.configuration.hasChanged())
		{
			Mekanism.configuration.save();
		}
	}
	
	private void loadWindConfiguration() 
	{
		if(Mekanism.configuration.hasKey("generation", "WindGeneration")) 
		{
			//Migrate the old wind generation config
			final double legacyWindGeneration = Mekanism.configuration.get("generation", "WindGeneration", 60D).getDouble(60D);
			final double windGenerationMax = legacyWindGeneration * 8D;
			Mekanism.configuration.getCategory("generation").remove("WindGeneration");

			generators.windGenerationMin = Mekanism.configuration.get("generation", "WindGenerationMin", legacyWindGeneration).getDouble(legacyWindGeneration);
			generators.windGenerationMax = Mekanism.configuration.get("generation", "WindGenerationMax", windGenerationMax).getDouble(windGenerationMax);
		} 
		else {
			generators.windGenerationMin = Mekanism.configuration.get("generation", "WindGenerationMin", 60D).getDouble(60D);
			generators.windGenerationMax = Mekanism.configuration.get("generation", "WindGenerationMax", 480D).getDouble(480D);
		}

		//Ensure max > min to avoid division by zero later
		final int minY = Mekanism.configuration.get("generation", "WindGenerationMinY", 24).getInt(24);
		final int maxY = Mekanism.configuration.get("generation", "WindGenerationMaxY", 255).getInt(255);

		generators.windGenerationMinY = minY;
		generators.windGenerationMaxY = Math.max(minY + 1, maxY);
	}

	/**
	 * Get the actual interface for a GUI. Client-only.
	 * @param ID - gui ID
	 * @param player - player that opened the GUI
	 * @param world - world the GUI was opened in
	 * @param x - gui's x position
	 * @param y - gui's y position
	 * @param z - gui's z position
	 * @return the GuiScreen of the GUI
	 */
	public Object getClientGui(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	/**
	 * Get the container for a GUI. Common.
	 * @param ID - gui ID
	 * @param player - player that opened the GUI
	 * @param world - world the GUI was opened in
	 * @param x - gui's x position
	 * @param y - gui's y position
	 * @param z - gui's z position
	 * @return the Container of the GUI
	 */
	public Container getServerGui(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);

		switch(ID)
		{
			case 0:
				return new ContainerHeatGenerator(player.inventory, (TileEntityHeatGenerator)tileEntity);
			case 1:
				return new ContainerSolarGenerator(player.inventory, (TileEntitySolarGenerator)tileEntity);
			case 3:
				return new ContainerGasGenerator(player.inventory, (TileEntityGasGenerator)tileEntity);
			case 4:
				return new ContainerBioGenerator(player.inventory, (TileEntityBioGenerator)tileEntity);
			case 5:
				return new ContainerWindTurbine(player.inventory, (TileEntityWindTurbine)tileEntity);
			case 10:
				return new ContainerReactorController(player.inventory, (TileEntityReactorController)tileEntity);
			case 11:
			case 12:
			case 13:
				return new ContainerNull(player, (TileEntityContainerBlock)tileEntity);
			case 14:
				return new ContainerNeutronCapture(player.inventory, (TileEntityReactorNeutronCapture)tileEntity);
			case 15:
				return new ContainerNull(player, (TileEntityContainerBlock)tileEntity);
		}
		
		return null;
	}
}
