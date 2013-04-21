package mekanism.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

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
		GameRegistry.registerTileEntity(TileEntityMetallurgicInfuser.class, "MetallurgicInfuser");
		GameRegistry.registerTileEntity(TileEntityPressurizedTube.class, "PressurizedTube");
		GameRegistry.registerTileEntity(TileEntityUniversalCable.class, "UniversalCable");
		GameRegistry.registerTileEntity(TileEntityElectricPump.class, "ElectricPump");
		GameRegistry.registerTileEntity(TileEntityElectricChest.class, "ElectricChest");
		GameRegistry.registerTileEntity(TileEntityMechanicalPipe.class, "MechanicalPipe");
	}
	
	/**
	 * Registers a client-side sound, assigned to a TileEntity.
	 * @param tileEntity - TileEntity who is registering the sound
	 */
	public void registerSound(TileEntity tileEntity) {}
	
	/**
	 * Unregisters a client-side sound, assigned to a TileEntity;
	 * @param tileEntity - TileEntity who is unregistering the sound
	 */
	public void unregisterSound(TileEntity tileEntity) {}
	
	/**
	 * Handles an ELECTRIC_CHEST_CLIENT_OPEN packet via the proxy, not handled on the server-side.
	 * @param entityplayer - player the packet was sent from
	 * @param id - the electric chest gui ID to open
	 * @param windowId - the container-specific window ID
	 * @param isBlock - if the chest is a block
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 */
	public void openElectricChest(EntityPlayer entityplayer, int id, int windowId, boolean isBlock, int x, int y, int z) {}
	
	/**
	 * Register and load client-only render information.
	 */
	public void registerRenderInformation() {}
	
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
	  	Mekanism.boundingBlockID = Mekanism.configuration.getBlock("BoundingBlock", 3005).getInt();
	  	Mekanism.gasTankID = Mekanism.configuration.getBlock("GasTank", 3006).getInt();
	  	Mekanism.transmitterID = Mekanism.configuration.getBlock("Transmitter", 3007).getInt();
	  	Mekanism.extrasEnabled = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ExtrasEnabled", true).getBoolean(true);
	  	Mekanism.osmiumGenerationEnabled = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "OsmiumGenerationEnabled", true).getBoolean(true);
	  	Mekanism.disableBCSteelCrafting = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "DisableBCSteelCrafting", false).getBoolean(true);
	  	Mekanism.disableBCBronzeCrafting = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "DisableBCBronzeCrafting", false).getBoolean(true);
	  	Mekanism.updateNotifications = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "UpdateNotifications", true).getBoolean(true);
	  	Mekanism.controlCircuitOreDict = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ControlCircuitOreDict", true).getBoolean(true);
	  	Mekanism.logPackets = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "LogPackets", false).getBoolean(false);
	  	Mekanism.obsidianTNTDelay = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ObsidianTNTDelay", 100).getInt();
	  	Mekanism.obsidianTNTBlastRadius = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ObsidianTNTBlastRadius", 12).getInt();
	  	Mekanism.FROM_IC2 = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "JoulesToEU", 10).getDouble(10);
	  	Mekanism.TO_IC2 = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EUToJoules", .1).getDouble(.1);
	  	Mekanism.FROM_BC = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "JoulesToMJ", 25).getDouble(25);
	  	Mekanism.TO_BC = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "MJToJoules", .04).getDouble(.04);
	  	Mekanism.configuration.save();
	}
	
	/**
	 * Set up and load the utilities this mod uses.
	 */
	public void loadUtilities()
	{
		TickRegistry.registerTickHandler(new CommonPlayerTickHandler(), Side.SERVER);
		TickRegistry.registerTickHandler(new CommonWorldTickHandler(), Side.SERVER);
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
				return new ContainerFactory(player.inventory, (TileEntityFactory)tileEntity);
			case 12:
				return new ContainerMetallurgicInfuser(player.inventory, (TileEntityMetallurgicInfuser)tileEntity);
			case 13:
				return new ContainerTeleporter(player.inventory, (TileEntityTeleporter)tileEntity);
			case 15:
				return new ContainerAdvancedElectricMachine(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 16:
				return new ContainerElectricMachine(player.inventory, (TileEntityElectricMachine)tileEntity);
			case 17:
				return new ContainerElectricPump(player.inventory, (TileEntityElectricPump)tileEntity);
		}
		return null;
	}
}
