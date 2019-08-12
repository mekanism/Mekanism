package mekanism.common.config;

import mekanism.common.MekanismBlock;
import mekanism.common.util.UnitDisplayUtils.EnergyType;
import mekanism.common.util.UnitDisplayUtils.TempType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.config.ModConfig.Type;

//TODO
public class GeneralConfig implements IMekanismConfig {

    private static final String ENABLED_CATEGORY = "enabled_machines";

    private final ForgeConfigSpec configSpec;

    public final BooleanValue controlCircuitOreDict;
    public final BooleanValue logPackets;
    public final BooleanValue dynamicTankEasterEgg;
    public final BooleanValue voiceServerEnabled;
    public final BooleanValue cardboardSpawners;
    public final BooleanValue enableWorldRegeneration;
    public final BooleanValue spawnBabySkeletons;
    public final IntValue obsidianTNTDelay;
    public final IntValue obsidianTNTBlastRadius;
    public final IntValue UPDATE_DELAY;
    public final IntValue osmiumPerChunk;
    public final IntValue osmiumMaxVeinSize;
    public final IntValue copperPerChunk;
    public final IntValue copperMaxVeinSize;
    public final IntValue tinPerChunk;
    public final IntValue tinMaxVeinSize;
    public final IntValue saltPerChunk;
    public final IntValue saltMaxVeinSize;
    public final IntValue userWorldGenVersion;
    public final DoubleValue FROM_IC2;
    public final DoubleValue TO_IC2;
    public final DoubleValue FROM_FORGE;
    public final DoubleValue TO_FORGE;
    public final DoubleValue FROM_H2;
    public final IntValue ETHENE_BURN_TIME;
    public final DoubleValue ENERGY_PER_REDSTONE;
    public final IntValue disassemblerEnergyUsage;
    public final IntValue disassemblerEnergyUsageHoe;
    public final IntValue disassemblerEnergyUsageWeapon;
    public final IntValue disassemblerMiningRange;
    public final IntValue disassemblerMiningCount;
    public final BooleanValue disassemblerSlowMode;
    public final BooleanValue disassemblerFastMode;
    public final BooleanValue disassemblerVeinMining;
    public final BooleanValue disassemblerExtendedMining;
    public final IntValue disassemblerDamageMin;
    public final IntValue disassemblerDamageMax;
    public final DoubleValue disassemblerBatteryCapacity;
    public final IntValue VOICE_PORT;
    //If this is less than 1, upgrades make machines worse. If less than 0, I don't even know.
    public final IntValue maxUpgradeMultiplier;
    public final IntValue minerSilkMultiplier;
    public final BooleanValue prefilledGasTanks;
    public final IntValue armoredJetpackArmor;
    public final IntValue armoredJetpackToughness;
    public final BooleanValue aestheticWorldDamage;
    public final BooleanValue opsBypassRestrictions;
    public final IntValue maxJetpackGas;
    public final IntValue maxScubaGas;
    public final IntValue maxFlamethrowerGas;
    public final IntValue maxPumpRange;
    public final BooleanValue pumpWaterSources;
    public final IntValue maxPlenisherNodes;
    public final DoubleValue evaporationHeatDissipation;
    public final DoubleValue evaporationTempMultiplier;
    public final DoubleValue evaporationSolarMultiplier;
    public final DoubleValue evaporationMaxTemp;
    public final DoubleValue energyPerHeat;
    public final DoubleValue maxEnergyPerSteam;
    public final DoubleValue superheatingHeatTransfer;
    public final DoubleValue heatPerFuelTick;
    public final BooleanValue allowTransmitterAlloyUpgrade;
    public final BooleanValue allowChunkloading;
    public final BooleanValue allowProtection;
    public final IntValue portableTeleporterDelay;
    public final DoubleValue quantumEntangloporterEnergyTransfer;
    public final IntValue quantumEntangloporterFluidBuffer;
    public final IntValue quantumEntangloporterGasBuffer;
    public final BooleanValue blacklistIC2;
    public final BooleanValue blacklistForge;
    public EnumValue<EnergyType> energyUnit;
    public EnumValue<TempType> tempUnit;
    public final IntValue laserRange;
    public final IntValue laserEnergyNeededPerHardness;
    public final BooleanValue destroyDisabledBlocks;
    public final BooleanValue voidInvalidGases;
    public final IntValue digitalMinerMaxRadius;
    public final DoubleValue sawdustChancePlank;
    public final DoubleValue sawdustChanceLog;

    GeneralConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("General Config");

        //TODO: Evaluate other mekanism blocks for if support should be added
        builder.comment("Enabled Machines").push(ENABLED_CATEGORY);
        MekanismConfig.addEnabledBlocksCategory(builder, MekanismBlock.values());
        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "mekanism-general.toml";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.COMMON;
    }
}