package mekanism.common.config;

import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class GearConfig extends BaseMekanismConfig {

    private static final String DISASSEMBLER_CATEGORY = "atomic_disassembler";
    private static final String CONFIGURATOR_CATEGORY = "configurator";
    private static final String ELECTRIC_BOW_CATEGORY = "electric_bow";
    private static final String ENERGY_TABLET_CATEGORY = "energy_tablet";
    private static final String FLAMETHROWER_CATEGORY = "flamethrower";
    private static final String FREE_RUNNER_CATEGORY = "free_runner";
    private static final String ARMORED_SUBCATEGORY = "armored";
    private static final String JETPACK_CATEGORY = "jetpack";
    private static final String NETWORK_READER_CATEGORY = "network_reader";
    private static final String PORTABLE_TELEPORTER_CATEGORY = "portable_teleporter";
    private static final String SCUBA_TANK_CATEGORY = "scuba_tank";
    private static final String SEISMIC_READER_CATEGORY = "seismic_reader";
    private static final String CANTEEN_CATEGORY = "canteen";
    private static final String MEKATOOL_CATEGORY = "mekatool";
    private static final String MEKASUIT_CATEGORY = "mekasuit";
    private static final String MEKASUIT_DAMAGE_CATEGORY = "damage_absorption";

    private final ForgeConfigSpec configSpec;

    //Atomic Disassembler
    public final CachedFloatingLongValue disassemblerEnergyUsage;
    public final CachedFloatingLongValue disassemblerEnergyUsageWeapon;
    public final CachedIntValue disassemblerMiningCount;
    public final CachedBooleanValue disassemblerSlowMode;
    public final CachedBooleanValue disassemblerFastMode;
    public final CachedBooleanValue disassemblerVeinMining;
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
    public final CachedBooleanValue flamethrowerDestroyItems;
    //Free runner
    public final CachedFloatingLongValue freeRunnerFallEnergyCost;
    public final CachedFloatValue freeRunnerFallDamageRatio;
    public final CachedFloatingLongValue freeRunnerMaxEnergy;
    public final CachedFloatingLongValue freeRunnerChargeRate;
    //Armored Free Runner
    public final CachedIntValue armoredFreeRunnerArmor;
    public final CachedFloatValue armoredFreeRunnerToughness;
    public final CachedFloatValue armoredFreeRunnerKnockbackResistance;
    //Jetpack
    public final CachedLongValue jetpackMaxGas;
    public final CachedLongValue jetpackFillRate;
    //Armored Jetpack
    public final CachedIntValue armoredJetpackArmor;
    public final CachedFloatValue armoredJetpackToughness;
    public final CachedFloatValue armoredJetpackKnockbackResistance;
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
    public final CachedIntValue canteenMaxStorage;
    public final CachedIntValue canteenTransferRate;
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
    public final CachedFloatingLongValue mekaToolEnergyUsageShearEntity;
    public final CachedBooleanValue mekaToolExtendedMining;
    //MekaSuit
    public final CachedFloatingLongValue mekaSuitBaseEnergyCapacity;
    public final CachedFloatingLongValue mekaSuitBaseChargeRate;
    public final CachedFloatingLongValue mekaSuitBaseJumpEnergyUsage;
    public final CachedFloatingLongValue mekaSuitElytraEnergyUsage;
    public final CachedFloatingLongValue mekaSuitEnergyUsagePotionTick;
    public final CachedFloatingLongValue mekaSuitEnergyUsageMagicReduce;
    public final CachedFloatingLongValue mekaSuitEnergyUsageFall;
    public final CachedFloatingLongValue mekaSuitEnergyUsageSprintBoost;
    public final CachedFloatingLongValue mekaSuitEnergyUsageGravitationalModulation;
    public final CachedFloatingLongValue mekaSuitInventoryChargeRate;
    public final CachedFloatingLongValue mekaSuitSolarRechargingRate;
    public final CachedFloatingLongValue mekaSuitEnergyUsageVisionEnhancement;
    public final CachedFloatingLongValue mekaSuitEnergyUsageHydrostaticRepulsion;
    public final CachedFloatingLongValue mekaSuitEnergyUsageNutritionalInjection;
    public final CachedFloatingLongValue mekaSuitEnergyUsageDamage;
    public final CachedFloatingLongValue mekaSuitEnergyUsageItemAttraction;
    public final CachedIntValue mekaSuitNutritionalMaxStorage;
    public final CachedIntValue mekaSuitNutritionalTransferRate;
    public final CachedLongValue mekaSuitJetpackMaxStorage;
    public final CachedLongValue mekaSuitJetpackTransferRate;
    public final CachedIntValue mekaSuitHelmetArmor;
    public final CachedIntValue mekaSuitBodyArmorArmor;
    public final CachedIntValue mekaSuitPantsArmor;
    public final CachedIntValue mekaSuitBootsArmor;
    public final CachedFloatValue mekaSuitToughness;
    public final CachedFloatValue mekaSuitKnockbackResistance;
    public final Map<DamageSource, CachedFloatValue> mekaSuitDamageRatios = new LinkedHashMap<>();
    public final CachedFloatValue mekaSuitFallDamageRatio;
    public final CachedFloatValue mekaSuitMagicDamageRatio;
    public final CachedFloatValue mekaSuitUnspecifiedDamageRatio;

    GearConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Gear Config. This config is synced from server to client.").push("gear");

        builder.comment("Atomic Disassembler Settings").push(DISASSEMBLER_CATEGORY);
        disassemblerEnergyUsage = CachedFloatingLongValue.define(this, builder, "Base Energy (Joules) usage of the Atomic Disassembler. (Gets multiplied by speed factor)",
              "energyUsage", FloatingLong.createConst(10));
        disassemblerEnergyUsageWeapon = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Atomic Disassembler as a weapon.",
              "energyUsageWeapon", FloatingLong.createConst(2_000));
        disassemblerMiningCount = CachedIntValue.wrap(this, builder.comment("The max Atomic Disassembler Vein Mining Block Count.")
              .define("miningCount", 128));
        disassemblerSlowMode = CachedBooleanValue.wrap(this, builder.comment("Enable the 'Slow' mode for the Atomic Disassembler.")
              .define("slowMode", true));
        disassemblerFastMode = CachedBooleanValue.wrap(this, builder.comment("Enable the 'Fast' mode for the Atomic Disassembler.")
              .define("fastMode", true));
        disassemblerVeinMining = CachedBooleanValue.wrap(this, builder.comment("Enable the 'Vein Mining' mode for the Atomic Disassembler.")
              .define("veinMining", false));
        disassemblerMinDamage = CachedIntValue.wrap(this, builder.comment("The amount of damage the Atomic Disassembler does when it is out of power. (Value is in number of half hearts)")
              .define("minDamage", 4));
        disassemblerMaxDamage = CachedIntValue.wrap(this, builder.comment("The amount of damage the Atomic Disassembler does when it has at least energyUsageWeapon power stored. (Value is in number of half hearts)")
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
              "energyUsageFire", FloatingLong.createConst(1_200));
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
        flamethrowerDestroyItems = CachedBooleanValue.wrap(this, builder.comment("Determines whether or not the Flamethrower can destroy items if it fails to smelt them.")
              .define("destroyItems", true));
        builder.pop();

        builder.comment("Free Runner Settings").push(FREE_RUNNER_CATEGORY);
        freeRunnerFallEnergyCost = CachedFloatingLongValue.define(this, builder, "Energy cost/multiplier in Joules for reducing fall damage with free runners. Energy cost is: FallDamage * freeRunnerFallEnergyCost. (1 FallDamage is 1 half heart)",
              "fallEnergyCost", FloatingLong.createConst(50));
        freeRunnerFallDamageRatio = CachedFloatValue.wrap(this, builder.comment("Percent of damage taken from falling that can be absorbed by Free Runners when they have enough power.")
              .defineInRange("fallDamageReductionRatio", 1D, 0, 1));
        freeRunnerMaxEnergy = CachedFloatingLongValue.define(this, builder, "Maximum amount (joules) of energy Free Runners can contain.",
              "maxEnergy", FloatingLong.createConst(64_000));
        freeRunnerChargeRate = CachedFloatingLongValue.define(this, builder, "Amount (joules) of energy the Free Runners can accept per tick.",
              "chargeRate", FloatingLong.createConst(320));
        builder.comment("Armored Free Runner Settings").push(ARMORED_SUBCATEGORY);
        armoredFreeRunnerArmor = CachedIntValue.wrap(this, builder.comment("Armor value of the Armored Free Runners")
              .defineInRange("armor", 3, 0, Integer.MAX_VALUE));
        armoredFreeRunnerToughness = CachedFloatValue.wrap(this, builder.comment("Toughness value of the Armored Free Runners.")
              .defineInRange("toughness", 2.0, 0, Float.MAX_VALUE));
        armoredFreeRunnerKnockbackResistance = CachedFloatValue.wrap(this, builder.comment("Knockback resistance value of the Armored Free Runners.")
              .defineInRange("knockbackResistance", 0.0, 0, Float.MAX_VALUE));
        builder.pop(2);

        builder.comment("Jetpack Settings").push(JETPACK_CATEGORY);
        jetpackMaxGas = CachedLongValue.wrap(this, builder.comment("Jetpack Gas Tank capacity in mB.")
              .defineInRange("maxGas", 24_000, 1, Long.MAX_VALUE));
        jetpackFillRate = CachedLongValue.wrap(this, builder.comment("Amount of hydrogen the Jetpack can accept per tick.")
              .defineInRange("fillRate", 16, 1, Long.MAX_VALUE));
        builder.comment("Armored Jetpack Settings").push(ARMORED_SUBCATEGORY);
        armoredJetpackArmor = CachedIntValue.wrap(this, builder.comment("Armor value of the Armored Jetpack.")
              .defineInRange("armor", 8, 0, Integer.MAX_VALUE));
        armoredJetpackToughness = CachedFloatValue.wrap(this, builder.comment("Toughness value of the Armored Jetpack.")
              .defineInRange("toughness", 2.0, 0, Float.MAX_VALUE));
        armoredJetpackKnockbackResistance = CachedFloatValue.wrap(this, builder.comment("Knockback resistance value of the Armored Jetpack.")
              .defineInRange("knockbackResistance", 0.0, 0, Float.MAX_VALUE));
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
        canteenMaxStorage = CachedIntValue.wrap(this, builder.comment("Maximum amount of Nutritional Paste storable by the Canteen.")
              .defineInRange("maxStorage", 64_000, 1, Integer.MAX_VALUE));
        canteenTransferRate = CachedIntValue.wrap(this, builder.comment("Rate at which Nutritional Paste can be transferred into a Canteen.")
              .defineInRange("transferRate", 128, 1, Integer.MAX_VALUE));
        builder.pop();

        builder.comment("Meka-Tool Settings").push(MEKATOOL_CATEGORY);
        mekaToolEnergyUsage = CachedFloatingLongValue.define(this, builder, "Base energy (Joules) usage of the Meka-Tool. (Gets multiplied by speed factor)",
              "energyUsage", FloatingLong.createConst(10));
        mekaToolEnergyUsageSilk = CachedFloatingLongValue.define(this, builder, "Silk touch energy (Joules) usage of the Meka-Tool. (Gets multiplied by speed factor)",
              "energyUsageSilk", FloatingLong.createConst(100));
        mekaToolEnergyUsageWeapon = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tool to deal 4 units of damage.",
              "energyUsageWeapon", FloatingLong.createConst(2_000));
        mekaToolEnergyUsageTeleport = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tool to teleport 10 blocks.",
              "energyUsageTeleport", FloatingLong.createConst(1_000));
        mekaToolMaxTeleportReach = CachedIntValue.wrap(this, builder.comment("Maximum distance a player can teleport with the Meka-Tool.")
              .define("maxTeleportReach", 100));
        mekaToolBaseDamage = CachedIntValue.wrap(this, builder.comment("Base damage applied by the Meka-Tool without using any energy.")
              .define("baseDamage", 4));
        mekaToolBaseEfficiency = CachedFloatValue.wrap(this, builder.comment("Efficiency of the Meka-Tool with energy but without any upgrades.")
              .define("baseEfficiency", 4D));
        mekaToolBaseEnergyCapacity = CachedFloatingLongValue.define(this, builder, "Energy capacity (Joules) of the Meka-Tool without any installed upgrades. Quadratically scaled by upgrades.",
              "baseEnergyCapacity", FloatingLong.createConst(16_000_000));
        mekaToolBaseChargeRate = CachedFloatingLongValue.define(this, builder, "Amount (joules) of energy the Meka-Tool can accept per tick. Quadratically scaled by upgrades.",
              "chargeRate", FloatingLong.createConst(100_000));
        mekaToolEnergyUsageHoe = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tool as a hoe.",
              "energyUsageHoe", FloatingLong.createConst(10));
        mekaToolEnergyUsageShovel = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tool as a shovel for making paths and dowsing campfires.",
              "energyUsageShovel", FloatingLong.createConst(10));
        mekaToolEnergyUsageAxe = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tool as an axe for stripping logs, scraping, or removing wax.",
              "energyUsageAxe", FloatingLong.createConst(10));
        mekaToolEnergyUsageShearEntity = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tool to shear entities.",
              "energyUsageShearEntity", FloatingLong.createConst(10));
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
        mekaSuitElytraEnergyUsage = CachedFloatingLongValue.define(this, builder, "Energy usage (Joules) per second of the MekaSuit when flying with the Elytra Unit.",
              "elytraEnergyUsage", FloatingLong.createConst(32_000));
        mekaSuitEnergyUsagePotionTick = CachedFloatingLongValue.define(this, builder, "Energy usage (Joules) of MekaSuit when lessening a potion effect.",
              "energyUsagePotionTick", FloatingLong.createConst(40_000));
        mekaSuitEnergyUsageMagicReduce = CachedFloatingLongValue.define(this, builder, "Energy cost/multiplier in Joules for reducing magic damage via the inhalation purification unit. Energy cost is: MagicDamage * energyUsageMagicPrevent. (1 MagicDamage is 1 half heart).",
              "energyUsageMagicReduce", FloatingLong.createConst(1_000));
        mekaSuitEnergyUsageFall = CachedFloatingLongValue.define(this, builder, "Energy cost/multiplier in Joules for reducing fall damage with MekaSuit Boots. Energy cost is: FallDamage * freeRunnerFallEnergyCost. (1 FallDamage is 1 half heart)",
              "energyUsageFall", FloatingLong.createConst(50));
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
        mekaSuitEnergyUsageHydrostaticRepulsion = CachedFloatingLongValue.define(this, builder, "Energy usage (Joules) of MekaSuit per tick of using hydrostatic repulsion.",
              "energyUsageHydrostaticRepulsion", FloatingLong.createConst(500));
        mekaSuitEnergyUsageNutritionalInjection = CachedFloatingLongValue.define(this, builder, "Energy usage (Joules) of MekaSuit per half-food of nutritional injection.",
              "energyUsageNutritionalInjection", FloatingLong.createConst(20_000));
        mekaSuitEnergyUsageDamage = CachedFloatingLongValue.define(this, builder, "Energy usage (Joules) of MekaSuit per unit of damage applied.",
              "energyUsageDamage", FloatingLong.createConst(100_000));
        mekaSuitEnergyUsageItemAttraction = CachedFloatingLongValue.define(this, builder, "Energy usage (Joules) of MekaSuit per tick of attracting a single item.",
              "energyUsageItemAttraction", FloatingLong.createConst(250));
        mekaSuitNutritionalMaxStorage = CachedIntValue.wrap(this, builder.comment("Maximum amount of Nutritional Paste storable by the nutritional injection unit.")
              .defineInRange("nutritionalMaxStorage", 128_000, 1, Integer.MAX_VALUE));
        mekaSuitNutritionalTransferRate = CachedIntValue.wrap(this, builder.comment("Rate at which Nutritional Paste can be transferred into the nutritional injection unit.")
              .defineInRange("nutritionalTransferRate", 256, 1, Integer.MAX_VALUE));
        mekaSuitJetpackMaxStorage = CachedLongValue.wrap(this, builder.comment("Maximum amount of Hydrogen storable in the jetpack unit.")
              .defineInRange("jetpackMaxStorage", 48_000, 1, Long.MAX_VALUE));
        mekaSuitJetpackTransferRate = CachedLongValue.wrap(this, builder.comment("Rate at which Hydrogen can be transferred into the jetpack unit.")
              .defineInRange("jetpackTransferRate", 256, 1, Long.MAX_VALUE));
        mekaSuitHelmetArmor = CachedIntValue.wrap(this, builder.comment("Armor value of MekaSuit Helmets.")
              .defineInRange("helmetArmor", ArmorMaterials.NETHERITE.getDefenseForSlot(EquipmentSlot.HEAD), 0, Integer.MAX_VALUE));
        mekaSuitBodyArmorArmor = CachedIntValue.wrap(this, builder.comment("Armor value of MekaSuit BodyArmor.")
              .defineInRange("bodyArmorArmor", ArmorMaterials.NETHERITE.getDefenseForSlot(EquipmentSlot.CHEST), 0, Integer.MAX_VALUE));
        mekaSuitPantsArmor = CachedIntValue.wrap(this, builder.comment("Armor value of MekaSuit Pants.")
              .defineInRange("pantsArmor", ArmorMaterials.NETHERITE.getDefenseForSlot(EquipmentSlot.LEGS), 0, Integer.MAX_VALUE));
        mekaSuitBootsArmor = CachedIntValue.wrap(this, builder.comment("Armor value of MekaSuit Boots.")
              .defineInRange("bootsArmor", ArmorMaterials.NETHERITE.getDefenseForSlot(EquipmentSlot.FEET), 0, Integer.MAX_VALUE));
        mekaSuitToughness = CachedFloatValue.wrap(this, builder.comment("Toughness value of the MekaSuit.")
              .defineInRange("toughness", ArmorMaterials.NETHERITE.getToughness(), 0, Float.MAX_VALUE));
        mekaSuitKnockbackResistance = CachedFloatValue.wrap(this, builder.comment("Knockback resistance value of the MekaSuit.")
              .defineInRange("knockbackResistance", ArmorMaterials.NETHERITE.getKnockbackResistance(), 0, Float.MAX_VALUE));
        builder.push(MEKASUIT_DAMAGE_CATEGORY);
        mekaSuitFallDamageRatio = CachedFloatValue.wrap(this, builder.comment("Percent of damage taken from falling that can be absorbed by MekaSuit Boots when they have enough power.")
              .defineInRange("fallDamageReductionRatio", 1D, 0, 1));
        mekaSuitMagicDamageRatio = CachedFloatValue.wrap(this, builder.comment("Percent of damage taken from magic damage that can be absorbed by MekaSuit Helmet with Purification unit when it has enough power.")
              .defineInRange("magicDamageReductionRatio", 1D, 0, 1));
        mekaSuitUnspecifiedDamageRatio = CachedFloatValue.wrap(this, builder.comment("Percent of damage taken from other non explicitly supported damage types that don't bypass armor when the MekaSuit has enough power and a full suit is equipped.")
              .defineInRange("unspecifiedDamageReductionRatio", 1D, 0, 1));
        for (DamageSource type : ItemMekaSuitArmor.getSupportedSources()) {
            mekaSuitDamageRatios.put(type, CachedFloatValue.wrap(this, builder
                  .comment("Percent of damage taken from " + type.getMsgId() + " that can be absorbed by the MekaSuit when there is enough power and a full suit is equipped.")
                  .defineInRange(type.getMsgId() + "DamageReductionRatio", 1D, 0, 1)));
        }
        builder.pop(2);

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