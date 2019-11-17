package mekanism.common.config;

import mekanism.common.tier.GasTankTier;
import mekanism.common.util.UnitDisplayUtils.EnergyType;
import mekanism.common.util.UnitDisplayUtils.TempType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.config.ModConfig.Type;

//TODO: FixME - forge really does not like using floats as config values
public class GeneralConfig implements IMekanismConfig {

    private static final String WORLD_GEN_CATEGORY = "world_generation";
    private static final String CONVERSION_CATEGORY = "energy_conversion";
    private static final String DISASSEMBLER_CATEGORY = "atomic_disassembler";
    private static final String EVAPORATION_CATEGORY = "thermal_evaporation";
    private static final String ENTANGLOPORTER_CATEGORY = "quantum_entangloporter";

    //TODO: final
    private ForgeConfigSpec configSpec;

    public final BooleanValue logPackets;
    public final BooleanValue dynamicTankEasterEgg;
    public final BooleanValue cardboardSpawners;
    public final BooleanValue enableWorldRegeneration;
    public final ConfigValue<Integer> UPDATE_DELAY;
    public final IntValue osmiumPerChunk;
    public final IntValue osmiumMaxVeinSize;
    public final IntValue copperPerChunk;
    public final IntValue copperMaxVeinSize;
    public final IntValue tinPerChunk;
    public final IntValue tinMaxVeinSize;
    public final IntValue saltPerChunk;
    public final IntValue saltMaxVeinSize;
    public final ConfigValue<Integer> userWorldGenVersion;
    public final ConfigValue<Double> FROM_IC2;
    public final ConfigValue<Double> TO_IC2;
    public final ConfigValue<Double> FROM_FORGE;
    public final ConfigValue<Double> TO_FORGE;
    public final ConfigValue<Double> FROM_H2;
    public final ConfigValue<Integer> ETHENE_BURN_TIME;
    public final ConfigValue<Double> ENERGY_PER_REDSTONE;
    public final ConfigValue<Integer> disassemblerEnergyUsage;
    public final ConfigValue<Integer> disassemblerEnergyUsageHoe;
    public final ConfigValue<Integer> disassemblerEnergyUsageWeapon;
    public final ConfigValue<Integer> disassemblerMiningRange;
    public final ConfigValue<Integer> disassemblerMiningCount;
    public final BooleanValue disassemblerSlowMode;
    public final BooleanValue disassemblerFastMode;
    public final BooleanValue disassemblerVeinMining;
    public final BooleanValue disassemblerExtendedMining;
    public final ConfigValue<Integer> disassemblerDamageMin;
    public final ConfigValue<Integer> disassemblerDamageMax;
    public final DoubleValue disassemblerBatteryCapacity;
    public final IntValue maxUpgradeMultiplier;
    public final ConfigValue<Integer> minerSilkMultiplier;
    public final BooleanValue prefilledGasTanks;
    public final ConfigValue<Integer> armoredJetpackArmor;
    public final ConfigValue<Integer> armoredJetpackToughness;
    public final BooleanValue aestheticWorldDamage;
    public final BooleanValue opsBypassRestrictions;
    public final ConfigValue<Integer> maxJetpackGas;
    public final ConfigValue<Integer> maxScubaGas;
    public final ConfigValue<Integer> maxFlamethrowerGas;
    public final ConfigValue<Integer> maxPumpRange;
    public final BooleanValue pumpWaterSources;
    public final ConfigValue<Integer> maxPlenisherNodes;
    public final FloatValue evaporationHeatDissipation;
    public final ConfigValue<Double> evaporationTempMultiplier;
    public final ConfigValue<Double> evaporationSolarMultiplier;
    public final ConfigValue<Double> evaporationMaxTemp;
    public final ConfigValue<Double> energyPerHeat;
    public final ConfigValue<Double> maxEnergyPerSteam;
    public final ConfigValue<Double> superheatingHeatTransfer;
    public final ConfigValue<Double> heatPerFuelTick;
    public final BooleanValue allowTransmitterAlloyUpgrade;
    public final BooleanValue allowChunkloading;
    public final BooleanValue allowProtection;
    public final ConfigValue<Integer> portableTeleporterDelay;
    public final DoubleValue quantumEntangloporterEnergyTransfer;
    public final IntValue quantumEntangloporterFluidBuffer;
    public final IntValue quantumEntangloporterGasBuffer;
    public final BooleanValue blacklistIC2;
    public final BooleanValue blacklistForge;
    public final ConfigValue<Integer> laserRange;
    public final ConfigValue<Integer> laserEnergyNeededPerHardness;
    public final BooleanValue voidInvalidGases;
    public final IntValue digitalMinerMaxRadius;
    public final DoubleValue sawdustChanceLog;
    public EnumValue<EnergyType> energyUnit;
    public EnumValue<TempType> tempUnit;

    GeneralConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("General Config").push("general");
        //TODO: Move things to different files where it makes more sense
        // Also make config options for the different gear types, where previously we didn't allow them to be configured

        logPackets = builder.comment("Log Mekanism packet names. Debug setting.").define("logPackets", false);
        dynamicTankEasterEgg = builder.comment("Audible sparkles.").define("dynamicTankEasterEgg", false);
        cardboardSpawners = builder.comment("Allows vanilla spawners to be moved with a Cardboard Box.").define("cardboardSpawners", true);
        enableWorldRegeneration = builder.comment("Allows chunks to retrogen Mekanism ore blocks.").define("enableWorldRegeneration", false);
        //TODO: Unused
        UPDATE_DELAY = builder.comment("How many ticks must pass until a block's active state can sync with the client.").define("UPDATE_DELAY", 10);

        builder.comment("World Generation Settings").push(WORLD_GEN_CATEGORY);
        osmiumPerChunk = builder.comment("Chance that osmium generates in a chunk. (0 to Disable)").defineInRange("osmiumPerChunk", 12, 0, Integer.MAX_VALUE);
        osmiumMaxVeinSize = builder.comment("Max number of blocks in an osmium vein.").defineInRange("osmiumMaxVeinSize", 8, 1, Integer.MAX_VALUE);
        copperPerChunk = builder.comment("Chance that copper generates in a chunk. (0 to Disable)").defineInRange("copperPerChunk", 16, 0, Integer.MAX_VALUE);
        copperMaxVeinSize = builder.comment("Max number of blocks in a copper vein.").defineInRange("copperMaxVeinSize", 8, 1, Integer.MAX_VALUE);
        tinPerChunk = builder.comment("Chance that tin generates in a chunk. (0 to Disable)").defineInRange("tinPerChunk", 14, 0, Integer.MAX_VALUE);
        tinMaxVeinSize = builder.comment("Max number of blocks in a tin vein.").defineInRange("tinMaxVeinSize", 8, 1, Integer.MAX_VALUE);
        saltPerChunk = builder.comment("Chance that salt generates in a chunk. (0 to Disable)").defineInRange("saltPerChunk", 2, 0, Integer.MAX_VALUE);
        saltMaxVeinSize = builder.comment("Max number of blocks in a salt vein.").defineInRange("saltMaxVeinSize", 6, 1, Integer.MAX_VALUE);
        userWorldGenVersion = builder.comment("Change this value to cause Mekanism to regen its ore in all loaded chunks.").define("userWorldGenVersion", 0);
        builder.pop();

        builder.comment("Energy Conversion Rate Settings").push(CONVERSION_CATEGORY);
        blacklistIC2 = builder.comment("Disables IC2 power integration. Requires world restart (server-side option in SMP).").worldRestart().define("blacklistIC2", false);
        FROM_IC2 = builder.comment("Conversion multiplier from EU to Joules (EU * JoulesToEU = Joules)").define("JoulesToEU", 10D);
        TO_IC2 = builder.comment("Conversion multiplier from Joules to EU (Joules * EUToJoules = EU)").define("EUToJoules", 0.1D);
        blacklistForge = builder.comment("Disables Forge Energy (FE,RF,IF,uF,CF) power integration. Requires world restart (server-side option in SMP).").worldRestart()
              .define("blacklistForge", false);
        FROM_FORGE = builder.comment("Conversion multiplier from Forge Energy to Joules (FE * JoulesToForge = Joules)").define("JoulesToForge", 2.5D);
        TO_FORGE = builder.comment("Conversion multiplier from Joules to Forge Energy (Joules * ForgeToJoules = FE)").define("ForgeToJoules", 0.4D);
        FROM_H2 = builder.comment("How much energy is produced per mB of Hydrogen, also affects Electrolytic Separator usage, Ethylene burn rate and Gas generator energy capacity.")
              .define("HydrogenEnergyDensity", 200D);
        ETHENE_BURN_TIME = builder.comment("Burn time for Ethylene (1mB hydrogen + 2*bioFuel/tick*200ticks/100mB * 20x efficiency bonus).").define("EthyleneBurnTime", 40);
        ENERGY_PER_REDSTONE = builder.comment("How much energy (Joules) a piece of redstone gives in machines.").define("EnergyPerRedstone", 10_000D);
        builder.pop();

        builder.comment("Atomic Disassembler Settings").push(DISASSEMBLER_CATEGORY);
        disassemblerEnergyUsage = builder.comment("Base Energy (Joules) usage of the Atomic Disassembler. (Gets multiplied by speed factor)").define("energyUsage", 10);
        disassemblerEnergyUsageHoe = builder.comment("Cost in Joules of using the Atomic Disassembler as a hoe.").define("energyUsageHoe", 10);
        disassemblerEnergyUsageWeapon = builder.comment("Cost in Joules of using the Atomic Disassembler as a weapon.").define("energyUsageWeapon", 2_000);
        disassemblerMiningRange = builder.comment("The Range of the Atomic Disassembler Extended Vein Mining.").define("miningRange", 10);
        disassemblerMiningCount = builder.comment("The max Atomic Disassembler Vein Mining Block Count.").define("miningCount", 128);
        disassemblerSlowMode = builder.comment("Enable the 'Slow' mode for the Atomic Disassembler.").define("slowMode", true);
        disassemblerFastMode = builder.comment("Enable the 'Fast' mode for the Atomic Disassembler.").define("fastMode", true);
        disassemblerVeinMining = builder.comment("Enable the 'Vein Mining' mode for the Atomic Disassembler.").define("veinMining", true);
        disassemblerExtendedMining = builder.comment("Enable the 'Extended Vein Mining' mode for the Atomic Disassembler. (Allows vein mining everything not just ores/logs)")
              .define("extendedMining", true);
        disassemblerDamageMin = builder.comment("The amount of damage the Atomic Disassembler does when it is out of power. (Value is in number of half hearts)")
              .define("damageMin", 4);
        disassemblerDamageMax = builder.comment("The amount of damage the Atomic Disassembler does when it has at least DisassemblerEnergyUsageWeapon power stored. (Value is in number of half hearts)")
              .define("damageMax", 20);
        disassemblerBatteryCapacity = builder.comment("Maximum amount (joules) of energy the Atomic Disassembler can contain").worldRestart()
              .defineInRange("batteryCapacity", 1_000_000D, 0, Double.MAX_VALUE);
        builder.pop();

        //If this is less than 1, upgrades make machines worse. If less than 0, I don't even know.
        maxUpgradeMultiplier = builder.comment("Base factor for working out machine performance with upgrades - UpgradeModifier * (UpgradesInstalled/UpgradesPossible).")
              .defineInRange("maxUpgradeMultiplier", 10, 1, Integer.MAX_VALUE);
        minerSilkMultiplier = builder.comment("Energy multiplier for using silk touch mode with the Digital Miner.").define("minerSilkMultiplier", 6);
        prefilledGasTanks = builder.comment("Add filled creative gas tanks to creative/JEI.").define("prefilledGasTanks", true);
        armoredJetpackArmor = builder.comment("Armor value of the Armored Jetpack.").define("armoredJetpackArmor", 12);
        armoredJetpackToughness = builder.comment("Toughness value of the Armored Jetpack.").define("armoredJetpackToughness", 2);
        aestheticWorldDamage = builder.comment("If enabled, lasers can break blocks and the flamethrower starts fires.").define("aestheticWorldDamage", true);
        opsBypassRestrictions = builder.comment("Ops can bypass the block security restrictions if enabled.").define("opsBypassRestrictions", false);
        maxJetpackGas = builder.comment("Jetpack Gas Tank capacity in mB.").define("maxJetpackGas", 24_000);
        maxScubaGas = builder.comment("Scuba Tank Gas Tank capacity in mB.").define("maxScubaGas", 24_000);
        maxFlamethrowerGas = builder.comment("Flamethrower Gas Tank capacity in mB.").define("maxFlamethrowerGas", 24_000);
        maxPumpRange = builder.comment("Maximum block distance to pull fluid from for the Electric Pump.").define("maxPumpRange", 80);
        pumpWaterSources = builder.comment("If enabled makes Water and Heavy Water blocks be removed from the world on pump.").define("pumpWaterSources", false);
        maxPlenisherNodes = builder.comment("Fluidic Plenisher stops after this many blocks.").define("maxPlenisherNodes", 4_000);

        builder.comment("Thermal Evaporation Plant Settings").push(EVAPORATION_CATEGORY);
        evaporationHeatDissipation = FloatValue.of(builder.comment("Thermal Evaporation Tower heat loss per tick.").define("heatDissipation", 0.02D));
        evaporationTempMultiplier = builder.comment("Temperature to amount produced ratio for Thermal Evaporation Tower.").define("tempMultiplier", 0.1D);
        evaporationSolarMultiplier = builder.comment("Heat to absorb per Solar Panel array of Thermal Evaporation Tower.").define("solarMultiplier", 0.2D);
        evaporationMaxTemp = builder.comment("Max Temperature of the Thermal Evaporation Tower.").define("maxTemp", 3_000D);
        builder.pop();

        energyPerHeat = builder.comment("Joules required by the Resistive Heater to produce one unit of heat. Also affects Thermoelectric Boiler's Water->Steam rate.")
              .define("energyPerHeat", 1_000D);
        maxEnergyPerSteam = builder.comment("Maximum Joules per mB of Steam. Also affects Thermoelectric Boiler.").define("maxEnergyPerSteam", 100D);
        superheatingHeatTransfer = builder.comment("Amount of heat each Boiler heating element produces.").define("superheatingHeatTransfer", 10_000D);
        heatPerFuelTick = builder.comment("Amount of heat produced per fuel tick of a fuel's burn time in the Fuelwood Heater.").define("heatPerFuelTick", 4D);
        allowTransmitterAlloyUpgrade = builder.comment("Allow right clicking on Cables/Pipes/Tubes with alloys to upgrade the tier.")
              .define("allowTransmitterAlloyUpgrade", true);
        allowChunkloading = builder.comment("Disable to make the anchor upgrade not do anything.").define("allowChunkloading", true);
        allowProtection = builder.comment("Enable the security system for players to prevent others from accessing their machines. Does NOT affect Frequencies.")
              .define("allowProtection", true);
        portableTeleporterDelay = builder.comment("Delay in ticks before a player is teleported after clicking the Teleport button in the portable teleporter.")
              .define("portableTeleporterDelay", 0);

        builder.comment("Quantum Entangloporter Settings").push(ENTANGLOPORTER_CATEGORY);
        quantumEntangloporterEnergyTransfer = builder.comment("Maximum energy buffer (Mekanism Joules) of an Entangoloporter frequency - i.e. the maximum transfer per tick per frequency.")
              .worldRestart().defineInRange("energyBuffer", 16_000_000D, 0, Double.MAX_VALUE);
        quantumEntangloporterFluidBuffer = builder.comment("Maximum fluid buffer (mb) of an Entangoloporter frequency - i.e. the maximum transfer per tick per frequency. Default is ultimate tier tank capacity.")
              .worldRestart().defineInRange("fluidBuffer", GasTankTier.ULTIMATE.getBaseStorage(), 0, Integer.MAX_VALUE);
        quantumEntangloporterGasBuffer = builder.comment("Maximum fluid buffer (mb) of an Entangoloporter frequency - i.e. the maximum transfer per tick per frequency. Default is ultimate tier tank capacity.")
              .worldRestart().defineInRange("gasBuffer", GasTankTier.ULTIMATE.getBaseStorage(), 0, Integer.MAX_VALUE);
        builder.pop();

        laserRange = builder.comment("How far (in blocks) a laser can travel.").define("laserRange", 64);
        laserEnergyNeededPerHardness = builder.comment("Energy needed to destroy or attract blocks with a Laser (per block hardness level).")
              .define("laserEnergyNeededPerHardness", 100_000);
        digitalMinerMaxRadius = builder.comment("Maximum radius in blocks that the Digital Miner can reach. (Increasing this may have negative effects on stability "
                                                + "and/or performance. We strongly recommend you leave it at the default value.)")
              .defineInRange("digitalMinerMaxRadius", 32, 1, Integer.MAX_VALUE);
        sawdustChanceLog = builder.comment("Chance of producing sawdust per operation in the precision sawmill when turning logs into planks.").worldRestart()
              .defineInRange("sawdustChanceLog", 1D, 0, 1);
        energyUnit = builder.comment("Displayed energy type in Mekanism GUIs.").defineEnum("energyType", EnergyType.FE);
        tempUnit = builder.comment("Displayed temperature unit in Mekanism GUIs.").defineEnum("temperatureUnit", TempType.K);

        voidInvalidGases = builder.comment("Should machines void the gas inside of them on load if there is no recipe using that gas.").define("voidInvalidGases", true);

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