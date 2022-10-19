package mekanism.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.api.util.UnitDisplayUtils.EnergyType;
import mekanism.api.util.UnitDisplayUtils.TempType;

public class MekanismConfig
{
	public static class general
	{
		public static boolean updateNotifications = true;
		public static boolean controlCircuitOreDict = true;
		public static boolean logPackets = false;
		public static boolean dynamicTankEasterEgg = false;
		public static boolean voiceServerEnabled = true;
		public static boolean cardboardSpawners = true;
		public static boolean enableWorldRegeneration = true;
		public static boolean spawnBabySkeletons = true;
		public static boolean enableBoPProgression = true;
		public static int obsidianTNTBlastRadius = 12;
		public static int osmiumPerChunk = 12;
		public static int copperPerChunk = 16;
		public static int tinPerChunk = 14;
		public static int saltPerChunk = 2;
		public static int obsidianTNTDelay = 100;
		public static int UPDATE_DELAY = 10;
		public static int VOICE_PORT = 36123;
		public static int maxUpgradeMultiplier = 10;
		public static int userWorldGenVersion = 0;
		public static double ENERGY_PER_REDSTONE = 10000;
		public static int ETHENE_BURN_TIME = 40;
		public static int METHANE_BURN_TIME = 10;
		public static double DISASSEMBLER_USAGE = 10;
		public static EnergyType energyUnit = EnergyType.J;
		public static TempType tempUnit =	TempType.K;
		public static double TO_IC2;
		public static double TO_TE;
		public static double FROM_H2;
		public static double FROM_IC2;
		public static double FROM_TE;
		public static int laserRange;
		public static double laserEnergyNeededPerHardness;
		public static double minerSilkMultiplier = 6;
		public static boolean blacklistIC2;
		public static boolean blacklistRF;
		public static boolean destroyDisabledBlocks;
		public static boolean prefilledFluidTanks;
		public static boolean prefilledGasTanks;
		public static double armoredJetpackDamageRatio;
		public static int armoredJetpackDamageMax;
		public static boolean aestheticWorldDamage;
		public static boolean opsBypassRestrictions;
		public static double thermalEvaporationSpeed;
		public static int maxJetpackGas;
		public static int maxScubaGas;
		public static int maxFlamethrowerGas;
		public static int maxPumpRange;
		public static boolean pumpWaterSources;
		public static int maxPlenisherNodes;
		public static double evaporationHeatDissipation = 0.02;
		public static double evaporationTempMultiplier = 0.1;
		public static double evaporationSolarMultiplier = 0.2;
		public static double evaporationMaxTemp = 3000;
		public static double energyPerHeat = 1000;
		public static double maxEnergyPerSteam = 100;
		public static double superheatingHeatTransfer = 10000;
		public static double heatPerFuelTick = 4;
		public static boolean allowTransmitterAlloyUpgrade;
		public static boolean allowProtection = true;
		public static boolean EnableQuartzCompat;
		public static boolean EnableDiamondCompat;
		public static boolean EnablePoorOresCompat;
		public static boolean OreDictOsmium;
		public static boolean OreDictPlatinum;
	}

	public static class client
	{
		public static boolean enablePlayerSounds = true;
		public static boolean enableMachineSounds = true;
		public static boolean holidays = true;
		public static float baseSoundVolume = 1F;
		public static boolean machineEffects = true;
		public static boolean oldTransmitterRender = false;
		public static boolean replaceSoundsWhenResuming = true;
		public static boolean renderCTM = true;
		public static boolean enableAmbientLighting;
		public static int ambientLightingLevel;
		public static boolean opaqueTransmitters = false;
		public static boolean doMultiblockSparkle = true;
		public static int multiblockSparkleIntensity = 6;
	}
	
	public static class machines
	{
		private static Map<String, Boolean> config = new HashMap<String, Boolean>();
		
		public static boolean isEnabled(String type)
		{
			return config.get(type) != null && config.get(type);
		}
		
		public static void setEntry(String type, boolean enabled)
		{
			config.put(type, enabled);
		}
	}

	public static class usage
	{
		public static double enrichmentChamberUsage;
		public static double osmiumCompressorUsage;
		public static double combinerUsage;
		public static double crusherUsage;
		public static double factoryUsage;
		public static double metallurgicInfuserUsage;
		public static double purificationChamberUsage;
		public static double energizedSmelterUsage;
		public static double digitalMinerUsage;
		public static double electricPumpUsage;
		public static double rotaryCondensentratorUsage;
		public static double oxidationChamberUsage;
		public static double chemicalInfuserUsage;
		public static double chemicalInjectionChamberUsage;
		public static double precisionSawmillUsage;
		public static double chemicalDissolutionChamberUsage;
		public static double chemicalWasherUsage;
		public static double chemicalCrystallizerUsage;
		public static double seismicVibratorUsage;
		public static double pressurizedReactionBaseUsage;
		public static double fluidicPlenisherUsage;
		public static double laserUsage;
		public static double gasCentrifugeUsage;
		public static double heavyWaterElectrolysisUsage;
		public static double formulaicAssemblicatorUsage;
	}

	public static class generators
	{
		public static double advancedSolarGeneration;
		public static double bioGeneration;
		public static double heatGeneration;
		public static double heatGenerationLava;
		public static double heatGenerationNether;
		public static int heatGenerationFluidRate;
		public static boolean heatGenEnable;
		public static double solarGeneration;

		public static double windGenerationMin;
		public static double windGenerationMax;

		public static int windGenerationMinY;
		public static int windGenerationMaxY;
		
		public static int turbineBladesPerCoil;
		public static double turbineVentGasFlow;
		public static double turbineDisperserGasFlow;
		public static int condenserRate;
		public static boolean enableWindmillWhitelist;
		public static List<String> winddimensionids;

	}

	public static class tools
	{
		public static double armorSpawnRate;
		public static boolean enableTools = true;
	}

	public static class recipes
	{
		public static boolean enableOsmiumBlock = true;
		public static boolean enableBronzeBlock = true;
		public static boolean enableRefinedObsidianBlock = true;
		public static boolean enableCharcoalBlock = true;
		public static boolean enableRefinedGlowstoneBlock = true;
		public static boolean enableSteelBlock = true;
		public static boolean enableCopperBlock = true;
		public static boolean enableTinBlock = true;
		public static boolean enableBins = true;
		public static boolean enableTeleporterFrame = true;
		public static boolean enableSteelCasing = true;
		public static boolean enableDynamicTank = true;
		public static boolean enableDynamicGlass = true;
		public static boolean enableDynamicValve = true;
		public static boolean enableThermalEvaporationController = true;
		public static boolean enableThermalEvaporationValve = true;
		public static boolean enableThermalEvaporationBlock = true;
		public static boolean enableInductionCasing = true;
		public static boolean enableInductionPorts = true;
		public static boolean enableInductionCells = true;
		public static boolean enableInductionProviders = true;
		public static boolean enableSuperheatingElement = true;
		public static boolean enablePressureDispenser = true;
		public static boolean enableBoilerCasing = true;
		public static boolean enableBoilerValve = true;
		public static boolean enableSecurityDesk = true;
		public static boolean enableEnrichmentChamber = true;
		public static boolean enableOsmiumCompressor = true;
		public static boolean enableCombiner = true;
		public static boolean enableCrusher = true;
		public static boolean enableDigitalMiner = true;
		public static boolean enableFactories = true;
		public static boolean enableMetallurgicInfuser = true;
		public static boolean enablePurificationChamber = true;
		public static boolean enableEnergizedSmelter = true;
		public static boolean enableTeleporterBlock = true;
		public static boolean enableElectricPump = true;
		public static boolean enablePersonalChest = true;
		public static boolean enableChargePad = true;
		public static boolean enableLogisticalSorter = true;
		public static boolean enableRotaryCondensentrator = true;
		public static boolean enableChemicalOxidiser = true;
		public static boolean enableChemicalInfuser = true;
		public static boolean enableChemicalInjection = true;
		public static boolean enableElectrolyticSeparator = true;
		public static  boolean enableElectroliticCore = true;
		public static boolean enableCardboardBox = true;
		public static boolean enableSawdusttoPaper = true;
		public static boolean enablePrecisionSawmill = true;
		public static boolean enableChemicaDissolution = true;
		public static boolean enableChemicalWasher = true;
		public static boolean enableChemicalCrystallizer = true;
		public static boolean enableSeismicVibrator = true;
		public static boolean enablePressurizedReactorChamber = true;
		public static boolean enableLiquidTanks = true;
		public static boolean enableFluidPlenisher = true;
		public static boolean enableLaser = true;
		public static boolean enableLaserAmplifyier = true;
		public static boolean enableLaserTractorBeam = true;
		public static boolean enableEntangloporter = true;
		public static boolean enableSolarNeutronActivator = true;
		public static boolean enableOredictionator = true;
		public static boolean enableResistiveHeater = true;
		public static boolean enableFormulaicAssembler = true;
		public static boolean enableFuelwoodHeater = true;
		public static boolean enableEnergyCubes = true;
		public static boolean enableObsidianTNT = true;
		public static boolean enableGasTanks = true;
		public static boolean enablePlasticBlocks = true;
		public static boolean enableSaltBlock = true;
		public static boolean enableUniversalCables = true;
		public static boolean enableMechanicalPipes = true;
		public static boolean enablePressurizedTubes = true;
		public static boolean enableLogisticalTransporter = true;
		public static boolean enableThermoConductors = true;
		public static boolean enableElectricBow = true;
		public static boolean enableEnergyTablet = true;
		public static boolean enableMachineUpgrades = true;
		public static boolean enableRobit = true;
		public static boolean enableAtomicDisassembler = true;
		public static boolean enableCircuits = true;
		public static boolean enablePortableTeleporter = true;
		public static boolean enableTeleporterCore = true;
		public static boolean enableConfigurator = true;
		public static boolean enableNetworkReader = true;
		public static boolean enableWalkieTalkie = true;
		public static boolean enableJetpacks = true;
		public static boolean enableScubaSet = true;
		public static boolean enableFreeRunners = true;
		public static boolean enableConfigurationCard = true;
		public static boolean enableCraftingFormula = true;
		public static boolean enableSeismicReader = true;
		public static boolean enableHDPEParts = true;
		public static boolean enableFlamethrower = true;
		public static boolean enableGaugeDropper = true;
		public static boolean enableTierInstaller = true;
		public static boolean enableHeatGenerator = true;
		public static boolean enableSolarGenerator = true;
		public static boolean enableGasGenerator = true;
		public static boolean enableBioGenerator = true;
		public static boolean enableAdvSolarGenerator = true;
		public static boolean enableWindGenerator = true;
		public static boolean enableTurbineRotor = true;
		public static boolean enableRotationalComplex = true;
		public static boolean enableElectromagneticCoil = true;
		public static boolean enableTurbineCasing = true;
		public static boolean enableTurbineValve = true;
		public static boolean enableTurbineVent = true;
		public static boolean enableSaturatingCondenser = true;
		public static boolean enableReactorController = true;
		public static boolean enableReactorFrame = true;
		public static boolean enableReactorPort = true;
		public static boolean enableReactorAdapter = true;
		public static boolean enableReactorGlass = true;
		public static boolean enableReactorMatrix = true;
		public static boolean enableSolarPanel = true;
		public static boolean enableTurbineBlade = true;

	}
}
