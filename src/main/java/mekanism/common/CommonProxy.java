package mekanism.common;

import java.io.File;
import java.lang.ref.WeakReference;

import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismConfig.general;
import mekanism.api.MekanismConfig.machines;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.Pos3D;
import mekanism.api.util.UnitDisplayUtils.EnergyType;
import mekanism.api.util.UnitDisplayUtils.TempType;
import mekanism.client.SparkleAnimation.INodeChecker;
import mekanism.common.base.IGuiProvider;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.BlockMachine.MachineType;
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
import mekanism.common.inventory.container.ContainerFluidTank;
import mekanism.common.inventory.container.ContainerFluidicPlenisher;
import mekanism.common.inventory.container.ContainerFormulaicAssemblicator;
import mekanism.common.inventory.container.ContainerFuelwoodHeater;
import mekanism.common.inventory.container.ContainerGasTank;
import mekanism.common.inventory.container.ContainerInductionMatrix;
import mekanism.common.inventory.container.ContainerLaserAmplifier;
import mekanism.common.inventory.container.ContainerLaserTractorBeam;
import mekanism.common.inventory.container.ContainerMetallurgicInfuser;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.inventory.container.ContainerOredictionificator;
import mekanism.common.inventory.container.ContainerPRC;
import mekanism.common.inventory.container.ContainerQuantumEntangloporter;
import mekanism.common.inventory.container.ContainerResistiveHeater;
import mekanism.common.inventory.container.ContainerRobitCrafting;
import mekanism.common.inventory.container.ContainerRobitInventory;
import mekanism.common.inventory.container.ContainerRobitMain;
import mekanism.common.inventory.container.ContainerRobitRepair;
import mekanism.common.inventory.container.ContainerRobitSmelting;
import mekanism.common.inventory.container.ContainerRotaryCondensentrator;
import mekanism.common.inventory.container.ContainerSecurityDesk;
import mekanism.common.inventory.container.ContainerSeismicVibrator;
import mekanism.common.inventory.container.ContainerSolarNeutronActivator;
import mekanism.common.inventory.container.ContainerTeleporter;
import mekanism.common.inventory.container.ContainerThermalEvaporationController;
import mekanism.common.inventory.container.ContainerUpgradeManagement;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterMessage;
import mekanism.common.tile.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.TileEntityAdvancedFactory;
import mekanism.common.tile.TileEntityAmbientAccumulator;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.TileEntityBoilerValve;
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
import mekanism.common.tile.TileEntityElectricMachine;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEliteFactory;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionPort;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.tile.TileEntityLaser;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.tile.TileEntityObsidianTNT;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPRC;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import mekanism.common.tile.TileEntityStructuralGlass;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.FMLInjectionData;

/**
 * Common proxy for the Mekanism mod.
 * @author AidanBrady
 *
 */
public class CommonProxy implements IGuiProvider
{
	public static int MACHINE_RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
	public static int BASIC_RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
	public static int PLASTIC_RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
	public static int CTM_RENDER_ID = RenderingRegistry.getNextAvailableRenderId();

	protected static WeakReference<EntityPlayer> dummyPlayer = new WeakReference<EntityPlayer>(null);

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
		GameRegistry.registerTileEntity(TileEntityPersonalChest.class, "ElectricChest"); //TODO rename
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
		GameRegistry.registerTileEntity(TileEntityThermalEvaporationController.class, "SalinationController"); //TODO rename
		GameRegistry.registerTileEntity(TileEntityPrecisionSawmill.class, "PrecisionSawmill");
		GameRegistry.registerTileEntity(TileEntityChemicalDissolutionChamber.class, "ChemicalDissolutionChamber");
		GameRegistry.registerTileEntity(TileEntityChemicalWasher.class, "ChemicalWasher");
		GameRegistry.registerTileEntity(TileEntityChemicalCrystallizer.class, "ChemicalCrystallizer");
		GameRegistry.registerTileEntity(TileEntitySeismicVibrator.class, "SeismicVibrator");
		GameRegistry.registerTileEntity(TileEntityPRC.class, "PressurizedReactionChamber");
		GameRegistry.registerTileEntity(TileEntityFluidTank.class, "PortableTank"); //TODO rename
		GameRegistry.registerTileEntity(TileEntityFluidicPlenisher.class, "FluidicPlenisher");
		GameRegistry.registerTileEntity(TileEntityLaser.class, "Laser");
		GameRegistry.registerTileEntity(TileEntityLaserAmplifier.class, "LaserAmplifier");
		GameRegistry.registerTileEntity(TileEntityLaserTractorBeam.class, "LaserTractorBeam");
		GameRegistry.registerTileEntity(TileEntitySolarNeutronActivator.class, "SolarNeutronActivator");
		GameRegistry.registerTileEntity(TileEntityAmbientAccumulator.class, "AmbientAccumulator");
		GameRegistry.registerTileEntity(TileEntityInductionCasing.class, "InductionCasing");
		GameRegistry.registerTileEntity(TileEntityInductionPort.class, "InductionPort");
		GameRegistry.registerTileEntity(TileEntityInductionCell.class, "InductionCell");
		GameRegistry.registerTileEntity(TileEntityInductionProvider.class, "InductionProvider");
		GameRegistry.registerTileEntity(TileEntityOredictionificator.class, "Oredictionificator");
		GameRegistry.registerTileEntity(TileEntityStructuralGlass.class, "StructuralGlass");
		GameRegistry.registerTileEntity(TileEntityFormulaicAssemblicator.class, "FormulaicAssemblicator");
		GameRegistry.registerTileEntity(TileEntityResistiveHeater.class, "ResistiveHeater");
		GameRegistry.registerTileEntity(TileEntityBoilerCasing.class, "BoilerCasing");
		GameRegistry.registerTileEntity(TileEntityBoilerValve.class, "BoilerValve");
		GameRegistry.registerTileEntity(TileEntitySecurityDesk.class, "SecurityDesk");
		GameRegistry.registerTileEntity(TileEntityQuantumEntangloporter.class, "QuantumEntangloporter");
		GameRegistry.registerTileEntity(TileEntityFuelwoodHeater.class, "FuelwoodHeater");
	}
	
	public void handleTeleporterUpdate(PortableTeleporterMessage message) {}

	/**
	 * Handles an PERSONAL_CHEST_CLIENT_OPEN packet via the proxy, not handled on the server-side.
	 * @param entityplayer - player the packet was sent from
	 * @param id - the gui ID to open
	 * @param windowId - the container-specific window ID
	 * @param isBlock - if the chest is a block
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 */
	public void openPersonalChest(EntityPlayer entityplayer, int id, int windowId, boolean isBlock, int x, int y, int z) {}

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
		general.updateNotifications = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "UpdateNotifications", true).getBoolean();
		general.controlCircuitOreDict = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ControlCircuitOreDict", true).getBoolean();
		general.logPackets = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "LogPackets", false).getBoolean();
		general.dynamicTankEasterEgg = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "DynamicTankEasterEgg", false).getBoolean();
		general.voiceServerEnabled = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "VoiceServerEnabled", true).getBoolean();
		general.cardboardSpawners = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "AllowSpawnerBoxPickup", true).getBoolean();
		general.enableWorldRegeneration = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EnableWorldRegeneration", false).getBoolean();
		general.spawnBabySkeletons = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "SpawnBabySkeletons", true).getBoolean();
		general.obsidianTNTDelay = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ObsidianTNTDelay", 100).getInt();
		general.obsidianTNTBlastRadius = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ObsidianTNTBlastRadius", 12).getInt();
		general.UPDATE_DELAY = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ClientUpdateDelay", 10).getInt();
		general.osmiumPerChunk = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "OsmiumPerChunk", 12).getInt();
		general.copperPerChunk = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "CopperPerChunk", 16).getInt();
		general.tinPerChunk = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "TinPerChunk", 14).getInt();
		general.saltPerChunk = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "SaltPerChunk", 2).getInt();
		general.userWorldGenVersion = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "WorldRegenVersion", 0).getInt();
		general.FROM_IC2 = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "JoulesToEU", 10D).getDouble();
		general.TO_IC2 = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EUToJoules", .1D).getDouble();
		general.FROM_TE = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "JoulesToRF", 2.5D).getDouble();
		general.TO_TE = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "RFToJoules", 0.4D).getDouble();
		general.FROM_H2 = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "HydrogenEnergyDensity", 200D, "Determines Electrolytic Separator usage").getDouble();
		general.ETHENE_BURN_TIME = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EthyleneBurnTime", 40).getInt();
		general.ENERGY_PER_REDSTONE = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EnergyPerRedstone", 10000D).getDouble();
		general.DISASSEMBLER_USAGE = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "DisassemblerEnergyUsage", 10).getInt();
		general.VOICE_PORT = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "VoicePort", 36123, null, 1, 65535).getInt();
		//If this is less than 1, upgrades make machines worse. If less than 0, I don't even know.
		general.maxUpgradeMultiplier = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "UpgradeModifier", 10, null, 1, Integer.MAX_VALUE).getInt();
		general.minerSilkMultiplier = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "MinerSilkMultiplier", 6).getDouble();
		general.prefilledFluidTanks = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "PrefilledFluidTanks", true).getBoolean();
		general.prefilledGasTanks = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "PrefilledGasTanks", true).getBoolean();
		general.armoredJetpackDamageRatio = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ArmoredJetpackDamageRatio", 0.8).getDouble();
		general.armoredJetpackDamageMax = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ArmoredJepackDamageMax", 115).getInt();
		general.aestheticWorldDamage = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "AestheticWorldDamage", true).getBoolean();
		general.opsBypassRestrictions = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "OpsBypassRestrictions", false).getBoolean();
		general.thermalEvaporationSpeed = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "ThermalEvaporationSpeed", 1.0D).getDouble();
		general.maxJetpackGas = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "MaxJetpackGas", 24000).getInt();
		general.maxScubaGas = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "MaxScubaGas", 24000).getInt();
		general.maxFlamethrowerGas = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "MaxFlamethrowerGas", 24000).getInt();
		general.maxPumpRange = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "MaxPumpRange", 80).getInt();
		general.pumpWaterSources = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "PumpWaterSources", false).getBoolean();
		general.maxPlenisherNodes = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "MaxPlenisherNodes", 4000).getInt();
		general.evaporationHeatDissipation = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EvaporationHeatDissipation", 0.02D).getDouble();
		general.evaporationTempMultiplier = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EvaporationTempMultiplier", 0.1D).getDouble();
		general.evaporationSolarMultiplier = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EvaporationSolarMultiplier", 0.2D).getDouble();
		general.evaporationMaxTemp = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EvaporationMaxTemp", 3000D).getDouble();
		general.energyPerHeat = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EnergyPerHeat", 1000D).getDouble();
		general.maxEnergyPerSteam = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "MaxEnergyPerSteam", 100D).getDouble();
		general.superheatingHeatTransfer = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "SuperheatingHeatTransfer", 10000D).getDouble();
		general.heatPerFuelTick = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "HeatPerFuelTick", 4D).getDouble();
		general.allowTransmitterAlloyUpgrade = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "AllowTransmitterAlloyUpgrade", true).getBoolean();
		general.allowProtection = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "AllowProtection", true).getBoolean();
		
		general.blacklistIC2 = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "BlacklistIC2Power", false).getBoolean();
		general.blacklistRF = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "BlacklistRFPower", false).getBoolean();
		
		String s = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EnergyType", "J", null, new String[]{"J", "RF", "MJ", "EU"}).getString();

		if(s != null)
		{
			if(s.trim().equalsIgnoreCase("j") || s.trim().equalsIgnoreCase("joules"))
			{
				general.energyUnit = EnergyType.J;
			}
			else if(s.trim().equalsIgnoreCase("rf") || s.trim().equalsIgnoreCase("te") || s.trim().equalsIgnoreCase("thermal expansion"))
			{
				general.energyUnit = EnergyType.RF;
			}
			else if(s.trim().equalsIgnoreCase("eu") || s.trim().equalsIgnoreCase("ic2"))
			{
				general.energyUnit = EnergyType.EU;
			}
			else if(s.trim().equalsIgnoreCase("mj") || s.trim().equalsIgnoreCase("bc") || s.trim().equalsIgnoreCase("buildcraft"))
			{
				general.energyUnit = EnergyType.MJ;
			}
		}

		s = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "Temperature Units", "K", null, new String[]{"K", "C", "R", "F"}).getString();

		if(s != null)
		{
			if(s.trim().equalsIgnoreCase("k") || s.trim().equalsIgnoreCase("kelvin"))
			{
				general.tempUnit = TempType.K;
			}
			else if(s.trim().equalsIgnoreCase("c") || s.trim().equalsIgnoreCase("celsius") || s.trim().equalsIgnoreCase("centigrade"))
			{
				general.tempUnit = TempType.C;
			}
			else if(s.trim().equalsIgnoreCase("r") || s.trim().equalsIgnoreCase("rankine"))
			{
				general.tempUnit = TempType.R;
			}
			else if(s.trim().equalsIgnoreCase("f") || s.trim().equalsIgnoreCase("fahrenheit"))
			{
				general.tempUnit = TempType.F;
			}
			else if(s.trim().equalsIgnoreCase("a") || s.trim().equalsIgnoreCase("ambient") || s.trim().equalsIgnoreCase("stp"))
			{
				general.tempUnit = TempType.STP;
			}
		}

		general.laserRange = Mekanism.configuration.get("general", "LaserRange", 64).getInt();
		general.laserEnergyNeededPerHardness = Mekanism.configuration.get("general", "LaserDiggingEnergy", 100000).getInt();
		general.destroyDisabledBlocks = Mekanism.configuration.get("general", "DestroyDisabledBlocks", true).getBoolean();

		
		for(MachineType type : MachineType.getValidMachines())
		{
			machines.setEntry(type.name, Mekanism.configuration.get("machines", type.name + "Enabled", true).getBoolean());
		}
		
		usage.enrichmentChamberUsage = Mekanism.configuration.get("usage", "EnrichmentChamberUsage", 50D).getDouble();
		usage.osmiumCompressorUsage = Mekanism.configuration.get("usage", "OsmiumCompressorUsage", 100D).getDouble();
		usage.combinerUsage = Mekanism.configuration.get("usage", "CombinerUsage", 50D).getDouble();
		usage.crusherUsage = Mekanism.configuration.get("usage", "CrusherUsage", 50D).getDouble();
		usage.factoryUsage = Mekanism.configuration.get("usage", "FactoryUsage", 50D).getDouble();
		usage.metallurgicInfuserUsage = Mekanism.configuration.get("usage", "MetallurgicInfuserUsage", 50D).getDouble();
		usage.purificationChamberUsage = Mekanism.configuration.get("usage", "PurificationChamberUsage", 200D).getDouble();
		usage.energizedSmelterUsage = Mekanism.configuration.get("usage", "EnergizedSmelterUsage", 50D).getDouble();
		usage.digitalMinerUsage = Mekanism.configuration.get("usage", "DigitalMinerUsage", 100D).getDouble();
		usage.electricPumpUsage = Mekanism.configuration.get("usage", "ElectricPumpUsage", 100D).getDouble();
		usage.rotaryCondensentratorUsage = Mekanism.configuration.get("usage", "RotaryCondensentratorUsage", 50D).getDouble();
		usage.oxidationChamberUsage = Mekanism.configuration.get("usage", "OxidationChamberUsage", 200D).getDouble();
		usage.chemicalInfuserUsage = Mekanism.configuration.get("usage", "ChemicalInfuserUsage", 200D).getDouble();
		usage.chemicalInjectionChamberUsage = Mekanism.configuration.get("usage", "ChemicalInjectionChamberUsage", 400D).getDouble();
		usage.precisionSawmillUsage = Mekanism.configuration.get("usage", "PrecisionSawmillUsage", 50D).getDouble();
		usage.chemicalDissolutionChamberUsage = Mekanism.configuration.get("usage", "ChemicalDissolutionChamberUsage", 400D).getDouble();
		usage.chemicalWasherUsage = Mekanism.configuration.get("usage", "ChemicalWasherUsage", 200D).getDouble();
		usage.chemicalCrystallizerUsage = Mekanism.configuration.get("usage", "ChemicalCrystallizerUsage", 400D).getDouble();
		usage.seismicVibratorUsage = Mekanism.configuration.get("usage", "SeismicVibratorUsage", 50D).getDouble();
		usage.pressurizedReactionBaseUsage = Mekanism.configuration.get("usage", "PressurizedReactionBaseUsage", 5D).getDouble();
		usage.fluidicPlenisherUsage = Mekanism.configuration.get("usage", "FluidicPlenisherUsage", 100D).getDouble();
		usage.laserUsage = Mekanism.configuration.get("usage", "LaserUsage", 5000D).getDouble();
		usage.gasCentrifugeUsage = Mekanism.configuration.get("usage", "GasCentrifugeUsage", 100D).getDouble();
		usage.heavyWaterElectrolysisUsage = Mekanism.configuration.get("usage", "HeavyWaterElectrolysisUsage", 800D).getDouble();
		usage.formulaicAssemblicatorUsage = Mekanism.configuration.get("usage", "FormulaicAssemblicatorUsage", 100D).getDouble();

		Tier.loadConfig();
		
		if(Mekanism.configuration.hasChanged())
		{
			Mekanism.configuration.save();
		}
	}

	/**
	 * Set up and load the utilities this mod uses.
	 */
	public void loadUtilities() 
	{
		FMLCommonHandler.instance().bus().register(Mekanism.worldTickHandler);
	}

	/**
	 * Whether or not the game is paused.
	 */
	public boolean isPaused()
	{
		return false;
	}
	
	/**
	 * Adds block hit effects on the client side.
	 */
	public void addHitEffects(Coord4D coord, MovingObjectPosition mop) {}
	
	/**
	 * Does a generic creation animation, starting from the rendering block.
	 */
	public void doGenericSparkle(TileEntity tileEntity, INodeChecker checker) {}

	/**
	 * Does the multiblock creation animation, starting from the rendering block.
	 */
	public void doMultiblockSparkle(TileEntityMultiblock<?> tileEntity) {}

	@Override
	public Object getClientGui(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	@Override
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
			case 14:
				ItemStack itemStack = player.getCurrentEquippedItem();

				if(itemStack != null && itemStack.getItem() instanceof ItemPortableTeleporter)
				{
					return new ContainerNull();
				}
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
				robit = (EntityRobit)world.getEntityByID(x);

				if(robit != null)
				{
					return new ContainerRobitCrafting(player.inventory, robit);
				}
			case 23:
				robit = (EntityRobit)world.getEntityByID(x);

				if(robit != null)
				{
					return new ContainerRobitInventory(player.inventory, robit);
				}
			case 24:
				robit = (EntityRobit)world.getEntityByID(x);

				if(robit != null)
				{
					return new ContainerRobitSmelting(player.inventory, robit);
				}
			case 25:
				robit = (EntityRobit)world.getEntityByID(x);

				if(robit != null)
				{
					return new ContainerRobitRepair(player.inventory, robit);
				}
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
				return new ContainerThermalEvaporationController(player.inventory, (TileEntityThermalEvaporationController)tileEntity);
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
			case 41:
				return new ContainerFluidTank(player.inventory, (TileEntityFluidTank)tileEntity);
			case 42:
				return new ContainerFluidicPlenisher(player.inventory, (TileEntityFluidicPlenisher)tileEntity);
			case 43:
				return new ContainerUpgradeManagement(player.inventory, (IUpgradeTile)tileEntity);
			case 44:
				return new ContainerLaserAmplifier(player.inventory, (TileEntityLaserAmplifier)tileEntity);
			case 45:
				return new ContainerLaserTractorBeam(player.inventory, (TileEntityLaserTractorBeam)tileEntity);
			case 46:
				return new ContainerQuantumEntangloporter(player.inventory, (TileEntityQuantumEntangloporter)tileEntity);
			case 47:
				return new ContainerSolarNeutronActivator(player.inventory, (TileEntitySolarNeutronActivator)tileEntity);
			case 48:
				return new ContainerNull(player, (TileEntityContainerBlock)tileEntity);
			case 49:
				return new ContainerInductionMatrix(player.inventory, (TileEntityInductionCasing)tileEntity);
			case 50:
				return new ContainerNull(player, (TileEntityContainerBlock)tileEntity);
			case 51:
				return new ContainerNull(player, (TileEntityContainerBlock)tileEntity);
			case 52:
				return new ContainerOredictionificator(player.inventory, (TileEntityOredictionificator)tileEntity);
			case 53:
				return new ContainerResistiveHeater(player.inventory, (TileEntityResistiveHeater)tileEntity);
			case 54:
				return new ContainerFilter(player.inventory, (TileEntityContainerBlock)tileEntity);
			case 55:
				return new ContainerNull(player, (TileEntityContainerBlock)tileEntity);
			case 56:
				return new ContainerFormulaicAssemblicator(player.inventory, (TileEntityFormulaicAssemblicator)tileEntity);
			case 57:
				return new ContainerSecurityDesk(player.inventory, (TileEntitySecurityDesk)tileEntity);
			case 58:
				return new ContainerFuelwoodHeater(player.inventory, (TileEntityFuelwoodHeater)tileEntity);
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
	
	public void updateConfigRecipes()
	{
		for(MachineType type : MachineType.getValidMachines())
		{
			if(machines.isEnabled(type.name))
			{
				CraftingManager.getInstance().getRecipeList().removeAll(type.getRecipes());
				CraftingManager.getInstance().getRecipeList().addAll(type.getRecipes());
			}
			else {
				CraftingManager.getInstance().getRecipeList().removeAll(type.getRecipes());
			}
		}
	}

	public void onConfigSync(boolean fromPacket)
	{
		if(general.cardboardSpawners)
		{
			MekanismAPI.removeBoxBlacklist(Blocks.mob_spawner, 0);
		}
		else {
			MekanismAPI.addBoxBlacklist(Blocks.mob_spawner, 0);
		}
		
		MachineType.updateAllUsages();
		
		updateConfigRecipes();

		if(fromPacket)
		{
			Mekanism.logger.info("Received config from server.");
		}
	}
	
	private WeakReference<EntityPlayer> createNewPlayer(WorldServer world) 
	{
		EntityPlayer player = FakePlayerFactory.get(world, Mekanism.gameProfile);

		return new WeakReference<EntityPlayer>(player);
	}

	private WeakReference<EntityPlayer> createNewPlayer(WorldServer world, double x, double y, double z)
	{
		EntityPlayer player = FakePlayerFactory.get(world, Mekanism.gameProfile);
		
		player.posX = x;
		player.posY = y;
		player.posZ = z;
		
		return new WeakReference<EntityPlayer>(player);
	}

	public final WeakReference<EntityPlayer> getDummyPlayer(WorldServer world) 
	{
		if(dummyPlayer.get() == null) 
		{
			dummyPlayer = createNewPlayer(world);
		} 
		else {
			dummyPlayer.get().worldObj = world;
		}

		return dummyPlayer;
	}

	public final WeakReference<EntityPlayer> getDummyPlayer(WorldServer world, double x, double y, double z) 
	{
		if(dummyPlayer.get() == null) 
		{
			dummyPlayer = createNewPlayer(world, x, y, z);
		} 
		else {
			dummyPlayer.get().worldObj = world;
			dummyPlayer.get().posX = x;
			dummyPlayer.get().posY = y;
			dummyPlayer.get().posZ = z;
		}

		return dummyPlayer;
	}

	public EntityPlayer getPlayer(MessageContext context)
	{
		return context.getServerHandler().playerEntity;
	}
	
	public int getGuiId(Block block, int metadata)
	{
		if(MachineType.get(block, metadata) != null)
		{
			return MachineType.get(block, metadata).guiId;
		}
		else if(block == MekanismBlocks.GasTank)
		{
			return 10;
		}
		else if(block == MekanismBlocks.EnergyCube)
		{
			return 8;
		}
		
		return -1;
	}

	public void renderLaser(World world, Pos3D from, Pos3D to, ForgeDirection direction, double energy) {}
	
	public Object getFontRenderer()
	{
		return null;
	}
}
