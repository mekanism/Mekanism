package mekanism.common.config;

import mekanism.api.math.FloatingLong;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class GearConfig extends BaseMekanismConfig {

    private static final String DISASSEMBLER_CATEGORY = "atomic_disassembler";
    private static final String CONFIGURATOR_CATEGORY = "configurator";
    private static final String ELECTRIC_BOW_CATEGORY = "electric_bow";
    private static final String ENERGY_TABLET_CATEGORY = "energy_tablet";
    private static final String FLAMETHROWER_CATEGORY = "flamethrower";
    private static final String FREE_RUNNER_CATEGORY = "free_runner";
    private static final String JETPACK_CATEGORY = "jetpack";
    private static final String ARMORED_JETPACK_SUBCATEGORY = "armored";
    private static final String NETWORK_READER_CATEGORY = "network_reader";
    private static final String PORTABLE_TELEPORTER_CATEGORY = "portable_teleporter";
    private static final String SCUBA_TANK_CATEGORY = "scuba_tank";
    private static final String SEISMIC_READER_CATEGORY = "seismic_reader";
    private static final String CANTEEN_CATEGORY = "canteen";
    private static final String MEKATOOL_CATEGORY = "mekatool";
    private static final String MEKASUIT_CATEGORY = "mekasuit";

    private final ForgeConfigSpec configSpec;

    //Atomic Disassembler
    public final CachedFloatingLongValue disassemblerEnergyUsage;
    public final CachedFloatingLongValue disassemblerEnergyUsageWeapon;
    public final CachedIntValue disassemblerMiningRange;
    public final CachedIntValue disassemblerMiningCount;
    public final CachedBooleanValue disassemblerSlowMode;
    public final CachedBooleanValue disassemblerFastMode;
    public final CachedBooleanValue disassemblerVeinMining;
    public final CachedBooleanValue disassemblerExtendedMining;
    public final CachedIntValue disassemblerMinDamage;
    public final CachedIntValue disassemblerMaxDamage;
    public final CachedFloatingLongValue disassemblerMaxEnergy;
    public final CachedFloatingLongValue disassemblerChargeRate;
    //Configurator
    public final CachedFloatingLongValue configuratorMaxEnergy;
    public final CachedFloatingLongValue configuratorChargeRate;
    public final CachedFloatingLongValue configuratorEnergyPerConfigure;
    public final CachedFloatingLongValue configuratorEnergyPerItem;
    //Electric Bow
    public final CachedFloatingLongValue electricBowMaxEnergy;
    public final CachedFloatingLongValue electricBowChargeRate;
    public final CachedFloatingLongValue electricBowEnergyUsage;
    public final CachedFloatingLongValue electricBowEnergyUsageFire;
    //Energy Tablet
    public final CachedFloatingLongValue tabletMaxEnergy;
    public final CachedFloatingLongValue tabletChargeRate;
    //Flamethrower
    public final CachedLongValue flamethrowerMaxGas;
    public final CachedLongValue flamethrowerFillRate;
    //Free runner
    public final CachedFloatingLongValue freeRunnerFallEnergyCost;
    public final CachedFloatingLongValue freeRunnerMaxEnergy;
    public final CachedFloatingLongValue freeRunnerChargeRate;
    //Jetpack
    public final CachedLongValue jetpackMaxGas;
    public final CachedLongValue jetpackFillRate;
    //Armored Jetpack
    public final CachedIntValue armoredJetpackArmor;
    public final CachedFloatValue armoredJetpackToughness;
    //Portable Teleporter
    public final CachedFloatingLongValue portableTeleporterMaxEnergy;
    public final CachedFloatingLongValue portableTeleporterChargeRate;
    public final CachedIntValue portableTeleporterDelay;
    //Network Reader
    public final CachedFloatingLongValue networkReaderMaxEnergy;
    public final CachedFloatingLongValue networkReaderChargeRate;
    public final CachedFloatingLongValue networkReaderEnergyUsage;
    //Scuba Tank
    public final CachedLongValue scubaMaxGas;
    public final CachedLongValue scubaFillRate;
    //Seismic Reader
    public final CachedFloatingLongValue seismicReaderMaxEnergy;
    public final CachedFloatingLongValue seismicReaderChargeRate;
    public final CachedFloatingLongValue seismicReaderEnergyUsage;
    //Canteen
    public final CachedLongValue canteenMaxStorage;
    public final CachedLongValue canteenTransferRate;
    //Meka-Tool
    public final CachedFloatingLongValue mekaToolEnergyUsageWeapon;
    public final CachedFloatingLongValue mekaToolEnergyUsageTeleport;
    public final CachedFloatingLongValue mekaToolEnergyUsage;
    public final CachedFloatingLongValue mekaToolEnergyUsageSilk;
    public final CachedIntValue mekaToolMaxTeleportReach;
    public final CachedIntValue mekaToolBaseDamage;
    public final CachedFloatValue mekaToolBaseEfficiency;
    public final CachedFloatingLongValue mekaToolBaseEnergyCapacity;
    public final CachedFloatingLongValue mekaToolBaseChargeRate;
    public final CachedFloatingLongValue mekaToolEnergyUsageHoe;
    public final CachedFloatingLongValue mekaToolEnergyUsageShovel;
    public final CachedFloatingLongValue mekaToolEnergyUsageAxe;
    public final CachedBooleanValue mekaToolExtendedMining;
    //MekaSuit
    public final CachedFloatingLongValue mekaSuitBaseEnergyCapacity;
    public final CachedFloatingLongValue mekaSuitBaseChargeRate;
    public final CachedFloatingLongValue mekaSuitBaseJumpEnergyUsage;
    public final CachedFloatingLongValue mekaSuitEnergyUsagePotionTick;
    public final CachedFloatingLongValue mekaSuitEnergyUsageMagicReduce;
    public final CachedFloatingLongValue mekaSuitEnergyUsageSprintBoost;
    public final CachedFloatingLongValue mekaSuitEnergyUsageGravitationalModulation;
    public final CachedFloatingLongValue mekaSuitInventoryChargeRate;
    public final CachedFloatingLongValue mekaSuitSolarRechargingRate;
    public final CachedFloatingLongValue mekaSuitEnergyUsageVisionEnhancement;
    public final CachedFloatingLongValue mekaSuitEnergyUsageNutritionalInjection;
    public final CachedFloatingLongValue mekaSuitEnergyUsageDamage;
    public final CachedFloatingLongValue mekaSuitEnergyUsageItemAttraction;
    public final CachedLongValue mekaSuitNutritionalMaxStorage;
    public final CachedLongValue mekaSuitNutritionalTransferRate;
    public final CachedLongValue mekaSuitJetpackMaxStorage;
    public final CachedLongValue mekaSuitJetpackTransferRate;

    GearConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Gear Config. This config is synced from server to client.").push("gear");

        builder.comment("Atomic Disassembler Settings").push(DISASSEMBLER_CATEGORY);
        disassemblerEnergyUsage = CachedFloatingLongValue.define(this, builder, "Base Energy (Joules) usage of the Atomic Disassembler. (Gets multiplied by speed factor)",
              "energyUsage", FloatingLong.createConst(10));
        disassemblerEnergyUsageWeapon = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Atomic Disassembler as a weapon.",
              "energyUsageWeapon", FloatingLong.createConst(2_000));
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
        disassemblerMinDamage = CachedIntValue.wrap(this, builder.comment("The amount of damage the Atomic Disassembler does when it is out of power. (Value is in number of half hearts)")
              .define("minDamage", 4));
        disassemblerMaxDamage = CachedIntValue.wrap(this, builder.comment("The amount of damage the Atomic Disassembler does when it has at least DisassemblerEnergyUsageWeapon power stored. (Value is in number of half hearts)")
              .define("maxDamage", 20));
        disassemblerMaxEnergy = CachedFloatingLongValue.define(this, builder, "Maximum amount (joules) of energy the Atomic Disassembler can contain.",
              "maxEnergy", FloatingLong.createConst(1_000_000));
        disassemblerChargeRate = CachedFloatingLongValue.define(this, builder, "Amount (joules) of energy the Atomic Disassembler can accept per tick.",
              "chargeRate", FloatingLong.createConst(5_000));
        builder.pop();

        builder.comment("Configurator Settings").push(CONFIGURATOR_CATEGORY);
        configuratorMaxEnergy = CachedFloatingLongValue.define(this, builder, "Maximum amount (joules) of energy the Configurator can contain.",
              "maxEnergy", FloatingLong.createConst(60_000));
        configuratorChargeRate = CachedFloatingLongValue.define(this, builder, "Amount (joules) of energy the Configurator can accept per tick.",
              "chargeRate", FloatingLong.createConst(300));
        configuratorEnergyPerConfigure = CachedFloatingLongValue.define(this, builder, "Energy usage in joules of using the configurator to configure machines.",
              "energyPerConfigure", FloatingLong.createConst(400));
        configuratorEnergyPerItem = CachedFloatingLongValue.define(this, builder, "Energy cost in joules for each item the configurator ejects from a machine on empty mode.",
              "energyPerItem", FloatingLong.createConst(8));
        builder.pop();

        builder.comment("Electric Bow Settings").push(ELECTRIC_BOW_CATEGORY);
        electricBowMaxEnergy = CachedFloatingLongValue.define(this, builder, "Maximum amount (joules) of energy the Electric Bow can contain.",
              "maxEnergy", FloatingLong.createConst(120_000));
        electricBowChargeRate = CachedFloatingLongValue.define(this, builder, "Amount (joules) of energy the Electric Bow can accept per tick.",
              "chargeRate", FloatingLong.createConst(600));
        electricBowEnergyUsage = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Electric Bow.",
              "energyUsage", FloatingLong.createConst(120));
        electricBowEnergyUsageFire = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Electric Bow with flame mode active.",
              "energyUsage", FloatingLong.createConst(1_200));
        builder.pop();

        builder.comment("Energy Tablet Settings").push(ENERGY_TABLET_CATEGORY);
        tabletMaxEnergy = CachedFloatingLongValue.define(this, builder, "Maximum amount (joules) of energy the Energy Tablet can contain.",
              "maxEnergy", FloatingLong.createConst(1_000_000));
        tabletChargeRate = CachedFloatingLongValue.define(this, builder, "Amount (joules) of energy the Energy Tablet can accept per tick.",
              "chargeRate", FloatingLong.createConst(5_000));
        builder.pop();

        builder.comment("Flamethrower Settings").push(FLAMETHROWER_CATEGORY);
        flamethrowerMaxGas = CachedLongValue.wrap(this, builder.comment("Flamethrower Gas Tank capacity in mB.")
              .defineInRange("maxGas", 24_000, 1, Long.MAX_VALUE));
        flamethrowerFillRate = CachedLongValue.wrap(this, builder.comment("Amount of hydrogen the Flamethrower can accept per tick.")
              .defineInRange("fillRate", 16, 1, Long.MAX_VALUE));
        builder.pop();

        builder.comment("Free Runner Settings").push(FREE_RUNNER_CATEGORY);
        freeRunnerFallEnergyCost = CachedFloatingLongValue.define(this, builder, "Energy cost/multiplier in Joules for reducing fall damage with free runners. Energy cost is: FallDamage * freeRunnerFallEnergyCost. (1 FallDamage is 1 half heart)",
              "fallEnergyCost", FloatingLong.createConst(50));
        freeRunnerMaxEnergy = CachedFloatingLongValue.define(this, builder, "Maximum amount (joules) of energy Free Runners can contain.",
              "maxEnergy", FloatingLong.createConst(64_000));
        freeRunnerChargeRate = CachedFloatingLongValue.define(this, builder, "Amount (joules) of energy the Free Runners can accept per tick.",
              "chargeRate", FloatingLong.createConst(320));
        builder.pop();

        builder.comment("Jetpack Settings").push(JETPACK_CATEGORY);
        jetpackMaxGas = CachedLongValue.wrap(this, builder.comment("Jetpack Gas Tank capacity in mB.")
              .defineInRange("maxGas", 24_000, 1, Long.MAX_VALUE));
        jetpackFillRate = CachedLongValue.wrap(this, builder.comment("Amount of hydrogen the Jetpack can accept per tick.")
              .defineInRange("fillRate", 16, 1, Long.MAX_VALUE));
        builder.comment("Armored Jetpack Settings").push(ARMORED_JETPACK_SUBCATEGORY);
        armoredJetpackArmor = CachedIntValue.wrap(this, builder.comment("Armor value of the Armored Jetpack.")
              .define("armor", 12));
        armoredJetpackToughness = CachedFloatValue.wrap(this, builder.comment("Toughness value of the Armored Jetpack.")
              .define("toughness", 2.0));
        builder.pop(2);

        builder.comment("Network Reader Settings").push(NETWORK_READER_CATEGORY);
        networkReaderMaxEnergy = CachedFloatingLongValue.define(this, builder, "Maximum amount (joules) of energy the Network Reader can contain.",
              "maxEnergy", FloatingLong.createConst(60_000));
        networkReaderChargeRate = CachedFloatingLongValue.define(this, builder, "Amount (joules) of energy the Network Reader can accept per tick.",
              "chargeRate", FloatingLong.createConst(300));
        networkReaderEnergyUsage = CachedFloatingLongValue.define(this, builder, "Energy usage in joules for each network reading.",
              "energyUsage", FloatingLong.createConst(400));
        builder.pop();

        builder.comment("Portable Teleporter Settings").push(PORTABLE_TELEPORTER_CATEGORY);
        portableTeleporterMaxEnergy = CachedFloatingLongValue.define(this, builder, "Maximum amount (joules) of energy the Portable Teleporter can contain.",
              "maxEnergy", FloatingLong.createConst(1_000_000));
        portableTeleporterChargeRate = CachedFloatingLongValue.define(this, builder, "Amount (joules) of energy the Portable Teleporter can accept per tick.",
              "chargeRate", FloatingLong.createConst(5_000));
        portableTeleporterDelay = CachedIntValue.wrap(this, builder.comment("Delay in ticks before a player is teleported after clicking the Teleport button in the portable teleporter.")
              .define("delay", 0));
        builder.pop();

        builder.comment("Scuba Tank Settings").push(SCUBA_TANK_CATEGORY);
        scubaMaxGas = CachedLongValue.wrap(this, builder.comment("Scuba Tank Gas Tank capacity in mB.")
              .defineInRange("maxGas", 24_000, 1, Long.MAX_VALUE));
        scubaFillRate = CachedLongValue.wrap(this, builder.comment("Amount of oxygen the Scuba Tank Gas Tank can accept per tick.")
              .defineInRange("fillRate", 16, 1, Long.MAX_VALUE));
        builder.pop();

        builder.comment("Seismic Reader Settings").push(SEISMIC_READER_CATEGORY);
        seismicReaderMaxEnergy = CachedFloatingLongValue.define(this, builder, "Maximum amount (joules) of energy the Seismic Reader can contain.",
              "maxEnergy", FloatingLong.createConst(12_000));
        seismicReaderChargeRate = CachedFloatingLongValue.define(this, builder, "Amount (joules) of energy the Seismic Reader can accept per tick.",
              "chargeRate", FloatingLong.createConst(60));
        seismicReaderEnergyUsage = CachedFloatingLongValue.define(this, builder, "Energy usage in joules required to use the Seismic Reader.",
              "energyUsage", FloatingLong.createConst(250));
        builder.pop();

        builder.comment("Canteen Settings").push(CANTEEN_CATEGORY);
        canteenMaxStorage = CachedLongValue.wrap(this, builder.comment("Maximum amount of Nutritional Paste storable by the Canteen.")
              .defineInRange("maxStorage", 64_000, 1, Long.MAX_VALUE));
        canteenTransferRate = CachedLongValue.wrap(this, builder.comment("Rate at which Nutritional Paste can be transferred into a Canteen.")
              .defineInRange("transferRate", 128, 1, Long.MAX_VALUE));
        builder.pop();

        builder.comment("Meka-Tool Settings").push(MEKATOOL_CATEGORY);
        mekaToolEnergyUsage = CachedFloatingLongValue.define(this, builder, "Base energy (Joules) usage of the Meka-Tool. (Gets multiplied by speed factor)",
              "energyUsageTeleport", FloatingLong.createConst(10));
        mekaToolEnergyUsageSilk = CachedFloatingLongValue.define(this, builder, "Silk touch energy (Joules) usage of the Meka-Tool. (Gets multiplied by speed factor)",
              "energyUsageTeleport", FloatingLong.createConst(100));
        mekaToolEnergyUsageWeapon = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tool to deal 4 units of damage.",
              "energyUsageWeapon", FloatingLong.createConst(2_000));
        mekaToolEnergyUsageTeleport = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tool to teleport 10 blocks.",
              "energyUsageTeleport", FloatingLong.createConst(1_000));
        mekaToolMaxTeleportReach = CachedIntValue.wrap(this, builder.comment("Maximum distance a player can teleport with the Meka-Tool.")
              .define("maxTeleportReach", 100));
        mekaToolBaseDamage = CachedIntValue.wrap(this, builder.comment("Damage applied by Meka-Tool without using any energy.")
              .define("baseDamage", 4));
        mekaToolBaseEfficiency = CachedFloatValue.wrap(this, builder.comment("Efficiency of the Meka-Tool with energy but without any upgrades.")
              .define("baseEfficiency", 4D));
        mekaToolBaseEnergyCapacity = CachedFloatingLongValue.define(this, builder, "Energy capacity (Joules) of the Meka-Tool without any installed upgrades. Quadratically scaled by upgrades.",
              "baseEnergyCapacity", FloatingLong.createConst(16_000_000));
        mekaToolBaseChargeRate = CachedFloatingLongValue.define(this, builder, "Amount (joules) of energy the Meka-Tool can accept per tick. Quadratically scaled by upgrades.",
              "chargeRate", FloatingLong.createConst(100_000));
        mekaToolEnergyUsageHoe = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tool as a hoe.",
              "energyUsageHoe", FloatingLong.createConst(10));
        mekaToolEnergyUsageShovel = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tool as a shovel for making paths.",
              "energyUsageShovel", FloatingLong.createConst(10));
        mekaToolEnergyUsageAxe = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tool as an axe for stripping logs.",
              "energyUsageAxe", FloatingLong.createConst(10));
        mekaToolExtendedMining = CachedBooleanValue.wrap(this, builder.comment("Enable the 'Extended Vein Mining' mode for the Meka-Tool. (Allows vein mining everything not just ores/logs)")
              .define("extendedMining", true));
        builder.pop();

        builder.comment("MekaSuit Settings").push(MEKASUIT_CATEGORY);
        mekaSuitBaseEnergyCapacity = CachedFloatingLongValue.define(this, builder, "Energy capacity (Joules) of MekaSuit items without any installed upgrades. Quadratically scaled by upgrades.",
              "baseEnergyCapacity", FloatingLong.createConst(16_000_000));
        mekaSuitBaseChargeRate = CachedFloatingLongValue.define(this, builder, "Amount (joules) of energy the MekaSuit can accept per tick. Quadratically scaled by upgrades.",
              "chargeRate", FloatingLong.createConst(100_000));
        mekaSuitBaseJumpEnergyUsage = CachedFloatingLongValue.define(this, builder, "Energy usage (Joules) of MekaSuit when adding 0.1 to jump motion.",
              "baseJumpEnergyUsage", FloatingLong.createConst(1_000));
        mekaSuitEnergyUsagePotionTick = CachedFloatingLongValue.define(this, builder, "Energy usage (Joules) of MekaSuit when lessening a potion effect.",
              "energyUsagePotionTick", FloatingLong.createConst(40_000));
        mekaSuitEnergyUsageMagicReduce = CachedFloatingLongValue.define(this, builder, "Energy cost/multiplier in Joules for reducing magic damage via the inhalation purification unit. Energy cost is: MagicDamage * energyUsageMagicPrevent. (1 MagicDamage is 1 half heart).",
              "energyUsageMagicReduce", FloatingLong.createConst(1_000));
        mekaSuitEnergyUsageSprintBoost = CachedFloatingLongValue.define(this, builder, "Energy usage (Joules) of MekaSuit when adding 0.1 to sprint motion.",
              "energyUsageSprintBoost", FloatingLong.createConst(100));
        mekaSuitEnergyUsageGravitationalModulation = CachedFloatingLongValue.define(this, builder, "Energy usage (Joules) of MekaSuit per tick when flying via Gravitational Modulation.",
              "energyUsageGravitationalModulation", FloatingLong.createConst(1_000));
        mekaSuitInventoryChargeRate = CachedFloatingLongValue.define(this, builder, "Charge rate of inventory items (Joules) per tick.",
              "inventoryChargeRate", FloatingLong.createConst(10_000));
        mekaSuitSolarRechargingRate = CachedFloatingLongValue.define(this, builder, "Solar recharging rate (Joules) of helmet per tick, per upgrade installed.",
              "solarRechargingRate", FloatingLong.createConst(500));
        mekaSuitEnergyUsageVisionEnhancement = CachedFloatingLongValue.define(this, builder, "Energy usage (Joules) of MekaSuit per tick of using vision enhancement.",
              "energyUsageVisionEnhancement", FloatingLong.createConst(500));
        mekaSuitEnergyUsageNutritionalInjection = CachedFloatingLongValue.define(this, builder, "Energy usage (Joules) of MekaSuit per half-food of nutritional injection.",
              "energyUsageNutritionalInjection", FloatingLong.createConst(20_000));
        mekaSuitEnergyUsageDamage = CachedFloatingLongValue.define(this, builder, "Energy usage (Joules) of MekaSuit per unit of damage applied.",
              "energyUsageDamage", FloatingLong.createConst(100_000));
        mekaSuitEnergyUsageItemAttraction = CachedFloatingLongValue.define(this, builder, "Energy usage (Joules) of MekaSuit per tick of attracting a single item.",
              "energyUsageItemAttraction", FloatingLong.createConst(250));
        mekaSuitNutritionalMaxStorage = CachedLongValue.wrap(this, builder.comment("Maximum amount of Nutritional Paste storable by the nutritional injection unit.")
              .defineInRange("nutritionalMaxStorage", 128_000, 1, Long.MAX_VALUE));
        mekaSuitNutritionalTransferRate = CachedLongValue.wrap(this, builder.comment("Rate at which Nutritional Paste can be transferred into the nutritional injection unit.")
              .defineInRange("nutritionalTransferRate", 256, 1, Long.MAX_VALUE));
        mekaSuitJetpackMaxStorage = CachedLongValue.wrap(this, builder.comment("Maximum amount of Hydrogen storable in the jetpack unit.")
              .defineInRange("jetpackMaxStorage", 48_000, 1, Long.MAX_VALUE));
        mekaSuitJetpackTransferRate = CachedLongValue.wrap(this, builder.comment("Rate at which Hydrogen can be transferred into the jetpack unit.")
              .defineInRange("jetpackTransferRate", 256, 1, Long.MAX_VALUE));
        builder.pop();

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "gear";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }

    @Override
    public boolean addToContainer() {
        return false;
    }
}