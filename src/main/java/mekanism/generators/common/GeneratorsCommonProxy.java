package mekanism.generators.common;

import mekanism.common.Mekanism;
import mekanism.generators.common.inventory.container.ContainerBioGenerator;
import mekanism.generators.common.inventory.container.ContainerHeatGenerator;
import mekanism.generators.common.inventory.container.ContainerGasGenerator;
import mekanism.generators.common.inventory.container.ContainerSolarGenerator;
import mekanism.generators.common.inventory.container.ContainerWindTurbine;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindTurbine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Common proxy for the Mekanism Generators module.
 * @author AidanBrady
 *
 */
public class GeneratorsCommonProxy
{
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
		MekanismGenerators.advancedSolarGeneration = Mekanism.configuration.get("generation", "AdvancedSolarGeneration", 300D).getDouble(300D);
		MekanismGenerators.bioGeneration = Mekanism.configuration.get("generation", "BioGeneration", 350D).getDouble(350D);
		MekanismGenerators.heatGeneration = Mekanism.configuration.get("generation", "HeatGeneration", 150D).getDouble(150D);
		MekanismGenerators.heatGenerationLava = Mekanism.configuration.get("generation", "HeatGenerationLava", 5D).getDouble(5D);
		MekanismGenerators.heatGenerationNether = Mekanism.configuration.get("generation", "HeatGenerationNether", 100D).getDouble(100D);
		MekanismGenerators.solarGeneration = Mekanism.configuration.get("generation", "SolarGeneration", 50D).getDouble(50D);
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

			MekanismGenerators.windGenerationMin = Mekanism.configuration.get("generation", "WindGenerationMin", legacyWindGeneration).getDouble(legacyWindGeneration);
			MekanismGenerators.windGenerationMax = Mekanism.configuration.get("generation", "WindGenerationMax", windGenerationMax).getDouble(windGenerationMax);
		} 
		else {
			MekanismGenerators.windGenerationMin = Mekanism.configuration.get("generation", "WindGenerationMin", 60D).getDouble(60D);
			MekanismGenerators.windGenerationMax = Mekanism.configuration.get("generation", "WindGenerationMax", 480D).getDouble(480D);
		}

		//Ensure max > min to avoid division by zero later
		final int minY = Mekanism.configuration.get("generation", "WindGenerationMinY", 24).getInt(24);
		final int maxY = Mekanism.configuration.get("generation", "WindGenerationMaxY", 255).getInt(255);
		
		MekanismGenerators.windGenerationMinY = minY;
		MekanismGenerators.windGenerationMaxY = Math.max(minY + 1, maxY);
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
		}
		
		return null;
	}
}
