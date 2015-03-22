package mekanism.api;

import java.util.HashMap;
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
		public static boolean creativeOverrideElectricChest = true;
		public static boolean spawnBabySkeletons = true;
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
		public static double DISASSEMBLER_USAGE = 10;
		public static EnergyType activeType = EnergyType.J;
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
		public static boolean enableAmbientLighting;
		public static int ambientLightingLevel;
		public static boolean prefilledPortableTanks;
	}

	public static class client
	{
		public static boolean enablePlayerSounds = true;
		public static boolean enableMachineSounds = true;
		public static boolean fancyUniversalCableRender = true;
		public static boolean holidays = true;
		public static float baseSoundVolume = 1F;
		public static boolean machineEffects = true;
		public static boolean oldTransmitterRender = false;
		public static boolean replaceSoundsWhenResuming = true;
		public static boolean renderCTM = true;
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
	}

	public static class generators
	{
		public static double advancedSolarGeneration;
		public static double bioGeneration;
		public static double heatGeneration;
		public static double heatGenerationLava;
		public static double heatGenerationNether;
		public static double solarGeneration;

		public static double windGenerationMin;
		public static double windGenerationMax;

		public static int windGenerationMinY;
		public static int windGenerationMaxY;
	}

	public static class tools
	{
		public static double armorSpawnRate;
	}
}
