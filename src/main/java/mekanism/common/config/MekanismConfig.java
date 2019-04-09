package mekanism.common.config;

import java.util.Set;
import mekanism.common.util.UnitDisplayUtils.EnergyType;
import mekanism.common.util.UnitDisplayUtils.TempType;

public class MekanismConfig {

    public static class general {

        public static TypeConfigManager machinesManager = new TypeConfigManager();

        public static boolean controlCircuitOreDict = true;
        public static boolean logPackets = false;
        public static boolean dynamicTankEasterEgg = false;
        public static boolean voiceServerEnabled = false;
        public static boolean cardboardSpawners = true;
        public static boolean enableWorldRegeneration = true;
        public static boolean spawnBabySkeletons = true;
        public static int obsidianTNTBlastRadius = 12;
        public static int osmiumPerChunk = 12;
        public static int osmiumMaxVeinSize = 8;
        public static int copperPerChunk = 16;
        public static int copperMaxVeinSize = 8;
        public static int tinPerChunk = 14;
        public static int tinMaxVeinSize = 8;
        public static int saltPerChunk = 2;
        public static int saltMaxVeinSize = 6;
        public static int obsidianTNTDelay = 100;
        public static int UPDATE_DELAY = 10;
        public static int VOICE_PORT = 36123;
        public static int maxUpgradeMultiplier = 10;
        public static int userWorldGenVersion = 0;
        public static double ENERGY_PER_REDSTONE = 10000;
        public static int ETHENE_BURN_TIME = 40;
        public static double DISASSEMBLER_USAGE = 10;
        public static EnergyType energyUnit = EnergyType.J;
        public static TempType tempUnit = TempType.K;
        public static double TO_IC2 = 0.1;
        public static double TO_RF = 0.4;
        public static double TO_TESLA = 0.4;
        public static double TO_FORGE = 0.4;
        public static double FROM_H2 = 200;
        public static double FROM_IC2 = 10;
        public static double FROM_RF = 2.5;
        public static double FROM_TESLA = 2.5;
        public static double FROM_FORGE = 2.5;
        public static int laserRange = 64;
        public static double laserEnergyNeededPerHardness = 100000;
        public static double minerSilkMultiplier = 6;
        public static boolean blacklistIC2 = false;
        public static boolean blacklistRF = false;
        public static boolean blacklistTesla = false;
        public static boolean blacklistForge = false;
        public static boolean destroyDisabledBlocks = true;
        public static int digitalMinerMaxRadius = 32;
        public static boolean prefilledGasTanks = true;
        public static double armoredJetpackDamageRatio = 0.8;
        public static int armoredJetpackDamageMax = 115;
        public static boolean aestheticWorldDamage = true;
        public static boolean opsBypassRestrictions = false;
        public static int maxJetpackGas = 24000;
        public static int maxScubaGas = 24000;
        public static int maxFlamethrowerGas = 24000;
        public static int maxPumpRange = 80;
        public static boolean pumpWaterSources = false;
        public static int maxPlenisherNodes = 4000;
        public static double evaporationHeatDissipation = 0.02;
        public static double evaporationTempMultiplier = 0.1;
        public static double evaporationSolarMultiplier = 0.2;
        public static double evaporationMaxTemp = 3000;
        public static double energyPerHeat = 1000;
        public static double maxEnergyPerSteam = 100;
        public static double superheatingHeatTransfer = 10000;
        public static double heatPerFuelTick = 4;
        public static boolean allowTransmitterAlloyUpgrade = true;
        public static boolean allowChunkloading = true;
        public static boolean allowProtection = true;
        public static int portableTeleporterDelay = 0;
        public static double quantumEntangloporterEnergyTransfer = 16000000;
        public static double sawdustChancePlank = 0.25;
        public static double sawdustChanceLog = 1;
    }

    public static class client {

        public static boolean enablePlayerSounds = true;
        public static boolean enableMachineSounds = true;
        public static boolean holidays = true;//Was going to remove?
        public static float baseSoundVolume = 1F;
        public static boolean machineEffects = true;
        public static boolean enableAmbientLighting;
        public static int ambientLightingLevel = 15;
        public static boolean opaqueTransmitters = false;
        public static boolean allowConfiguratorModeScroll = true;
        public static boolean enableMultiblockFormationParticles = true;
    }

    public static class usage {

        public static double enrichmentChamberUsage = 50;
        public static double osmiumCompressorUsage = 100;
        public static double combinerUsage = 50;
        public static double crusherUsage = 50;
        public static double metallurgicInfuserUsage = 50;
        public static double purificationChamberUsage = 200;
        public static double energizedSmelterUsage = 50;
        public static double digitalMinerUsage = 100;
        public static double electricPumpUsage = 100;
        public static double rotaryCondensentratorUsage = 50;
        public static double oxidationChamberUsage = 200;
        public static double chemicalInfuserUsage = 200;
        public static double chemicalInjectionChamberUsage = 400;
        public static double precisionSawmillUsage = 50;
        public static double chemicalDissolutionChamberUsage = 400;
        public static double chemicalWasherUsage = 200;
        public static double chemicalCrystallizerUsage = 400;
        public static double seismicVibratorUsage = 50;
        public static double pressurizedReactionBaseUsage = 5;
        public static double fluidicPlenisherUsage = 100;
        public static double laserUsage = 5000;
        public static double heavyWaterElectrolysisUsage = 800;
        public static double formulaicAssemblicatorUsage = 100;
        public static int teleporterBaseUsage = 1000;
        public static int teleporterDistanceUsage = 10;
        public static int teleporterDimensionPenalty = 10000;
    }

    public static class generators {

        public static TypeConfigManager generatorsManager = new TypeConfigManager();

        public static double advancedSolarGeneration = 300;
        public static double bioGeneration = 350;
        public static double heatGeneration = 150;
        public static double heatGenerationLava = 5;
        public static double heatGenerationNether = 100;
        public static double solarGeneration = 50;

        public static double windGenerationMin = 60;
        public static double windGenerationMax = 480;

        public static int windGenerationMinY = 24;
        public static int windGenerationMaxY = 255;

        public static Set<Integer> windGenerationDimBlacklist;

        public static int turbineBladesPerCoil = 4;
        public static double turbineVentGasFlow = 16000;
        public static double turbineDisperserGasFlow = 640;
        public static int condenserRate = 32000;

        public static double energyPerFusionFuel = 5E6;
    }

    public static class tools {

        public static double armorSpawnRate = 0.03;
    }
}