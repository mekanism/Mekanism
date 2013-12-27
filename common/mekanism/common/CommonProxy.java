package mekanism.common;

import java.io.File;

import mekanism.common.inventory.container.ContainerAdvancedElectricMachine;
import mekanism.common.inventory.container.ContainerChemicalFormulator;
import mekanism.common.inventory.container.ContainerChemicalInfuser;
import mekanism.common.inventory.container.ContainerDictionary;
import mekanism.common.inventory.container.ContainerDigitalMiner;
import mekanism.common.inventory.container.ContainerDynamicTank;
import mekanism.common.inventory.container.ContainerElectricMachine;
import mekanism.common.inventory.container.ContainerElectricPump;
import mekanism.common.inventory.container.ContainerEnergyCube;
import mekanism.common.inventory.container.ContainerFactory;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerGasTank;
import mekanism.common.inventory.container.ContainerMetallurgicInfuser;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.inventory.container.ContainerRobitCrafting;
import mekanism.common.inventory.container.ContainerRobitInventory;
import mekanism.common.inventory.container.ContainerRobitMain;
import mekanism.common.inventory.container.ContainerRobitRepair;
import mekanism.common.inventory.container.ContainerRobitSmelting;
import mekanism.common.inventory.container.ContainerRotaryCondensentrator;
import mekanism.common.inventory.container.ContainerTeleporter;
import mekanism.common.tileentity.TileEntityAdvancedElectricMachine;
import mekanism.common.tileentity.TileEntityAdvancedFactory;
import mekanism.common.tileentity.TileEntityBin;
import mekanism.common.tileentity.TileEntityChargepad;
import mekanism.common.tileentity.TileEntityChemicalFormulator;
import mekanism.common.tileentity.TileEntityChemicalInfuser;
import mekanism.common.tileentity.TileEntityChemicalInjectionChamber;
import mekanism.common.tileentity.TileEntityCombiner;
import mekanism.common.tileentity.TileEntityContainerBlock;
import mekanism.common.tileentity.TileEntityCrusher;
import mekanism.common.tileentity.TileEntityDigitalMiner;
import mekanism.common.tileentity.TileEntityDiversionTransporter;
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
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.tileentity.TileEntityMechanicalPipe;
import mekanism.common.tileentity.TileEntityMetallurgicInfuser;
import mekanism.common.tileentity.TileEntityObsidianTNT;
import mekanism.common.tileentity.TileEntityOsmiumCompressor;
import mekanism.common.tileentity.TileEntityPressurizedTube;
import mekanism.common.tileentity.TileEntityPurificationChamber;
import mekanism.common.tileentity.TileEntityRotaryCondensentrator;
import mekanism.common.tileentity.TileEntityTeleporter;
import mekanism.common.tileentity.TileEntityUniversalCable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
		GameRegistry.registerTileEntity(TileEntityMetallurgicInfuser.class, "MetallurgicInfuser");
		GameRegistry.registerTileEntity(TileEntityGasTank.class, "GasTank");
		GameRegistry.registerTileEntity(TileEntityEnergyCube.class, "EnergyCube");
		GameRegistry.registerTileEntity(TileEntityPressurizedTube.class, "PressurizedTube");
		GameRegistry.registerTileEntity(TileEntityUniversalCable.class, "UniversalCable");
		GameRegistry.registerTileEntity(TileEntityElectricPump.class, "ElectricPump");
		GameRegistry.registerTileEntity(TileEntityElectricChest.class, "ElectricChest");
		GameRegistry.registerTileEntity(TileEntityMechanicalPipe.class, "MechanicalPipe");
		GameRegistry.registerTileEntity(TileEntityDynamicTank.class, "DynamicTank");
		GameRegistry.registerTileEntity(TileEntityDynamicValve.class, "DynamicValve");
		GameRegistry.registerTileEntity(TileEntityChargepad.class, "Chargepad");
		GameRegistry.registerTileEntity(TileEntityLogisticalTransporter.class, "LogisticalTransporter");
		GameRegistry.registerTileEntity(TileEntityDiversionTransporter.class, "DiversionTransporter");
		GameRegistry.registerTileEntity(TileEntityLogisticalSorter.class, "LogisticalSorter");
		GameRegistry.registerTileEntity(TileEntityBin.class, "Bin");
		GameRegistry.registerTileEntity(TileEntityDigitalMiner.class, "DigitalMiner");
		GameRegistry.registerTileEntity(TileEntityObsidianTNT.class, "ObsidianTNT");
		GameRegistry.registerTileEntity(TileEntityRotaryCondensentrator.class, "RotaryCondensentrator");
		GameRegistry.registerTileEntity(TileEntityTeleporter.class, "MekanismTeleporter");
		GameRegistry.registerTileEntity(TileEntityChemicalFormulator.class, "ChemicalFormulator");
		GameRegistry.registerTileEntity(TileEntityChemicalInfuser.class, "ChemicalInfuser");
		GameRegistry.registerTileEntity(TileEntityChemicalInjectionChamber.class, "ChemicalInjectionChamber");
	}
	
	/**
	 * Registers a client-side sound, assigned to a TileEntity.
	 * @param tileEntity - TileEntity who is registering the sound
	 */
	public void registerSound(Object obj) {}
	
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
		Mekanism.machineBlock2ID = Mekanism.configuration.getBlock("MachineBlock2", 3008).getInt();
	  	
	  	Mekanism.osmiumGenerationEnabled = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "OsmiumGenerationEnabled", true).getBoolean(true);
	  	Mekanism.copperGenerationEnabled = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "CopperGenerationEnabled", true).getBoolean(true);
	  	Mekanism.tinGenerationEnabled = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "TinGenerationEnabled", true).getBoolean(true);
	  	Mekanism.disableBCSteelCrafting = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "DisableBCSteelCrafting", false).getBoolean(true);
	  	Mekanism.disableBCBronzeCrafting = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "DisableBCBronzeCrafting", false).getBoolean(true);
	  	Mekanism.updateNotifications = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "UpdateNotifications", true).getBoolean(true);
	  	Mekanism.controlCircuitOreDict = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ControlCircuitOreDict", true).getBoolean(true);
	  	Mekanism.logPackets = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "LogPackets", false).getBoolean(true);
	  	Mekanism.dynamicTankEasterEgg = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "DynamicTankEasterEgg", false).getBoolean(true);
	  	Mekanism.voiceServerEnabled = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "VoiceServerEnabled", true).getBoolean(true);
	  	Mekanism.forceBuildcraft = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ForceBuildcraftPower", false).getBoolean(false);
	  	Mekanism.obsidianTNTDelay = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ObsidianTNTDelay", 100).getInt();
	  	Mekanism.obsidianTNTBlastRadius = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ObsidianTNTBlastRadius", 12).getInt();
	  	Mekanism.UPDATE_DELAY = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ClientUpdateDelay", 10).getInt();
	  	Mekanism.osmiumGenerationAmount = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "OsmiumGenerationAmount", 12).getInt();
	  	Mekanism.copperGenerationAmount = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "CopperGenerationAmount", 16).getInt();
	  	Mekanism.tinGenerationAmount = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "TinGenerationAmount", 14).getInt();
	  	Mekanism.FROM_IC2 = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "JoulesToEU", 10D).getDouble(10D);
	  	Mekanism.TO_IC2 = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EUToJoules", .1D).getDouble(.1D);
	  	Mekanism.FROM_BC = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "JoulesToMJ", 25D).getDouble(25D);
	  	Mekanism.TO_BC = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "MJToJoules", .04D).getDouble(.04D);
	  	Mekanism.ENERGY_PER_REDSTONE = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EnergyPerRedstone", 10000D).getDouble(10000D);
	  	Mekanism.VOICE_PORT = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "VoicePort", 36123).getInt();
		//If this is less than 1, upgrades make machines worse. If less than 0, I don't even know.
	  	Mekanism.maxUpgradeMultiplier = Math.max(1, Mekanism.configuration.get(Configuration.CATEGORY_GENERAL,"UpgradeModifier", 10).getInt());
	  	
	  	Mekanism.TO_TE = Mekanism.TO_BC*10;
	  	Mekanism.FROM_TE = Mekanism.FROM_BC/10;
	  	
		Mekanism.enrichmentChamberUsage = Mekanism.configuration.get("usage", "EnrichmentChamberUsage", 50D).getDouble(50D);
		Mekanism.osmiumCompressorUsage = Mekanism.configuration.get("usage", "OsmiumCompressorUsage", 50D).getDouble(50D);
		Mekanism.combinerUsage = Mekanism.configuration.get("usage", "CombinerUsage", 50D).getDouble(50D);
		Mekanism.crusherUsage = Mekanism.configuration.get("usage", "CrusherUsage", 50D).getDouble(50D);
		Mekanism.factoryUsage = Mekanism.configuration.get("usage", "FactoryUsage", 50D).getDouble(50D);
		Mekanism.metallurgicInfuserUsage = Mekanism.configuration.get("usage", "MetallurgicInfuserUsage", 50D).getDouble(50D);
		Mekanism.purificationChamberUsage = Mekanism.configuration.get("usage", "PurificationChamberUsage", 100D).getDouble(100D);
		Mekanism.energizedSmelterUsage = Mekanism.configuration.get("usage", "EnergizedSmelterUsage", 50D).getDouble(50D);
		Mekanism.digitalMinerUsage = Mekanism.configuration.get("usage", "DigitalMinerUsage", 100D).getDouble(100D);
		Mekanism.rotaryCondensentratorUsage = Mekanism.configuration.get("usage", "RotaryCondensentratorUsage", 50D).getDouble(50D);
		Mekanism.chemicalFormulatorUsage = Mekanism.configuration.get("usage", "ChemicalFormulatorUsage", 100D).getDouble(100D);
		Mekanism.chemicalInfuserUsage = Mekanism.configuration.get("usage", "ChemicalInfuserUsage", 100D).getDouble(100D);
		Mekanism.chemicalInjectionChamberUsage = Mekanism.configuration.get("usage", "ChemicalInjectionChamberUsage", 200D).getDouble(200D);
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
			case 0:
				return new ContainerDictionary(player.inventory);
			case 2:
				return new ContainerDigitalMiner(player.inventory, (TileEntityDigitalMiner)tileEntity);
			case 3:
				return new ContainerElectricMachine(player.inventory, (TileEntityElectricMachine)tileEntity);
			case 4:
				return new ContainerAdvancedElectricMachine(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 5:
				return new ContainerAdvancedElectricMachine(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 6:
				return new ContainerElectricMachine(player.inventory, (TileEntityElectricMachine)tileEntity);
			case 7:
				return new ContainerRotaryCondensentrator(player.inventory, (TileEntityRotaryCondensentrator)tileEntity);
			case 8:
				return new ContainerEnergyCube(player.inventory, (TileEntityEnergyCube)tileEntity);
			case 9:
				return new ContainerNull(player, (TileEntityContainerBlock)tileEntity);
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
			case 26:
				return new ContainerNull(player, (TileEntityContainerBlock)tileEntity);
			case 27:
				return new ContainerFilter(player.inventory, (TileEntityContainerBlock)tileEntity);
			case 28:
				return new ContainerFilter(player.inventory, (TileEntityContainerBlock)tileEntity);
			case 29:
				return new ContainerChemicalFormulator(player.inventory, (TileEntityChemicalFormulator)tileEntity);
			case 30:
				return new ContainerChemicalInfuser(player.inventory, (TileEntityChemicalInfuser)tileEntity);
			case 31:
				return new ContainerAdvancedElectricMachine(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
		}
		
		return null;
	}
	
	public void preInit() {}
	
	public double getReach(EntityPlayer player)
	{
		if(player instanceof EntityPlayerMP)
		{
			return ((EntityPlayerMP)player).theItemInWorldManager.getBlockReachDistance();
		}
		
		return 0;
	}
	
	/**
	 * Gets the Minecraft base directory.
	 * @return base directory
	 */
	public File getMinecraftDir()
	{
		return null;
	}
	
	public void onConfigSync() 
	{
		System.out.println("[Mekanism] Received config from server.");
	}
}
