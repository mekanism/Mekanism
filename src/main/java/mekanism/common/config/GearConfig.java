package mekanism.common.config;

import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.content.gear.mekasuit.ModuleGravitationalModulatingUnit;
import net.minecraft.SharedConstants;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.fluids.FluidType;

public class GearConfig extends BaseMekanismConfig {

    private static final String DISASSEMBLER_CATEGORY = "atomic_disassembler";
    private static final String ELECTRIC_BOW_CATEGORY = "electric_bow";
    private static final String ENERGY_TABLET_CATEGORY = "energy_tablet";
    private static final String GAUGE_DROPPER_CATEGORY = "gauge_dropper";
    private static final String FLAMETHROWER_CATEGORY = "flamethrower";
    public static final String FREE_RUNNER_CATEGORY = "free_runner";
    public static final String JETPACK_CATEGORY = "jetpack";
    private static final String NETWORK_READER_CATEGORY = "network_reader";
    private static final String PORTABLE_TELEPORTER_CATEGORY = "portable_teleporter";
    private static final String SCUBA_TANK_CATEGORY = "scuba_tank";
    private static final String SEISMIC_READER_CATEGORY = "seismic_reader";
    private static final String CANTEEN_CATEGORY = "canteen";
    private static final String MEKATOOL_CATEGORY = "mekatool";
    public static final String MEKASUIT_CATEGORY = "mekasuit";
    public static final String MEKASUIT_DAMAGE_CATEGORY = "damage_absorption";

    private final ModConfigSpec configSpec;

    //Atomic Disassembler
    public final CachedLongValue disassemblerEnergyUsage;
    public final CachedLongValue disassemblerEnergyUsageWeapon;
    public final CachedIntValue disassemblerMiningCount;
    public final CachedBooleanValue disassemblerSlowMode;
    public final CachedBooleanValue disassemblerFastMode;
    public final CachedBooleanValue disassemblerVeinMining;
    public final CachedIntValue disassemblerMinDamage;
    public final CachedIntValue disassemblerMaxDamage;
    public final CachedDoubleValue disassemblerAttackSpeed;
    public final CachedLongValue disassemblerMaxEnergy;
    public final CachedLongValue disassemblerChargeRate;
    
    //Electric Bow
    public final CachedLongValue electricBowMaxEnergy;
    public final CachedLongValue electricBowChargeRate;
    public final CachedLongValue electricBowEnergyUsage;
    public final CachedLongValue electricBowEnergyUsageFire;
    //Energy Tablet
    public final CachedLongValue tabletMaxEnergy;
    public final CachedLongValue tabletChargeRate;
    //Gauge Dropper
    public final CachedIntValue gaugeDroppedTransferRate;
    public final CachedIntValue gaugeDropperCapacity;
    //Flamethrower
    public final CachedLongValue flamethrowerMaxGas;
    public final CachedLongValue flamethrowerFillRate;
    public final CachedBooleanValue flamethrowerDestroyItems;
    //Free runner
    public final CachedLongValue freeRunnerFallEnergyCost;
    public final CachedFloatValue freeRunnerFallDamageRatio;
    public final CachedLongValue freeRunnerMaxEnergy;
    public final CachedLongValue freeRunnerChargeRate;
    //Jetpack
    public final CachedLongValue jetpackMaxGas;
    public final CachedLongValue jetpackFillRate;
    //Portable Teleporter
    public final CachedLongValue portableTeleporterMaxEnergy;
    public final CachedLongValue portableTeleporterChargeRate;
    public final CachedIntValue portableTeleporterDelay;
    //Network Reader
    public final CachedLongValue networkReaderMaxEnergy;
    public final CachedLongValue networkReaderChargeRate;
    public final CachedLongValue networkReaderEnergyUsage;
    //Scuba Tank
    public final CachedLongValue scubaMaxGas;
    public final CachedLongValue scubaFillRate;
    //Seismic Reader
    public final CachedLongValue seismicReaderMaxEnergy;
    public final CachedLongValue seismicReaderChargeRate;
    public final CachedLongValue seismicReaderEnergyUsage;
    //Canteen
    public final CachedIntValue canteenMaxStorage;
    public final CachedIntValue canteenTransferRate;
    //Meka-Tool
    public final CachedLongValue mekaToolEnergyUsageWeapon;
    public final CachedLongValue mekaToolEnergyUsageTeleport;
    public final CachedLongValue mekaToolEnergyUsage;
    public final CachedLongValue mekaToolEnergyUsageSilk;
    public final CachedIntValue mekaToolMaxTeleportReach;
    public final CachedIntValue mekaToolBaseDamage;
    public final CachedDoubleValue mekaToolAttackSpeed;
    public final CachedFloatValue mekaToolBaseEfficiency;
    public final CachedLongValue mekaToolBaseEnergyCapacity;
    public final CachedLongValue mekaToolBaseChargeRate;
    public final CachedLongValue mekaToolEnergyUsageHoe;
    public final CachedLongValue mekaToolEnergyUsageShovel;
    public final CachedLongValue mekaToolEnergyUsageAxe;
    public final CachedLongValue mekaToolEnergyUsageShearEntity;
    public final CachedLongValue mekaToolEnergyUsageShearTrim;
    public final CachedBooleanValue mekaToolExtendedMining;
    //MekaSuit
    public final CachedLongValue mekaSuitBaseEnergyCapacity;
    public final CachedLongValue mekaSuitBaseChargeRate;
    public final CachedLongValue mekaSuitBaseJumpEnergyUsage;
    public final CachedLongValue mekaSuitElytraEnergyUsage;
    public final CachedLongValue mekaSuitEnergyUsagePotionTick;
    public final CachedLongValue mekaSuitEnergyUsageMagicReduce;
    public final CachedLongValue mekaSuitEnergyUsageFall;
    public final CachedLongValue mekaSuitEnergyUsageSprintBoost;
    public final CachedLongValue mekaSuitEnergyUsageGravitationalModulation;
    public final CachedLongValue mekaSuitInventoryChargeRate;
    public final CachedLongValue mekaSuitSolarRechargingRate;
    public final CachedLongValue mekaSuitEnergyUsageVisionEnhancement;
    public final CachedLongValue mekaSuitEnergyUsageHydrostaticRepulsion;
    public final CachedLongValue mekaSuitEnergyUsageNutritionalInjection;
    public final CachedLongValue mekaSuitEnergyUsageDamage;
    public final CachedLongValue mekaSuitEnergyUsageItemAttraction;
    public final CachedBooleanValue mekaSuitGravitationalVibrations;
    public final CachedIntValue mekaSuitNutritionalMaxStorage;
    public final CachedIntValue mekaSuitNutritionalTransferRate;
    public final CachedLongValue mekaSuitJetpackMaxStorage;
    public final CachedLongValue mekaSuitJetpackTransferRate;

    public final CachedFloatValue mekaSuitFallDamageRatio;
    public final CachedFloatValue mekaSuitMagicDamageRatio;
    public final CachedFloatValue mekaSuitUnspecifiedDamageRatio;

    GearConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Atomic Disassembler Settings").push(DISASSEMBLER_CATEGORY);
        disassemblerEnergyUsage = CachedLongValue.definePositive(this, builder, "Base Energy (Joules) usage of the Atomic Disassembler. (Gets multiplied by speed factor)",
              "energyUsage", 10);
        disassemblerEnergyUsageWeapon = CachedLongValue.definePositive(this, builder, "Cost in Joules of using the Atomic Disassembler as a weapon.",
              "energyUsageWeapon", 2_000);
        disassemblerMiningCount = CachedIntValue.wrap(this, builder.comment("The max Atomic Disassembler Vein Mining Block Count.")
              .defineInRange("miningCount", 128, 2, 1_000_000));
        disassemblerSlowMode = CachedBooleanValue.wrap(this, builder.comment("Enable the 'Slow' mode for the Atomic Disassembler.")
              .define("slowMode", true));
        disassemblerFastMode = CachedBooleanValue.wrap(this, builder.comment("Enable the 'Fast' mode for the Atomic Disassembler.")
              .define("fastMode", true));
        disassemblerVeinMining = CachedBooleanValue.wrap(this, builder.comment("Enable the 'Vein Mining' mode for the Atomic Disassembler.")
              .define("veinMining", false));
        disassemblerMinDamage = CachedIntValue.wrap(this, builder.comment("The bonus attack damage of the Atomic Disassembler when it is out of power. (Value is in number of half hearts)")
              .defineInRange("minDamage", 4, 0, 1_000));
        disassemblerMaxDamage = CachedIntValue.wrap(this, builder.comment("The bonus attack damage of the Atomic Disassembler when it has at least energyUsageWeapon power stored. (Value is in number of half hearts)")
              .defineInRange("maxDamage", 20, 1, 10_000));
        disassemblerAttackSpeed = CachedDoubleValue.wrap(this, builder.comment("Attack speed of the Atomic Disassembler.")
              .defineInRange("attackSpeed", -2.4, -Attributes.ATTACK_SPEED.value().getDefaultValue(), 100));
        disassemblerMaxEnergy = CachedLongValue.definePositive(this, builder, "Maximum amount (joules) of energy the Atomic Disassembler can contain.",
              "maxEnergy", 1_000_000);
        disassemblerChargeRate = CachedLongValue.definePositive(this, builder, "Amount (joules) of energy the Atomic Disassembler can accept per tick.",
              "chargeRate", 5_000);
        builder.pop();

        builder.comment("Electric Bow Settings").push(ELECTRIC_BOW_CATEGORY);
        electricBowMaxEnergy = CachedLongValue.definePositive(this, builder, "Maximum amount (joules) of energy the Electric Bow can contain.",
              "maxEnergy", 120_000);
        electricBowChargeRate = CachedLongValue.definePositive(this, builder, "Amount (joules) of energy the Electric Bow can accept per tick.",
              "chargeRate", 600);
        electricBowEnergyUsage = CachedLongValue.definePositive(this, builder, "Cost in Joules of using the Electric Bow.",
              "energyUsage", 120);
        electricBowEnergyUsageFire = CachedLongValue.definePositive(this, builder, "Cost in Joules of using the Electric Bow with flame mode active.",
              "energyUsageFire", 1_200);
        builder.pop();

        builder.comment("Energy Tablet Settings").push(ENERGY_TABLET_CATEGORY);
        tabletMaxEnergy = CachedLongValue.definePositive(this, builder, "Maximum amount (joules) of energy the Energy Tablet can contain.",
              "maxEnergy", 1_000_000);
        tabletChargeRate = CachedLongValue.definePositive(this, builder, "Amount (joules) of energy the Energy Tablet can accept per tick.",
              "chargeRate", 5_000L);
        builder.pop();

        builder.comment("Gauge Dropper Settings").push(GAUGE_DROPPER_CATEGORY);
        gaugeDroppedTransferRate = CachedIntValue.wrap(this, builder.comment("Rate at which a gauge dropper can be filled or emptied.")
              .defineInRange("transferRate", 250, 1, Integer.MAX_VALUE));
        gaugeDropperCapacity = CachedIntValue.wrap(this, builder.comment("Capacity of gauge droppers.")
              .defineInRange("capacity", 16 * FluidType.BUCKET_VOLUME, 1, Integer.MAX_VALUE));
        builder.pop();

        builder.comment("Flamethrower Settings").push(FLAMETHROWER_CATEGORY);
        flamethrowerMaxGas = CachedLongValue.wrap(this, builder.comment("Flamethrower Gas Tank capacity in mB.")
              .defineInRange("maxGas", 24 * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        flamethrowerFillRate = CachedLongValue.wrap(this, builder.comment("Amount of hydrogen the Flamethrower can accept per tick.")
              .defineInRange("fillRate", 16, 1, Long.MAX_VALUE));
        flamethrowerDestroyItems = CachedBooleanValue.wrap(this, builder.comment("Determines whether or not the Flamethrower can destroy items if it fails to smelt them.")
              .define("destroyItems", true));
        builder.pop();

        builder.comment("Free Runner Settings").push(FREE_RUNNER_CATEGORY);
        freeRunnerFallEnergyCost = CachedLongValue.definePositive(this, builder, "Energy cost/multiplier in Joules for reducing fall damage with free runners. Energy cost is: FallDamage * freeRunnerFallEnergyCost. (1 FallDamage is 1 half heart)",
              "fallEnergyCost", 50L);
        freeRunnerFallDamageRatio = CachedFloatValue.wrap(this, builder.comment("Percent of damage taken from falling that can be absorbed by Free Runners when they have enough power.")
              .defineInRange("fallDamageReductionRatio", 1D, 0, 1));
        freeRunnerMaxEnergy = CachedLongValue.definePositive(this, builder, "Maximum amount (joules) of energy Free Runners can contain.",
              "maxEnergy", 64_000L);
        freeRunnerChargeRate = CachedLongValue.definePositive(this, builder, "Amount (joules) of energy the Free Runners can accept per tick.",
              "chargeRate", 320L);
        builder.pop();

        builder.comment("Jetpack Settings").push(JETPACK_CATEGORY);
        jetpackMaxGas = CachedLongValue.wrap(this, builder.comment("Jetpack Gas Tank capacity in mB.")
              .defineInRange("maxGas", 24 * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        jetpackFillRate = CachedLongValue.wrap(this, builder.comment("Amount of hydrogen the Jetpack can accept per tick.")
              .defineInRange("fillRate", 16, 1, Long.MAX_VALUE));
        builder.pop();

        builder.comment("Network Reader Settings").push(NETWORK_READER_CATEGORY);
        networkReaderMaxEnergy = CachedLongValue.definePositive(this, builder, "Maximum amount (joules) of energy the Network Reader can contain.",
              "maxEnergy", 60_000L);
        networkReaderChargeRate = CachedLongValue.definePositive(this, builder, "Amount (joules) of energy the Network Reader can accept per tick.",
              "chargeRate", 300L);
        networkReaderEnergyUsage = CachedLongValue.definePositive(this, builder, "Energy usage in joules for each network reading.",
              "energyUsage", 400L);
        builder.pop();

        builder.comment("Portable Teleporter Settings").push(PORTABLE_TELEPORTER_CATEGORY);
        portableTeleporterMaxEnergy = CachedLongValue.definePositive(this, builder, "Maximum amount (joules) of energy the Portable Teleporter can contain.",
              "maxEnergy", 1_000_000L);
        portableTeleporterChargeRate = CachedLongValue.definePositive(this, builder, "Amount (joules) of energy the Portable Teleporter can accept per tick.",
              "chargeRate", 5_000L);
        portableTeleporterDelay = CachedIntValue.wrap(this, builder.comment("Delay in ticks before a player is teleported after clicking the Teleport button in the portable teleporter.")
              .defineInRange("delay", 0, 0, 5 * SharedConstants.TICKS_PER_MINUTE));
        builder.pop();

        builder.comment("Scuba Tank Settings").push(SCUBA_TANK_CATEGORY);
        scubaMaxGas = CachedLongValue.wrap(this, builder.comment("Scuba Tank Gas Tank capacity in mB.")
              .defineInRange("maxGas", 24 * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        scubaFillRate = CachedLongValue.wrap(this, builder.comment("Amount of oxygen the Scuba Tank Gas Tank can accept per tick.")
              .defineInRange("fillRate", 16, 1, Long.MAX_VALUE));
        builder.pop();

        builder.comment("Seismic Reader Settings").push(SEISMIC_READER_CATEGORY);
        seismicReaderMaxEnergy = CachedLongValue.definePositive(this, builder, "Maximum amount (joules) of energy the Seismic Reader can contain.",
              "maxEnergy", 12_000L);
        seismicReaderChargeRate = CachedLongValue.definePositive(this, builder, "Amount (joules) of energy the Seismic Reader can accept per tick.",
              "chargeRate", 60L);
        seismicReaderEnergyUsage = CachedLongValue.definePositive(this, builder, "Energy usage in joules required to use the Seismic Reader.",
              "energyUsage", 250L);
        builder.pop();

        builder.comment("Canteen Settings").push(CANTEEN_CATEGORY);
        canteenMaxStorage = CachedIntValue.wrap(this, builder.comment("Maximum amount of Nutritional Paste storable by the Canteen.")
              .defineInRange("maxStorage", 64 * FluidType.BUCKET_VOLUME, 1, Integer.MAX_VALUE));
        canteenTransferRate = CachedIntValue.wrap(this, builder.comment("Rate at which Nutritional Paste can be transferred into a Canteen.")
              .defineInRange("transferRate", 128, 1, Integer.MAX_VALUE));
        builder.pop();

        builder.comment("Meka-Tool Settings").push(MEKATOOL_CATEGORY);
        mekaToolEnergyUsage = CachedLongValue.definePositive(this, builder, "Base energy (Joules) usage of the Meka-Tool. (Gets multiplied by speed factor)",
              "energyUsage", 10L);
        mekaToolEnergyUsageSilk = CachedLongValue.definePositive(this, builder, "Silk touch energy (Joules) usage of the Meka-Tool. (Gets multiplied by speed factor)",
              "energyUsageSilk", 100L);
        mekaToolEnergyUsageWeapon = CachedLongValue.definePositive(this, builder, "Cost in Joules of using the Meka-Tool to deal 4 units of damage.",
              "energyUsageWeapon", 2_000L);
        mekaToolEnergyUsageTeleport = CachedLongValue.definePositive(this, builder, "Cost in Joules of using the Meka-Tool to teleport 10 blocks.",
              "energyUsageTeleport", 1_000L);
        mekaToolMaxTeleportReach = CachedIntValue.wrap(this, builder.comment("Maximum distance a player can teleport with the Meka-Tool.")
              .defineInRange("maxTeleportReach", 100, 3, 1_024));
        mekaToolBaseDamage = CachedIntValue.wrap(this, builder.comment("Base bonus damage applied by the Meka-Tool without using any energy.")
              .defineInRange("baseDamage", 4, 0, 100_000));
        mekaToolAttackSpeed = CachedDoubleValue.wrap(this, builder.comment("Attack speed of the Meka-Tool.")
              .defineInRange("attackSpeed", -2.4, -Attributes.ATTACK_SPEED.value().getDefaultValue(), 100));
        mekaToolBaseEfficiency = CachedFloatValue.wrap(this, builder.comment("Efficiency of the Meka-Tool with energy but without any upgrades.")
              .defineInRange("baseEfficiency", 4, 0.1, 100));
        mekaToolBaseEnergyCapacity = CachedLongValue.definePositive(this, builder, "Energy capacity (Joules) of the Meka-Tool without any installed upgrades. Quadratically scaled by upgrades.",
              "baseEnergyCapacity", 16_000_000L);
        mekaToolBaseChargeRate = CachedLongValue.definePositive(this, builder, "Amount (joules) of energy the Meka-Tool can accept per tick. Quadratically scaled by upgrades.",
              "chargeRate", 100_000L);
        mekaToolEnergyUsageHoe = CachedLongValue.definePositive(this, builder, "Cost in Joules of using the Meka-Tool as a hoe.",
              "energyUsageHoe", 10L);
        mekaToolEnergyUsageShovel = CachedLongValue.definePositive(this, builder, "Cost in Joules of using the Meka-Tool as a shovel for making paths and dowsing campfires.",
              "energyUsageShovel", 10L);
        mekaToolEnergyUsageAxe = CachedLongValue.definePositive(this, builder, "Cost in Joules of using the Meka-Tool as an axe for stripping logs, scraping, or removing wax.",
              "energyUsageAxe", 10L);
        mekaToolEnergyUsageShearEntity = CachedLongValue.definePositive(this, builder, "Cost in Joules of using the Meka-Tool to shear entities.",
              "energyUsageShearEntity", 10L);
        mekaToolEnergyUsageShearTrim = CachedLongValue.definePositive(this, builder, "Cost in Joules of using the Meka-Tool to carefully shear and trim blocks.",
              "energyUsageShearTrim", 10L);
        mekaToolExtendedMining = CachedBooleanValue.wrap(this, builder.comment("Enable the 'Extended Vein Mining' mode for the Meka-Tool. (Allows vein mining everything not just ores/logs)")
              .define("extendedMining", true));
        builder.pop();

        MekanismConfigTranslations.GEAR_MEKA_SUIT.applyToBuilder(builder).push(MEKASUIT_CATEGORY);
        mekaSuitBaseEnergyCapacity = CachedLongValue.definePositive(this, builder, "Energy capacity (Joules) of MekaSuit items without any installed upgrades. Quadratically scaled by upgrades.",
              "baseEnergyCapacity", 16_000_000L);
        mekaSuitBaseChargeRate = CachedLongValue.definePositive(this, builder, "Amount (joules) of energy the MekaSuit can accept per tick. Quadratically scaled by upgrades.",
              "chargeRate", 100_000L);
        mekaSuitBaseJumpEnergyUsage = CachedLongValue.definePositive(this, builder, "Energy usage (Joules) of MekaSuit when adding 0.1 to jump motion.",
              "baseJumpEnergyUsage", 1_000L);
        mekaSuitElytraEnergyUsage = CachedLongValue.definePositive(this, builder, "Energy usage (Joules) per second of the MekaSuit when flying with the Elytra Unit.",
              "elytraEnergyUsage", 32_000L);
        mekaSuitEnergyUsagePotionTick = CachedLongValue.definePositive(this, builder, "Energy usage (Joules) of MekaSuit when lessening a potion effect.",
              "energyUsagePotionTick", 40_000L);
        mekaSuitEnergyUsageMagicReduce = CachedLongValue.definePositive(this, builder, "Energy cost/multiplier in Joules for reducing magic damage via the inhalation purification unit. Energy cost is: MagicDamage * energyUsageMagicPrevent. (1 MagicDamage is 1 half heart).",
              "energyUsageMagicReduce", 1_000L);
        mekaSuitEnergyUsageFall = CachedLongValue.definePositive(this, builder, "Energy cost/multiplier in Joules for reducing fall damage with MekaSuit Boots. Energy cost is: FallDamage * freeRunnerFallEnergyCost. (1 FallDamage is 1 half heart)",
              "energyUsageFall", 50L);
        mekaSuitEnergyUsageSprintBoost = CachedLongValue.definePositive(this, builder, "Energy usage (Joules) of MekaSuit when adding 0.1 to sprint motion.",
              "energyUsageSprintBoost", 100L);
        mekaSuitEnergyUsageGravitationalModulation = CachedLongValue.define(this, builder, "Energy usage (Joules) of MekaSuit per tick when flying via Gravitational Modulation.",
              "energyUsageGravitationalModulation", 1_000L, 0, Long.MAX_VALUE / ModuleGravitationalModulatingUnit.BOOST_ENERGY_MULTIPLIER);
        mekaSuitInventoryChargeRate = CachedLongValue.definePositive(this, builder, "Charge rate of inventory items (Joules) per tick.",
              "inventoryChargeRate", 10_000L);
        mekaSuitSolarRechargingRate = CachedLongValue.definePositive(this, builder, "Solar recharging rate (Joules) of helmet per tick, per upgrade installed.",
              "solarRechargingRate", 500L);
        mekaSuitEnergyUsageVisionEnhancement = CachedLongValue.definePositive(this, builder, "Energy usage (Joules) of MekaSuit per tick of using vision enhancement.",
              "energyUsageVisionEnhancement", 500L);
        mekaSuitEnergyUsageHydrostaticRepulsion = CachedLongValue.definePositive(this, builder, "Energy usage (Joules) of MekaSuit per tick of using hydrostatic repulsion.",
              "energyUsageHydrostaticRepulsion", 500L);
        mekaSuitEnergyUsageNutritionalInjection = CachedLongValue.definePositive(this, builder, "Energy usage (Joules) of MekaSuit per half-food of nutritional injection.",
              "energyUsageNutritionalInjection", 20_000L);
        mekaSuitEnergyUsageDamage = CachedLongValue.definePositive(this, builder, "Energy usage (Joules) of MekaSuit per unit of damage applied.",
              "energyUsageDamage", 100_000L);
        mekaSuitEnergyUsageItemAttraction = CachedLongValue.definePositive(this, builder, "Energy usage (Joules) of MekaSuit per tick of attracting a single item.",
              "energyUsageItemAttraction", 250L);
        mekaSuitGravitationalVibrations = CachedBooleanValue.wrap(this, builder.comment("Should the Gravitational Modulation unit give off vibrations when in use.")
              .define("gravitationalVibrations", true));
        mekaSuitNutritionalMaxStorage = CachedIntValue.wrap(this, builder.comment("Maximum amount of Nutritional Paste storable by the nutritional injection unit.")
              .defineInRange("nutritionalMaxStorage", 128 * FluidType.BUCKET_VOLUME, 1, Integer.MAX_VALUE));
        mekaSuitNutritionalTransferRate = CachedIntValue.wrap(this, builder.comment("Rate at which Nutritional Paste can be transferred into the nutritional injection unit.")
              .defineInRange("nutritionalTransferRate", 256, 1, Integer.MAX_VALUE));
        mekaSuitJetpackMaxStorage = CachedLongValue.wrap(this, builder.comment("Maximum amount of Hydrogen storable per installed jetpack unit.")
              .defineInRange("jetpackMaxStorage", 24 * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        mekaSuitJetpackTransferRate = CachedLongValue.wrap(this, builder.comment("Rate at which Hydrogen can be transferred into the jetpack unit.")
              .defineInRange("jetpackTransferRate", 256, 1, Long.MAX_VALUE));
        MekanismConfigTranslations.GEAR_MEKA_SUIT_DAMAGE_ABSORPTION.applyToBuilder(builder).push(MEKASUIT_DAMAGE_CATEGORY);
        mekaSuitFallDamageRatio = CachedFloatValue.wrap(this, builder.comment("Percent of damage taken from falling that can be absorbed by MekaSuit Boots when they have enough power.")
              .defineInRange("fallDamageReductionRatio", 1D, 0, 1));
        mekaSuitMagicDamageRatio = CachedFloatValue.wrap(this, builder.comment("Percent of damage taken from magic damage that can be absorbed by MekaSuit Helmet with Purification unit when it has enough power.")
              .defineInRange("magicDamageReductionRatio", 1D, 0, 1));
        mekaSuitUnspecifiedDamageRatio = CachedFloatValue.wrap(this, builder.comment("Percent of damage taken from other non explicitly supported damage types that don't bypass armor when the MekaSuit has enough power and a full suit is equipped.",
                    "Note: Support for specific damage types can be added by adding an entry for the damage type in the mekanism:mekasuit_absorption data map.")
              .defineInRange("unspecifiedDamageReductionRatio", 1D, 0, 1));
        builder.pop(2);

        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "gear";
    }

    @Override
    public String getTranslation() {
        return "Gear Config";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }
}
