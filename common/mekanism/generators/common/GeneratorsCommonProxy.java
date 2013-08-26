package mekanism.generators.common;

import mekanism.common.Mekanism;
import mekanism.generators.common.inventory.container.ContainerBioGenerator;
import mekanism.generators.common.inventory.container.ContainerElectrolyticSeparator;
import mekanism.generators.common.inventory.container.ContainerHeatGenerator;
import mekanism.generators.common.inventory.container.ContainerHydrogenGenerator;
import mekanism.generators.common.inventory.container.ContainerSolarGenerator;
import mekanism.generators.common.inventory.container.ContainerWindTurbine;
import mekanism.generators.common.tileentity.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tileentity.TileEntityBioGenerator;
import mekanism.generators.common.tileentity.TileEntityElectrolyticSeparator;
import mekanism.generators.common.tileentity.TileEntityHeatGenerator;
import mekanism.generators.common.tileentity.TileEntityHydrogenGenerator;
import mekanism.generators.common.tileentity.TileEntitySolarGenerator;
import mekanism.generators.common.tileentity.TileEntityWindTurbine;
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
		GameRegistry.registerTileEntity(TileEntityBioGenerator.class, "BioGenerator");
		GameRegistry.registerTileEntity(TileEntityHeatGenerator.class, "HeatGenerator");
		GameRegistry.registerTileEntity(TileEntityHydrogenGenerator.class, "HydrogenGenerator");
		GameRegistry.registerTileEntity(TileEntityElectrolyticSeparator.class, "ElectrolyticSeparator");
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
	  	MekanismGenerators.advancedSolarGeneration = Mekanism.configuration.get("generation", "AdvancedSolarGeneration", 180).getDouble(180);
	  	MekanismGenerators.bioGeneration = Mekanism.configuration.get("generation", "BioGeneration", 120).getDouble(120);
	  	MekanismGenerators.heatGeneration = Mekanism.configuration.get("generation", "HeatGeneration", 100).getDouble(100);
	  	MekanismGenerators.hydrogenGeneration = Mekanism.configuration.get("generation", "HydrogenGeneration", 300).getDouble(300);
	  	MekanismGenerators.solarGeneration = Mekanism.configuration.get("generation", "SolarGeneration", 30).getDouble(30);
	  	MekanismGenerators.windGeneration = Mekanism.configuration.get("generation", "WindGeneration", 30).getDouble(30);
	  	MekanismGenerators.electrolyticSeparatorUsage = Mekanism.configuration.get("usage", "ElectrolyticSeparatorUsage", 50).getDouble(50);
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
			case 2:
				return new ContainerElectrolyticSeparator(player.inventory, (TileEntityElectrolyticSeparator)tileEntity);
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
