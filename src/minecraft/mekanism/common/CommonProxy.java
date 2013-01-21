package mekanism.common;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import mekanism.client.GuiTeleporter;
import mekanism.generators.common.TileEntityAdvancedSolarGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;

/**
 * Common proxy for the Mekanism mod.
 * @author AidanBrady
 *
 */
public class CommonProxy
{
	/**
	 * Register tile entities that have special models. Overwritten in client to register TESRs.
	 */
	public void registerSpecialTileEntities() 
	{
		GameRegistry.registerTileEntity(TileEntityTheoreticalElementizer.class, "TheoreticalElementizer");
	}
	
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
	 * @param string - armor indicator
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
		Mekanism.basicBlockID = Mekanism.configuration.getBlock("BasicBlock", 3000).getInt();
		Mekanism.machineBlockID = Mekanism.configuration.getBlock("MachineBlock", 3001).getInt();
		Mekanism.oreBlockID = Mekanism.configuration.getBlock("OreBlock", 3002).getInt();
	  	Mekanism.obsidianTNTID = Mekanism.configuration.getBlock("ObsidianTNT", 3003).getInt();
	  	Mekanism.energyCubeID = Mekanism.configuration.getBlock("EnergyCube", 3004).getInt();
	  	Mekanism.nullRenderID = Mekanism.configuration.getBlock("NullRender", 3005).getInt();
	  	Mekanism.gasTankID = Mekanism.configuration.getBlock("GasTank", 3006).getInt();
	  	Mekanism.extrasEnabled = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ExtrasEnabled", true).getBoolean(true);
	  	Mekanism.platinumGenerationEnabled = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "PlatinumGenerationEnabled", true).getBoolean(true);
	  	Mekanism.disableBCSteelCrafting = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "DisableBCSteelCrafting", true).getBoolean(true);
	  	Mekanism.disableBCBronzeCrafting = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "DisableBCBronzeCrafting", true).getBoolean(true);
	  	Mekanism.configuration.save();
	}
	
	/**
	 * Load and initiate utilities for the mod's proxy.
	 */
	public void loadUtilities() {}
	
	/**
	 * Set up and load the tick handlers.
	 */
	public void loadTickHandler()
	{
		TickRegistry.registerTickHandler(new CommonTickHandler(), Side.SERVER);
	}
	
	/**
	 * Set up and load the sound handler.
	 */
	public void loadSoundHandler() {}
	
	/**
	 * Unload the sound handler.
	 */
	public void unloadSoundHandler() {}
	
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
			case 3:
				return new ContainerElectricMachine(player.inventory, (TileEntityElectricMachine)tileEntity);
			case 4:
				return new ContainerAdvancedElectricMachine(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 5:
				return new ContainerAdvancedElectricMachine(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 6:
				return new ContainerElectricMachine(player.inventory, (TileEntityElectricMachine)tileEntity);
			case 7:
				return new ContainerAdvancedElectricMachine(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 8:
				return new ContainerEnergyCube(player.inventory, (TileEntityEnergyCube)tileEntity);
			case 10:
				return new ContainerGasTank(player.inventory, (TileEntityGasTank)tileEntity);
			case 11:
				return new ContainerSmeltingFactory(player.inventory, (TileEntitySmeltingFactory)tileEntity);
			case 12:
				return new ContainerMetallurgicInfuser(player.inventory, (TileEntityMetallurgicInfuser)tileEntity);
			case 13:
				return new ContainerTeleporter(player.inventory, (TileEntityTeleporter)tileEntity);
		}
		return null;
	}
}
