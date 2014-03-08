package mekanism.generators.common;

import cpw.mods.fml.common.registry.GameRegistry;
import mekanism.common.Mekanism;
import mekanism.generators.common.inventory.container.*;
import mekanism.generators.common.tile.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

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
		GameRegistry.registerTileEntity(TileEntityHydrogenGenerator.class, "HydrogenGenerator");
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
		Mekanism.configuration.load();
		MekanismGenerators.generatorID = Mekanism.configuration.getBlock("Generator", 3010).getInt();
		MekanismGenerators.advancedSolarGeneration = Mekanism.configuration.get("generation", "AdvancedSolarGeneration", 400D).getDouble(400D);
		MekanismGenerators.bioGeneration = Mekanism.configuration.get("generation", "BioGeneration", 250D).getDouble(250D);
		MekanismGenerators.heatGeneration = Mekanism.configuration.get("generation", "HeatGeneration", 150D).getDouble(150D);
		MekanismGenerators.hydrogenGeneration = Mekanism.configuration.get("generation", "HydrogenGeneration", 400D).getDouble(400D);
		MekanismGenerators.solarGeneration = Mekanism.configuration.get("generation", "SolarGeneration", 50D).getDouble(50D);
		MekanismGenerators.windGeneration = Mekanism.configuration.get("generation", "WindGeneration", 100D).getDouble(100D);
		Mekanism.configuration.save();
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
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		switch(ID)
		{
			case 0:
				return new ContainerHeatGenerator(player.inventory, (TileEntityHeatGenerator)tileEntity);
			case 1:
				return new ContainerSolarGenerator(player.inventory, (TileEntitySolarGenerator)tileEntity);
			case 3:
				return new ContainerHydrogenGenerator(player.inventory, (TileEntityHydrogenGenerator)tileEntity);
			case 4:
				return new ContainerBioGenerator(player.inventory, (TileEntityBioGenerator)tileEntity);
			case 5:
				return new ContainerWindTurbine(player.inventory, (TileEntityWindTurbine)tileEntity);
		}
		return null;
	}
}
