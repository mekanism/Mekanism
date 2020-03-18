package mekanism.common.config;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedConfigValue;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedEnumValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.GasTankTier;
import mekanism.common.util.UnitDisplayUtils.EnergyType;
import mekanism.common.util.UnitDisplayUtils.TempType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

//TODO: Make electrolytic separator recipes actually scale with the config value energy in case From H2 changes
public class GeneralConfig extends BaseMekanismConfig {

    private static final String CONVERSION_CATEGORY = "energy_conversion";
    private static final String DISASSEMBLER_CATEGORY = "atomic_disassembler";
    private static final String EVAPORATION_CATEGORY = "thermal_evaporation";
    private static final String ENTANGLOPORTER_CATEGORY = "quantum_entangloporter";

    private final ForgeConfigSpec configSpec;

    public final CachedBooleanValue logPackets;
    public final CachedBooleanValue dynamicTankEasterEgg;
    public final CachedConfigValue<List<String>> cardboardModBlacklist;
    public final CachedIntValue UPDATE_DELAY;
    public final CachedDoubleValue FROM_IC2;
    public final CachedDoubleValue TO_IC2;
    public final CachedDoubleValue FROM_FORGE;
    public final CachedDoubleValue TO_FORGE;
    public final CachedDoubleValue FROM_H2;
    public final CachedIntValue ETHENE_BURN_TIME;
    public final CachedIntValue disassemblerEnergyUsage;
    public final CachedIntValue disassemblerEnergyUsageHoe;
    public final CachedIntValue disassemblerEnergyUsageShovel;
    public final CachedIntValue disassemblerEnergyUsageAxe;
    public final CachedIntValue disassemblerEnergyUsageWeapon;
    public final CachedIntValue disassemblerMiningRange;
    public final CachedIntValue disassemblerMiningCount;
    public final CachedBooleanValue disassemblerSlowMode;
    public final CachedBooleanValue disassemblerFastMode;
    public final CachedBooleanValue disassemblerVeinMining;
    public final CachedBooleanValue disassemblerExtendedMining;
    public final CachedIntValue disassemblerDamageMin;
    public final CachedIntValue disassemblerDamageMax;
    public final CachedDoubleValue disassemblerBatteryCapacity;
    public final CachedIntValue maxUpgradeMultiplier;
    public final CachedIntValue minerSilkMultiplier;
    public final CachedBooleanValue prefilledGasTanks;
    public final CachedIntValue armoredJetpackArmor;
    public final CachedIntValue armoredJetpackToughness;
    public final CachedBooleanValue aestheticWorldDamage;
    public final CachedBooleanValue opsBypassRestrictions;
    public final CachedIntValue maxJetpackGas;
    public final CachedIntValue maxScubaGas;
    public final CachedIntValue maxFlamethrowerGas;
    public final CachedIntValue maxPumpRange;
    public final CachedBooleanValue pumpWaterSources;
    public final CachedIntValue maxPlenisherNodes;
    public final CachedFloatValue evaporationHeatDissipation;
    public final CachedDoubleValue evaporationTempMultiplier;
    public final CachedDoubleValue evaporationSolarMultiplier;
    public final CachedDoubleValue evaporationMaxTemp;
    public final CachedDoubleValue energyPerHeat;
    public final CachedDoubleValue maxEnergyPerSteam;
    public final CachedDoubleValue superheatingHeatTransfer;
    public final CachedDoubleValue heatPerFuelTick;
    public final CachedBooleanValue allowTransmitterAlloyUpgrade;
    public final CachedBooleanValue allowChunkloading;
    public final CachedBooleanValue allowProtection;
    public final CachedIntValue portableTeleporterDelay;
    public final CachedDoubleValue quantumEntangloporterEnergyBuffer;
    public final CachedIntValue quantumEntangloporterFluidBuffer;
    public final CachedIntValue quantumEntangloporterGasBuffer;
    public final CachedBooleanValue blacklistIC2;
    public final CachedBooleanValue blacklistForge;
    public final CachedIntValue laserRange;
    public final CachedIntValue laserEnergyNeededPerHardness;
    public final CachedBooleanValue voidInvalidGases;
    public final CachedIntValue digitalMinerMaxRadius;
    public final CachedIntValue digitalMinerTicksPerMine;
    public CachedEnumValue<EnergyType> energyUnit;
    public CachedEnumValue<TempType> tempUnit;

    GeneralConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("General Config. This config is synced from server to client.").push("general");
        //TODO: Move things to different files where it makes more sense
        // Also make config options for the different gear types, where previously we didn't allow them to be configured

        logPackets = CachedBooleanValue.wrap(this, builder.comment("Log Mekanism packet names. Debug setting.")
              .define("logPackets", false));
        dynamicTankEasterEgg = CachedBooleanValue.wrap(this, builder.comment("Audible sparkles.")
              .define("dynamicTankEasterEgg", false));
        cardboardModBlacklist = CachedConfigValue.wrap(this, builder.comment("Any mod ids added to this list will not be able to have any of their blocks, picked up by the cardboard box.")
              .define("cardboardModBlacklist", new ArrayList<>()));
        UPDATE_DELAY = CachedIntValue.wrap(this, builder.comment("How many ticks must pass until a block's active state can sync with the client.")
              .define("UPDATE_DELAY", 10));

        builder.comment("Energy Conversion Rate Settings").push(CONVERSION_CATEGORY);
        blacklistIC2 = CachedBooleanValue.wrap(this, builder.comment("Disables IC2 power integration. Requires world restart (server-side option in SMP).")
              .worldRestart()
              .define("blacklistIC2", false));
        FROM_IC2 = CachedDoubleValue.wrap(this, builder.comment("Conversion multiplier from EU to Joules (EU * JoulesToEU = Joules)")
              .define("JoulesToEU", 10D));
        TO_IC2 = CachedDoubleValue.wrap(this, builder.comment("Conversion multiplier from Joules to EU (Joules * EUToJoules = EU)")
              .define("EUToJoules", 0.1D));
        blacklistForge = CachedBooleanValue.wrap(this, builder.comment("Disables Forge Energy (FE,RF,IF,uF,CF) power integration. Requires world restart (server-side option in SMP).")
              .worldRestart()
              .define("blacklistForge", false));
        FROM_FORGE = CachedDoubleValue.wrap(this, builder.comment("Conversion multiplier from Forge Energy to Joules (FE * JoulesToForge = Joules)")
              .define("JoulesToForge", 2.5D));
        TO_FORGE = CachedDoubleValue.wrap(this, builder.comment("Conversion multiplier from Joules to Forge Energy (Joules * ForgeToJoules = FE)")
              .define("ForgeToJoules", 0.4D));
        FROM_H2 = CachedDoubleValue.wrap(this, builder.comment("How much energy is produced per mB of Hydrogen, also affects Electrolytic Separator usage, Ethylene burn rate and Gas generator energy capacity.")
              .define("HydrogenEnergyDensity", 200D));
        ETHENE_BURN_TIME = CachedIntValue.wrap(this, builder.comment("Burn time for Ethylene (1mB hydrogen + 2*bioFuel/tick*200ticks/100mB * 20x efficiency bonus).")
              .define("EthyleneBurnTime", 40));
        builder.pop();

        builder.comment("Atomic Disassembler Settings").push(DISASSEMBLER_CATEGORY);
        disassemblerEnergyUsage = CachedIntValue.wrap(this, builder.comment("Base Energy (Joules) usage of the Atomic Disassembler. (Gets multiplied by speed factor)")
              .define("energyUsage", 10));
        disassemblerEnergyUsageHoe = CachedIntValue.wrap(this, builder.comment("Cost in Joules of using the Atomic Disassembler as a hoe.")
              .define("energyUsageHoe", 10));
        disassemblerEnergyUsageShovel = CachedIntValue.wrap(this, builder.comment("Cost in Joules of using the Atomic Disassembler as a shovel for making paths.")
              .define("energyUsageShovel", 10));
        disassemblerEnergyUsageAxe = CachedIntValue.wrap(this, builder.comment("Cost in Joules of using the Atomic Disassembler as an axe for stripping logs.")
              .define("energyUsageAxe", 10));
        disassemblerEnergyUsageWeapon = CachedIntValue.wrap(this, builder.comment("Cost in Joules of using the Atomic Disassembler as a weapon.")
              .define("energyUsageWeapon", 2_000));
        disassemblerMiningRange = CachedIntValue.wrap(this, builder.comment("The Range of the Atomic Disassembler Extended Vein Mining.")
              .define("miningRange", 10));
        disassemblerMiningCount = CachedIntValue.wrap(this, builder.comment("The max Atomic Disassembler Vein Mining Block Count.")
              .define("miningCount", 128));
        disassemblerSlowMode = CachedBooleanValue.wrap(this, builder.comment("Enable the 'Slow' mode for the Atomic Disassembler.")
              .define("slowMode", true));
        disassemblerFastMode = CachedBooleanValue.wrap(this, builder.comment("Enable the 'Fast' mode for the Atomic Disassembler.")
              .define("fastMode", true));
        disassemblerVeinMining = CachedBooleanValue.wrap(this, builder.comment("Enable the 'Vein Mining' mode for the Atomic Disassembler.")
              .define("veinMining", true));
        disassemblerExtendedMining = CachedBooleanValue.wrap(this, builder.comment("Enable the 'Extended Vein Mining' mode for the Atomic Disassembler. (Allows vein mining everything not just ores/logs)")
              .define("extendedMining", true));
        disassemblerDamageMin = CachedIntValue.wrap(this, builder.comment("The amount of damage the Atomic Disassembler does when it is out of power. (Value is in number of half hearts)")
              .define("damageMin", 4));
        disassemblerDamageMax = CachedIntValue.wrap(this, builder.comment("The amount of damage the Atomic Disassembler does when it has at least DisassemblerEnergyUsageWeapon power stored. (Value is in number of half hearts)")
              .define("damageMax", 20));
        disassemblerBatteryCapacity = CachedDoubleValue.wrap(this, builder.comment("Maximum amount (joules) of energy the Atomic Disassembler can contain")
              .worldRestart()
              .defineInRange("batteryCapacity", 1_000_000D, 0, Double.MAX_VALUE));
        builder.pop();

        //If this is less than 1, upgrades make machines worse. If less than 0, I don't even know.
        maxUpgradeMultiplier = CachedIntValue.wrap(this, builder.comment("Base factor for working out machine performance with upgrades - UpgradeModifier * (UpgradesInstalled/UpgradesPossible).")
              .defineInRange("maxUpgradeMultiplier", 10, 1, Integer.MAX_VALUE));
        minerSilkMultiplier = CachedIntValue.wrap(this, builder.comment("Energy multiplier for using silk touch mode with the Digital Miner.")
              .define("minerSilkMultiplier", 6));
        prefilledGasTanks = CachedBooleanValue.wrap(this, builder.comment("Add filled creative gas tanks to creative/JEI.")
              .define("prefilledGasTanks", true));
        armoredJetpackArmor = CachedIntValue.wrap(this, builder.comment("Armor value of the Armored Jetpack.")
              .define("armoredJetpackArmor", 12));
        armoredJetpackToughness = CachedIntValue.wrap(this, builder.comment("Toughness value of the Armored Jetpack.")
              .define("armoredJetpackToughness", 2));
        aestheticWorldDamage = CachedBooleanValue.wrap(this, builder.comment("If enabled, lasers can break blocks and the flamethrower starts fires.")
              .define("aestheticWorldDamage", true));
        opsBypassRestrictions = CachedBooleanValue.wrap(this, builder.comment("Ops can bypass the block security restrictions if enabled.")
              .define("opsBypassRestrictions", false));
        maxJetpackGas = CachedIntValue.wrap(this, builder.comment("Jetpack Gas Tank capacity in mB.")
              .define("maxJetpackGas", 24_000));
        maxScubaGas = CachedIntValue.wrap(this, builder.comment("Scuba Tank Gas Tank capacity in mB.")
              .define("maxScubaGas", 24_000));
        maxFlamethrowerGas = CachedIntValue.wrap(this, builder.comment("Flamethrower Gas Tank capacity in mB.")
              .define("maxFlamethrowerGas", 24_000));
        maxPumpRange = CachedIntValue.wrap(this, builder.comment("Maximum block distance to pull fluid from for the Electric Pump.")
              .define("maxPumpRange", 80));
        pumpWaterSources = CachedBooleanValue.wrap(this, builder.comment("If enabled makes Water and Heavy Water blocks be removed from the world on pump.")
              .define("pumpWaterSources", false));
        maxPlenisherNodes = CachedIntValue.wrap(this, builder.comment("Fluidic Plenisher stops after this many blocks.")
              .define("maxPlenisherNodes", 4_000));

        builder.comment("Thermal Evaporation Plant Settings").push(EVAPORATION_CATEGORY);
        evaporationHeatDissipation = CachedFloatValue.wrap(this, builder.comment("Thermal Evaporation Tower heat loss per tick.")
              .define("heatDissipation", 0.02D));
        evaporationTempMultiplier = CachedDoubleValue.wrap(this, builder.comment("Temperature to amount produced ratio for Thermal Evaporation Tower.")
              .define("tempMultiplier", 0.1D));
        evaporationSolarMultiplier = CachedDoubleValue.wrap(this, builder.comment("Heat to absorb per Solar Panel array of Thermal Evaporation Tower.")
              .define("solarMultiplier", 0.2D));
        evaporationMaxTemp = CachedDoubleValue.wrap(this, builder.comment("Max Temperature of the Thermal Evaporation Tower.")
              .defineInRange("maxTemp", 3_000D, 1, Double.MAX_VALUE));
        builder.pop();

        energyPerHeat = CachedDoubleValue.wrap(this, builder.comment("Joules required by the Resistive Heater to produce one unit of heat. Also affects Thermoelectric Boiler's Water->Steam rate.")
              .define("energyPerHeat", 1_000D));
        maxEnergyPerSteam = CachedDoubleValue.wrap(this, builder.comment("Maximum Joules per mB of Steam. Also affects Thermoelectric Boiler.")
              .define("maxEnergyPerSteam", 100D));
        superheatingHeatTransfer = CachedDoubleValue.wrap(this, builder.comment("Amount of heat each Boiler heating element produces.")
              .define("superheatingHeatTransfer", 10_000D));
        heatPerFuelTick = CachedDoubleValue.wrap(this, builder.comment("Amount of heat produced per fuel tick of a fuel's burn time in the Fuelwood Heater.")
              .define("heatPerFuelTick", 4D));
        allowTransmitterAlloyUpgrade = CachedBooleanValue.wrap(this, builder.comment("Allow right clicking on Cables/Pipes/Tubes with alloys to upgrade the tier.")
              .define("allowTransmitterAlloyUpgrade", true));
        allowChunkloading = CachedBooleanValue.wrap(this, builder.comment("Disable to make the anchor upgrade not do anything.")
              .define("allowChunkloading", true));
        allowProtection = CachedBooleanValue.wrap(this, builder.comment("Enable the security system for players to prevent others from accessing their machines. Does NOT affect Frequencies.")
              .define("allowProtection", true));
        portableTeleporterDelay = CachedIntValue.wrap(this, builder.comment("Delay in ticks before a player is teleported after clicking the Teleport button in the portable teleporter.")
              .define("portableTeleporterDelay", 0));

        builder.comment("Quantum Entangloporter Settings").push(ENTANGLOPORTER_CATEGORY);
        quantumEntangloporterEnergyBuffer = CachedDoubleValue.wrap(this, builder.comment("Maximum energy buffer (Mekanism Joules) of an Entangoloporter frequency - i.e. the maximum transfer per tick per frequency. Default is ultimate tier energy cube capacity.")
              .worldRestart()
              .defineInRange("energyBuffer", EnergyCubeTier.ULTIMATE.getBaseMaxEnergy(), 0, Double.MAX_VALUE));
        quantumEntangloporterFluidBuffer = CachedIntValue.wrap(this, builder.comment("Maximum fluid buffer (mb) of an Entangoloporter frequency - i.e. the maximum transfer per tick per frequency. Default is ultimate tier tank capacity.")
              .worldRestart()
              .defineInRange("fluidBuffer", GasTankTier.ULTIMATE.getBaseStorage(), 0, Integer.MAX_VALUE));
        quantumEntangloporterGasBuffer = CachedIntValue.wrap(this, builder.comment("Maximum fluid buffer (mb) of an Entangoloporter frequency - i.e. the maximum transfer per tick per frequency. Default is ultimate tier tank capacity.")
              .worldRestart()
              .defineInRange("gasBuffer", GasTankTier.ULTIMATE.getBaseStorage(), 0, Integer.MAX_VALUE));
        builder.pop();

        laserRange = CachedIntValue.wrap(this, builder.comment("How far (in blocks) a laser can travel.")
              .define("laserRange", 64));
        laserEnergyNeededPerHardness = CachedIntValue.wrap(this, builder.comment("Energy needed to destroy or attract blocks with a Laser (per block hardness level).")
              .define("laserEnergyNeededPerHardness", 100_000));
        digitalMinerMaxRadius = CachedIntValue.wrap(this, builder.comment("Maximum radius in blocks that the Digital Miner can reach. (Increasing this may have negative effects on stability "
                                                                          + "and/or performance. We strongly recommend you leave it at the default value).")
              .defineInRange("digitalMinerMaxRadius", 32, 1, Integer.MAX_VALUE));
        digitalMinerTicksPerMine = CachedIntValue.wrap(this, builder.comment("Number of ticks required to mine a single block with a Digital Miner (without any upgrades).")
              .defineInRange("digitalMinerTicksPerMine", 80, 1, Integer.MAX_VALUE));
        energyUnit = CachedEnumValue.wrap(this, builder.comment("Displayed energy type in Mekanism GUIs.")
              .defineEnum("energyType", EnergyType.FE));
        tempUnit = CachedEnumValue.wrap(this, builder.comment("Displayed temperature unit in Mekanism GUIs.")
              .defineEnum("temperatureUnit", TempType.K));

        //TODO: FIXME, currently is broken in 1.14 (at least in singleplayer) due to recipes not existing when it checks them for voiding the gas.
        voidInvalidGases = CachedBooleanValue.wrap(this, builder.comment("Should machines void the gas inside of them on load if there is no recipe using that gas. Note: Currently broken in 1.14 and always voids the gas.")
              .define("voidInvalidGases", false));

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "general";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }
}