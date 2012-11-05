package mekanism.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Common proxy for the Mekanism mod.
 * @author AidanBrady
 *
 */
public class CommonProxy
{
	/**
	 * Register and load client-only render information.
	 */
	public void registerRenderInformation() {}
	
	/**
	 * Gets the world the client is using from ClientProxy.
	 * @return client world
	 */
	public World getClientWorld() 
	{
		return null;
	}
	
	/**
	 * Gets the armor index number from ClientProxy.
	 * @param armor indicator
	 * @return armor index number
	 */
	public int getArmorIndex(String string) 
	{
		return 0;
	}
	
	/**
	 * Set and load the mod's common configuration properties.
	 */
	public void loadConfiguration()
	{
		Mekanism.configuration.load();
		Mekanism.multiBlockID = Mekanism.configuration.getBlock("MultiBlock", 3000).getInt();
		Mekanism.machineBlockID = Mekanism.configuration.getBlock("MachineBlock", 3001).getInt();
		Mekanism.oreBlockID = Mekanism.configuration.getBlock("OreBlock", 3002).getInt();
	  	Mekanism.obsidianTNTID = Mekanism.configuration.getBlock("ObsidianTNT", 3003).getInt();
	  	Mekanism.powerUnitID = Mekanism.configuration.getBlock("PowerUnit", 3004).getInt();
	  	Mekanism.generatorID = Mekanism.configuration.getBlock("Generator", 3005).getInt();
	  	Mekanism.extrasEnabled = Mekanism.configuration.get("ExtrasEnabled", Configuration.CATEGORY_GENERAL, true).getBoolean(true);
	  	Mekanism.oreGenerationEnabled = Mekanism.configuration.get("OreGenerationEnabled", Configuration.CATEGORY_GENERAL, true).getBoolean(true);
	  	Mekanism.configuration.save();
	}
	
	/**
	 * Load and initiate utilities for the mod's proxy.
	 */
	public void loadUtilities() {}
	
	/**
	 * Set up and load the client-only tick handler.
	 */
	public void loadTickHandler() {}
	
	/**
	 * Set up and load the sound handler.
	 */
	public void loadSoundHandler() {}
	
	/**
	 * Get the actual interface for a GUI.  Client-only.
	 * @param ID - gui ID
	 * @param player - player that opened the GUI
	 * @param world - world the GUI was opened in
	 * @param x - gui's x position
	 * @param y - gui's y position
	 * @param z - gui's z position
	 * @return the GuiScreen of the interface
	 */
	public Object getClientGui(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		return null;
	}
	
	/**
	 * Get the container for a GUI.  Common.
	 * @param ID - gui ID
	 * @param player - player that opened the GUI
	 * @param world - world the GUI was opened in
	 * @param x - gui's x position
	 * @param y - gui's y position
	 * @param z - gui's z position
	 * @return the Container of the interface
	 */
	public Container getServerGui(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		switch(ID)
		{
			case 3:
				TileEntityElectricMachine tileentity = (TileEntityElectricMachine)world.getBlockTileEntity(x, y, z);
				return new ContainerElectricMachine(player.inventory, tileentity);
			case 4:
				TileEntityAdvancedElectricMachine tileentity1 = (TileEntityAdvancedElectricMachine)world.getBlockTileEntity(x, y, z);
				return new ContainerAdvancedElectricMachine(player.inventory, tileentity1);
			case 5:
				TileEntityAdvancedElectricMachine tileentity2 = (TileEntityAdvancedElectricMachine)world.getBlockTileEntity(x, y, z);
				return new ContainerAdvancedElectricMachine(player.inventory, tileentity2);
			case 6:
				TileEntityElectricMachine tileentity3 = (TileEntityElectricMachine)world.getBlockTileEntity(x, y, z);
				return new ContainerElectricMachine(player.inventory, tileentity3);
			case 7:
				TileEntityAdvancedElectricMachine tileentity4 = (TileEntityAdvancedElectricMachine)world.getBlockTileEntity(x, y, z);
				return new ContainerAdvancedElectricMachine(player.inventory, tileentity4);
			case 8:
				TileEntityPowerUnit tileentity5 = (TileEntityPowerUnit)world.getBlockTileEntity(x, y, z);
				return new ContainerPowerUnit(player.inventory, tileentity5);
			case 9:
				TileEntityGenerator tileentity6 = (TileEntityGenerator)world.getBlockTileEntity(x, y, z);
				return new ContainerGenerator(player.inventory, tileentity6);
		}
		return null;
	}
}
