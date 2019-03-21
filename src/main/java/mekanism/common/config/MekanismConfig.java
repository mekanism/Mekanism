package mekanism.common.config;

import java.util.Set;
import mekanism.common.util.UnitDisplayUtils.EnergyType;
import mekanism.common.util.UnitDisplayUtils.TempType;

public class MekanismConfig {

    public static class general {

        public static TypeConfigManager machinesManager = new TypeConfigManager();

        public static boolean updateNotifications = true;
        public static boolean controlCircuitOreDict = true;
        public static boolean logPackets = false;
        public static boolean dynamicTankEasterEgg = false;
        public static boolean cardboardSpawners = true;
        public static boolean enableWorldRegeneration = true;
        public static boolean spawnBabySkeletons = true;
        public static int obsidianTNTBlastRadius = 12;
        public static int osmiumPerChunk = 12;
        public static int copperPerChunk = 16;
        public static int tinPerChunk = 14;
        public static int saltPerChunk = 2;
        public static int obsidianTNTDelay = 100;
        public static int UPDATE_DELAY = 10;
        public static int maxUpgradeMultiplier = 10;
        public static int userWorldGenVersion = 0;
        public static double ENERGY_PER_REDSTONE = 10000;
        public static int ETHENE_BURN_TIME = 40;
        public static double DISASSEMBLER_USAGE = 10;
        public static EnergyType energyUnit = EnergyType.J;
        public static TempType tempUnit = TempType.K;
        public static double TO_IC2;
        public static double TO_RF;
        public static double TO_TESLA;
        public static double TO_FORGE;
        public static double FROM_H2;
        public static double FROM_IC2;
        public static double FROM_RF;
        public static double FROM_TESLA;
        public static double FROM_FORGE;
        public static int laserRange;
        public static double laserEnergyNeededPerHardness;
        public static double minerSilkMultiplier = 6;
        public static boolean blacklistIC2;
        public static boolean blacklistRF;
        public static boolean blacklistTesla;
        public static boolean blacklistForge;
        public static boolean destroyDisabledBlocks;
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
        public static boolean allowChunkloading;
        public static boolean allowProtection = true;
        public static int portableTeleporterDelay;
        public static double quantumEntangloporterEnergyTransfer;
    }

    public static class client {

        public static boolean enablePlayerSounds = true;
        public static boolean enableMachineSounds = true;
        public static boolean holidays = true;
        public static float baseSoundVolume = 1F;
        public static boolean machineEffects = true;
        public static boolean oldTransmitterRender = false;
        public static boolean replaceSoundsWhenResuming = true;
        public static boolean enableAmbientLighting;
        public static int ambientLightingLevel;
        public static boolean opaqueTransmitters = false;
        public static boolean allowConfiguratorModeScroll;
        public static boolean enableMultiblockFormationParticles = true;
    }

    public static class usage {

        public static double enrichmentChamberUsage;
        public static double osmiumCompressorUsage;
        public static double combinerUsage;
        public static double crusherUsage;
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
        public static int teleporterBaseUsage;
        public static int teleporterDistanceUsage;
        public static int teleporterDimensionPenalty;
    }

    public static class generators {

        public static TypeConfigManager generatorsManager = new TypeConfigManager();

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

        public static Set<Integer> windGenerationDimBlacklist;

        public static int turbineBladesPerCoil;
        public static double turbineVentGasFlow;
        public static double turbineDisperserGasFlow;
        public static int condenserRate;

        public static double energyPerFusionFuel;
    }

    public static class tools {

        public static double armorSpawnRate;
    }
}
