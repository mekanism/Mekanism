package mekanism.common;

import java.io.File;

import mekanism.api.MekanismAPI;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.ContainerAdvancedElectricMachine;
import mekanism.common.inventory.container.ContainerChanceMachine;
import mekanism.common.inventory.container.ContainerChemicalCrystallizer;
import mekanism.common.inventory.container.ContainerChemicalDissolutionChamber;
import mekanism.common.inventory.container.ContainerChemicalInfuser;
import mekanism.common.inventory.container.ContainerChemicalOxidizer;
import mekanism.common.inventory.container.ContainerChemicalWasher;
import mekanism.common.inventory.container.ContainerDictionary;
import mekanism.common.inventory.container.ContainerDigitalMiner;
import mekanism.common.inventory.container.ContainerDynamicTank;
import mekanism.common.inventory.container.ContainerElectricMachine;
import mekanism.common.inventory.container.ContainerElectricPump;
import mekanism.common.inventory.container.ContainerElectrolyticSeparator;
import mekanism.common.inventory.container.ContainerEnergyCube;
import mekanism.common.inventory.container.ContainerFactory;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerGasTank;
import mekanism.common.inventory.container.ContainerMetallurgicInfuser;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.inventory.container.ContainerPRC;
import mekanism.common.inventory.container.ContainerRobitCrafting;
import mekanism.common.inventory.container.ContainerRobitInventory;
import mekanism.common.inventory.container.ContainerRobitMain;
import mekanism.common.inventory.container.ContainerRobitRepair;
import mekanism.common.inventory.container.ContainerRobitSmelting;
import mekanism.common.inventory.container.ContainerRotaryCondensentrator;
import mekanism.common.inventory.container.ContainerSalinationController;
import mekanism.common.inventory.container.ContainerSeismicVibrator;
import mekanism.common.inventory.container.ContainerTeleporter;
import mekanism.common.tile.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.TileEntityAdvancedFactory;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityChanceMachine;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.TileEntityChemicalOxidizer;
import mekanism.common.tile.TileEntityChemicalWasher;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.tile.TileEntityElectricChest;
import mekanism.common.tile.TileEntityElectricMachine;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEliteFactory;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityObsidianTNT;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPRC;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.TileEntitySalinationController;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.FMLInjectionData;

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
		GameRegistry.registerTileEntity(TileEntityElectricPump.class, "ElectricPump");
		GameRegistry.registerTileEntity(TileEntityElectricChest.class, "ElectricChest");
		GameRegistry.registerTileEntity(TileEntityDynamicTank.class, "DynamicTank");
		GameRegistry.registerTileEntity(TileEntityDynamicValve.class, "DynamicValve");
		GameRegistry.registerTileEntity(TileEntityChargepad.class, "Chargepad");
		GameRegistry.registerTileEntity(TileEntityLogisticalSorter.class, "LogisticalSorter");
		GameRegistry.registerTileEntity(TileEntityBin.class, "Bin");
		GameRegistry.registerTileEntity(TileEntityDigitalMiner.class, "DigitalMiner");
		GameRegistry.registerTileEntity(TileEntityObsidianTNT.class, "ObsidianTNT");
		GameRegistry.registerTileEntity(TileEntityRotaryCondensentrator.class, "RotaryCondensentrator");
		GameRegistry.registerTileEntity(TileEntityTeleporter.class, "MekanismTeleporter");
		GameRegistry.registerTileEntity(TileEntityChemicalOxidizer.class, "ChemicalOxidizer");
		GameRegistry.registerTileEntity(TileEntityChemicalInfuser.class, "ChemicalInfuser");
		GameRegistry.registerTileEntity(TileEntityChemicalInjectionChamber.class, "ChemicalInjectionChamber");
		GameRegistry.registerTileEntity(TileEntityElectrolyticSeparator.class, "ElectrolyticSeparator");
		GameRegistry.registerTileEntity(TileEntitySalinationController.class, "SalinationController");
		GameRegistry.registerTileEntity(TileEntityPrecisionSawmill.class, "PrecisionSawmill");
		GameRegistry.registerTileEntity(TileEntityChemicalDissolutionChamber.class, "ChemicalDissolutionChamber");
		GameRegistry.registerTileEntity(TileEntityChemicalWasher.class, "ChemicalWasher");
		GameRegistry.registerTileEntity(TileEntityChemicalCrystallizer.class, "ChemicalCrystallizer");
		GameRegistry.registerTileEntity(TileEntityPRC.class, "PressurizedReactionChamber");
	}

	/**
	 * Registers a client-side sound, assigned to a TileEntity.
	 * @param obj - TileEntity who is registering the sound
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

		Mekanism.osmiumGenerationEnabled = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "OsmiumGenerationEnabled", true).getBoolean(true);
		Mekanism.copperGenerationEnabled = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "CopperGenerationEnabled", true).getBoolean(true);
		Mekanism.tinGenerationEnabled = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "TinGenerationEnabled", true).getBoolean(true);
		Mekanism.disableBCSteelCrafting = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "DisableBCSteelCrafting", false).getBoolean(false);
		Mekanism.disableBCBronzeCrafting = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "DisableBCBronzeCrafting", false).getBoolean(false);
		Mekanism.updateNotifications = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "UpdateNotifications", true).getBoolean(true);
		Mekanism.controlCircuitOreDict = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ControlCircuitOreDict", true).getBoolean(true);
		Mekanism.logPackets = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "LogPackets", false).getBoolean(false);
		Mekanism.dynamicTankEasterEgg = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "DynamicTankEasterEgg", false).getBoolean(true);
		Mekanism.voiceServerEnabled = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "VoiceServerEnabled", true).getBoolean(true);
		Mekanism.forceBuildcraft = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ForceBuildcraftPower", false).getBoolean(false);
		Mekanism.cardboardSpawners = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "AllowSpawnerBoxPickup", true).getBoolean(true);
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
		Mekanism.FROM_H2 = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "HydrogenEnergyDensity", 2000D).getDouble(2000D);
		Mekanism.ENERGY_PER_REDSTONE = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EnergyPerRedstone", 10000D).getDouble(10000D);
		Mekanism.VOICE_PORT = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "VoicePort", 36123).getInt();
		//If this is less than 1, upgrades make machines worse. If less than 0, I don't even know.
		Mekanism.maxUpgradeMultiplier = Math.max(1, Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "UpgradeModifier", 10).getInt());

		Mekanism.TO_TE = Mekanism.TO_BC*10;
		Mekanism.FROM_TE = Mekanism.FROM_BC/10;

		if(Mekanism.cardboardSpawners)
		{
			MekanismAPI.removeBoxBlacklist(Blocks.mob_spawner, 0);
		}
		else {
			MekanismAPI.addBoxBlacklist(Blocks.mob_spawner, 0);
		}

		Mekanism.enrichmentChamberUsage = Mekanism.configuration.get("usage", "EnrichmentChamberUsage", 50D).getDouble(50D);
		Mekanism.osmiumCompressorUsage = Mekanism.configuration.get("usage", "OsmiumCompressorUsage", 100D).getDouble(100D);
		Mekanism.combinerUsage = Mekanism.configuration.get("usage", "CombinerUsage", 50D).getDouble(50D);
		Mekanism.crusherUsage = Mekanism.configuration.get("usage", "CrusherUsage", 50D).getDouble(50D);
		Mekanism.factoryUsage = Mekanism.configuration.get("usage", "FactoryUsage", 50D).getDouble(50D);
		Mekanism.metallurgicInfuserUsage = Mekanism.configuration.get("usage", "MetallurgicInfuserUsage", 50D).getDouble(50D);
		Mekanism.purificationChamberUsage = Mekanism.configuration.get("usage", "PurificationChamberUsage", 200D).getDouble(200D);
		Mekanism.energizedSmelterUsage = Mekanism.configuration.get("usage", "EnergizedSmelterUsage", 50D).getDouble(50D);
		Mekanism.digitalMinerUsage = Mekanism.configuration.get("usage", "DigitalMinerUsage", 100D).getDouble(100D);
		Mekanism.rotaryCondensentratorUsage = Mekanism.configuration.get("usage", "RotaryCondensentratorUsage", 50D).getDouble(50D);
		Mekanism.oxidationChamberUsage = Mekanism.configuration.get("usage", "OxidationChamberUsage", 200D).getDouble(200D);
		Mekanism.chemicalInfuserUsage = Mekanism.configuration.get("usage", "ChemicalInfuserUsage", 200D).getDouble(200D);
		Mekanism.chemicalInjectionChamberUsage = Mekanism.configuration.get("usage", "ChemicalInjectionChamberUsage", 400D).getDouble(400D);
		Mekanism.precisionSawmillUsage = Mekanism.configuration.get("usage", "PrecisionSawmillUsage", 50D).getDouble(50D);
		Mekanism.chemicalDissolutionChamberUsage = Mekanism.configuration.get("usage", "ChemicalDissolutionChamberUsage", 400D).getDouble(400D);
		Mekanism.chemicalWasherUsage = Mekanism.configuration.get("usage", "ChemicalWasherUsage", 200D).getDouble(200D);
		Mekanism.chemicalCrystallizerUsage = Mekanism.configuration.get("usage", "ChemicalCrystallizerUsage", 400D).getDouble(400D);
		Mekanism.seismicVibratorUsage = Mekanism.configuration.get("usage", "SeismicVibratorUsage", 50D).getDouble(50D);
		Mekanism.pressurizedReactionBaseUsage = Mekanism.configuration.get("usage", "PressurizedReactionBaseUsage", 5D).getDouble(5D);
		Mekanism.configuration.save();
	}

	/**
	 * Set up and load the utilities this mod uses.
	 */
	public void loadUtilities() 
	{
		FMLCommonHandler.instance().bus().register(new CommonWorldTickHandler());
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
		TileEntity tileEntity = world.getTileEntity(x, y, z);

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
				return new ContainerChemicalOxidizer(player.inventory, (TileEntityChemicalOxidizer)tileEntity);
			case 30:
				return new ContainerChemicalInfuser(player.inventory, (TileEntityChemicalInfuser)tileEntity);
			case 31:
				return new ContainerAdvancedElectricMachine(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 32:
				return new ContainerElectrolyticSeparator(player.inventory, (TileEntityElectrolyticSeparator)tileEntity);
			case 33:
				return new ContainerSalinationController(player.inventory, (TileEntitySalinationController)tileEntity);
			case 34:
				return new ContainerChanceMachine(player.inventory, (TileEntityChanceMachine)tileEntity);
			case 35:
				return new ContainerChemicalDissolutionChamber(player.inventory, (TileEntityChemicalDissolutionChamber)tileEntity);
			case 36:
				return new ContainerChemicalWasher(player.inventory, (TileEntityChemicalWasher)tileEntity);
			case 37:
				return new ContainerChemicalCrystallizer(player.inventory, (TileEntityChemicalCrystallizer)tileEntity);
			case 39:
				return new ContainerSeismicVibrator(player.inventory, (TileEntitySeismicVibrator)tileEntity);
			case 40:
				return new ContainerPRC(player.inventory, (TileEntityPRC)tileEntity);
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
		return (File)FMLInjectionData.data()[6];
	}

	public void onConfigSync()
	{
		if(Mekanism.cardboardSpawners)
		{
			MekanismAPI.removeBoxBlacklist(Blocks.mob_spawner, 0);
		}
		else {
			MekanismAPI.addBoxBlacklist(Blocks.mob_spawner, 0);
		}

		Mekanism.logger.info("Received config from server.");
	}
}
