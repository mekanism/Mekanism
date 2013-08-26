package mekanism.common;

import java.io.File;

import mekanism.common.inventory.container.ContainerAdvancedElectricMachine;
import mekanism.common.inventory.container.ContainerDynamicTank;
import mekanism.common.inventory.container.ContainerElectricMachine;
import mekanism.common.inventory.container.ContainerElectricPump;
import mekanism.common.inventory.container.ContainerEnergyCube;
import mekanism.common.inventory.container.ContainerFactory;
import mekanism.common.inventory.container.ContainerGasTank;
import mekanism.common.inventory.container.ContainerMetallurgicInfuser;
import mekanism.common.inventory.container.ContainerRobitCrafting;
import mekanism.common.inventory.container.ContainerRobitInventory;
import mekanism.common.inventory.container.ContainerRobitMain;
import mekanism.common.inventory.container.ContainerRobitRepair;
import mekanism.common.inventory.container.ContainerRobitSmelting;
import mekanism.common.inventory.container.ContainerTeleporter;
import mekanism.common.tileentity.TileEntityAdvancedElectricMachine;
import mekanism.common.tileentity.TileEntityAdvancedFactory;
import mekanism.common.tileentity.TileEntityChargepad;
import mekanism.common.tileentity.TileEntityCombiner;
import mekanism.common.tileentity.TileEntityCrusher;
import mekanism.common.tileentity.TileEntityDynamicTank;
import mekanism.common.tileentity.TileEntityDynamicValve;
import mekanism.common.tileentity.TileEntityElectricChest;
import mekanism.common.tileentity.TileEntityElectricMachine;
import mekanism.common.tileentity.TileEntityElectricPump;
import mekanism.common.tileentity.TileEntityEliteFactory;
import mekanism.common.tileentity.TileEntityEnergizedSmelter;
import mekanism.common.tileentity.TileEntityEnergyCube;
import mekanism.common.tileentity.TileEntityEnrichmentChamber;
import mekanism.common.tileentity.TileEntityFactory;
import mekanism.common.tileentity.TileEntityGasTank;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.tileentity.TileEntityMechanicalPipe;
import mekanism.common.tileentity.TileEntityMetallurgicInfuser;
import mekanism.common.tileentity.TileEntityOsmiumCompressor;
import mekanism.common.tileentity.TileEntityPressurizedTube;
import mekanism.common.tileentity.TileEntityPurificationChamber;
import mekanism.common.tileentity.TileEntityTeleporter;
import mekanism.common.tileentity.TileEntityTheoreticalElementizer;
import mekanism.common.tileentity.TileEntityUniversalCable;
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
		GameRegistry.registerTileEntity(TileEntityEnrichmentChamber.class, "EnrichmentChamber");
		GameRegistry.registerTileEntity(TileEntityOsmiumCompressor.class, "OsmiumCompressor");
		GameRegistry.registerTileEntity(TileEntityCombiner.class, "Combiner");
		GameRegistry.registerTileEntity(TileEntityCrusher.class, "Crusher");
		GameRegistry.registerTileEntity(TileEntityFactory.class, "SmeltingFactory");
		GameRegistry.registerTileEntity(TileEntityAdvancedFactory.class, "AdvancedSmeltingFactory");
		GameRegistry.registerTileEntity(TileEntityEliteFactory.class, "UltimateSmeltingFactory");
		GameRegistry.registerTileEntity(TileEntityPurificationChamber.class, "PurificationChamber");
		GameRegistry.registerTileEntity(TileEntityEnergizedSmelter.class, "EnergizedSmelter");
		GameRegistry.registerTileEntity(TileEntityTheoreticalElementizer.class, "TheoreticalElementizer");
		GameRegistry.registerTileEntity(TileEntityMetallurgicInfuser.class, "MetallurgicInfuser");
		GameRegistry.registerTileEntity(TileEntityPressurizedTube.class, "PressurizedTube");
		GameRegistry.registerTileEntity(TileEntityUniversalCable.class, "UniversalCable");
		GameRegistry.registerTileEntity(TileEntityElectricPump.class, "ElectricPump");
		GameRegistry.registerTileEntity(TileEntityElectricChest.class, "ElectricChest");
		GameRegistry.registerTileEntity(TileEntityMechanicalPipe.class, "MechanicalPipe");
		GameRegistry.registerTileEntity(TileEntityDynamicTank.class, "DynamicTank");
		GameRegistry.registerTileEntity(TileEntityDynamicValve.class, "DynamicValve");
		GameRegistry.registerTileEntity(TileEntityChargepad.class, "Chargepad");
		GameRegistry.registerTileEntity(TileEntityLogisticalTransporter.class, "LogisticalTransporter");
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
	  	Mekanism.logPackets = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "LogPackets", false).getBoolean(true);
	  	Mekanism.dynamicTankEasterEgg = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "DynamicTankEasterEgg", false).getBoolean(true);
	  	Mekanism.obsidianTNTDelay = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ObsidianTNTDelay", 100).getInt();
	  	Mekanism.obsidianTNTBlastRadius = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ObsidianTNTBlastRadius", 12).getInt();
	  	Mekanism.UPDATE_DELAY = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ClientUpdateDelay", 10).getInt();
	  	Mekanism.FROM_IC2 = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "JoulesToEU", 10).getDouble(10);
	  	Mekanism.TO_IC2 = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EUToJoules", .1).getDouble(.1);
	  	Mekanism.FROM_BC = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "JoulesToMJ", 25).getDouble(25);
	  	Mekanism.TO_BC = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "MJToJoules", .04).getDouble(.04);
	  	Mekanism.ENERGY_PER_REDSTONE = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EnergyPerRedstone", 10000).getDouble(10000);
	  	
		Mekanism.enrichmentChamberUsage = Mekanism.configuration.get("usage", "EnrichmentChamberUsage", 50).getDouble(50);
		Mekanism.osmiumCompressorUsage = Mekanism.configuration.get("usage", "OsmiumCompressorUsage", 50).getDouble(50);
		Mekanism.combinerUsage = Mekanism.configuration.get("usage", "CombinerUsage", 50).getDouble(50);
		Mekanism.crusherUsage = Mekanism.configuration.get("usage", "CrusherUsage", 50).getDouble(50);
		Mekanism.theoreticalElementizerUsage = Mekanism.configuration.get("usage", "TheoreticalElementizerUsage", 80).getDouble(80);
		Mekanism.factoryUsage = Mekanism.configuration.get("usage", "FactoryUsage", 50).getDouble(50);
		Mekanism.metallurgicInfuserUsage = Mekanism.configuration.get("usage", "MetallurgicInfuserUsage", 50).getDouble(50);
		Mekanism.purificationChamberUsage = Mekanism.configuration.get("usage", "PurificationChamberUsage", 50).getDouble(50);
		Mekanism.energizedSmelterUsage = Mekanism.configuration.get("usage", "EnergizedSmelterUsage", 50).getDouble(50);
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
	 * Whether or not the game is paused. 
	 */
	public boolean isPaused() 
	{
		return false;
	}
	
	/**
	 * Does the Dynamic Tank creation animation, starting from the rendering block.
	 */
	public void doTankAnimation(TileEntityDynamicTank tileEntity) {}
	
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
			case 18:
				return new ContainerDynamicTank(player.inventory, (TileEntityDynamicTank)tileEntity);
			case 21:
				EntityRobit robit = (EntityRobit)world.getEntityByID(x);
				if(robit != null)
				{
					return new ContainerRobitMain(player.inventory, robit);
				}
			case 22:
				return new ContainerRobitCrafting(player.inventory, world);
			case 23:
				EntityRobit robit1 = (EntityRobit)world.getEntityByID(x);
				if(robit1 != null)
				{
					return new ContainerRobitInventory(player.inventory, robit1);
				}
			case 24:
				EntityRobit robit2 = (EntityRobit)world.getEntityByID(x);
				if(robit2 != null)
				{
					return new ContainerRobitSmelting(player.inventory, robit2);
				}
			case 25:
				return new ContainerRobitRepair(player.inventory, world);
		}
		
		return null;
	}
	
	/**
	 * Gets the Minecraft base directory.
	 * @return base directory
	 */
	public File getMinecraftDir()
	{
		return null;
	}
}
