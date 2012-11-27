package mekanism.generators.common;

import cpw.mods.fml.common.registry.GameRegistry;
import mekanism.common.ContainerAdvancedElectricMachine;
import mekanism.common.ContainerElectricMachine;
import mekanism.common.ContainerGasTank;
import mekanism.common.ContainerPowerUnit;
import mekanism.common.Mekanism;
import mekanism.common.TileEntityAdvancedElectricMachine;
import mekanism.common.TileEntityElectricMachine;
import mekanism.common.TileEntityGasTank;
import mekanism.common.TileEntityPowerUnit;
import net.minecraft.src.*;

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
	  	MekanismGenerators.generatorID = Mekanism.configuration.getBlock("Generator", 3005).getInt();
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
		}
		return null;
	}
}
