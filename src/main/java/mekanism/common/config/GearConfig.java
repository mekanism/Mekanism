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

    public static final String FREE_RUNNER_CATEGORY = "free_runner";
    public static final String JETPACK_CATEGORY = "jetpack";
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
    public final CachedLongValue flamethrowerCapacity;
    public final CachedLongValue flamethrowerFillRate;
    public final CachedBooleanValue flamethrowerDestroyItems;
    //Free runner
    public final CachedLongValue freeRunnerFallEnergyCost;
    public final CachedFloatValue freeRunnerFallDamageRatio;
    public final CachedLongValue freeRunnerMaxEnergy;
    public final CachedLongValue freeRunnerChargeRate;
    //Jetpack
    public final CachedLongValue jetpackCapacity;
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
    public final CachedLongValue scubaTankCapacity;
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

        MekanismConfigTranslations.GEAR_DISASSEMBLER.applyToBuilder(builder).push("atomic_disassembler");
        disassemblerMaxEnergy = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_DISASSEMBLER_MAX_ENERGY, "maxEnergy", 1_000_000);
        disassemblerChargeRate = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_DISASSEMBLER_CHARGE_RATE, "chargeRate", 5_000);
        disassemblerEnergyUsage = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_DISASSEMBLER_ENERGY_USAGE, "energyUsage", 10);
        disassemblerEnergyUsageWeapon = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_DISASSEMBLER_ENERGY_USAGE_WEAPON, "energyUsageWeapon", 2_000);
        disassemblerMinDamage = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_DISASSEMBLER_MIN_DAMAGE.applyToBuilder(builder)
              .defineInRange("minDamage", 4, 0, 1_000));
        disassemblerMaxDamage = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_DISASSEMBLER_MAX_DAMAGE.applyToBuilder(builder)
              .defineInRange("maxDamage", 20, 1, 10_000));
        disassemblerAttackSpeed = CachedDoubleValue.wrap(this, MekanismConfigTranslations.GEAR_DISASSEMBLER_ATTACK_SPEED.applyToBuilder(builder)
              .defineInRange("attackSpeed", -2.4, -Attributes.ATTACK_SPEED.value().getDefaultValue(), 100));
        disassemblerSlowMode = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GEAR_DISASSEMBLER_SLOW.applyToBuilder(builder)
              .define("slowMode", true));
        disassemblerFastMode = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GEAR_DISASSEMBLER_FAST.applyToBuilder(builder)
              .define("fastMode", true));
        disassemblerVeinMining = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GEAR_DISASSEMBLER_VEIN_MINING.applyToBuilder(builder)
              .define("veinMining", false));
        disassemblerMiningCount = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_DISASSEMBLER_MINING_COUNT.applyToBuilder(builder)
              .defineInRange("miningCount", 128, 2, 1_000_000));
        builder.pop();

        MekanismConfigTranslations.GEAR_BOW.applyToBuilder(builder).push("electric_bow");
        electricBowMaxEnergy = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_BOW_MAX_ENERGY, "maxEnergy", 120_000);
        electricBowChargeRate = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_BOW_CHARGE_RATE, "chargeRate", 600);
        electricBowEnergyUsage = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_BOW_ENERGY_USAGE, "energyUsage", 120);
        electricBowEnergyUsageFire = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_BOW_ENERGY_USAGE_FLAME, "energyUsageFlame", 1_200);
        builder.pop();

        MekanismConfigTranslations.GEAR_ENERGY_TABLET.applyToBuilder(builder).push("energy_tablet");
        tabletMaxEnergy = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_ENERGY_TABLET_MAX_ENERGY, "maxEnergy", 1_000_000);
        tabletChargeRate = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_ENERGY_TABLET_CHARGE_RATE, "chargeRate", 5_000L);
        builder.pop();

        MekanismConfigTranslations.GEAR_GAUGE_DROPPER.applyToBuilder(builder).push("gauge_dropper");
        gaugeDropperCapacity = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_GAUGE_DROPPER_CAPACITY.applyToBuilder(builder)
              .defineInRange("capacity", 16 * FluidType.BUCKET_VOLUME, 1, Integer.MAX_VALUE));
        gaugeDroppedTransferRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_GAUGE_DROPPER_TRANSFER_RATE.applyToBuilder(builder)
              .defineInRange("transferRate", 250, 1, Integer.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GEAR_FLAMETHROWER.applyToBuilder(builder).push("flamethrower");
        flamethrowerCapacity = CachedLongValue.wrap(this, MekanismConfigTranslations.GEAR_FLAMETHROWER_CAPACITY.applyToBuilder(builder)
              .defineInRange("capacity", 24 * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        flamethrowerFillRate = CachedLongValue.wrap(this, MekanismConfigTranslations.GEAR_FLAMETHROWER_FILL_RATE.applyToBuilder(builder)
              .defineInRange("fillRate", 16, 1, Long.MAX_VALUE));
        flamethrowerDestroyItems = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GEAR_FLAMETHROWER_DESTROY_ITEMS.applyToBuilder(builder)
              .define("destroyItems", true));
        builder.pop();

        MekanismConfigTranslations.GEAR_FREE_RUNNERS.applyToBuilder(builder).push(FREE_RUNNER_CATEGORY);
        freeRunnerMaxEnergy = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_FREE_RUNNERS_MAX_ENERGY, "maxEnergy", 64_000L);
        freeRunnerChargeRate = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_FREE_RUNNERS_CHARGE_RATE, "chargeRate", 320L);
        freeRunnerFallEnergyCost = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_FREE_RUNNERS_FALL_COST, "fallEnergyCost", 50L);
        freeRunnerFallDamageRatio = CachedFloatValue.wrap(this, MekanismConfigTranslations.GEAR_FREE_RUNNERS_FALL_DAMAGE.applyToBuilder(builder)
              .defineInRange("fallDamageReductionRatio", 1D, 0, 1));
        builder.pop();

        MekanismConfigTranslations.GEAR_JETPACK.applyToBuilder(builder).push(JETPACK_CATEGORY);
        jetpackCapacity = CachedLongValue.wrap(this, MekanismConfigTranslations.GEAR_JETPACK_CAPACITY.applyToBuilder(builder)
              .defineInRange("capacity", 24 * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        jetpackFillRate = CachedLongValue.wrap(this, MekanismConfigTranslations.GEAR_JETPACK_FILL_RATE.applyToBuilder(builder)
              .defineInRange("fillRate", 16, 1, Long.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GEAR_NETWORK_READER.applyToBuilder(builder).push("network_reader");
        networkReaderMaxEnergy = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_NETWORK_READER_MAX_ENERGY, "maxEnergy", 60_000L);
        networkReaderChargeRate = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_NETWORK_READER_CHARGE_RATE, "chargeRate", 300L);
        networkReaderEnergyUsage = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_NETWORK_READER_ENERGY_USAGE, "energyUsage", 400L);
        builder.pop();

        MekanismConfigTranslations.GEAR_PORTABLE_TELEPORTER.applyToBuilder(builder).push("portable_teleporter");
        portableTeleporterMaxEnergy = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_PORTABLE_TELEPORTER_MAX_ENERGY, "maxEnergy", 1_000_000L);
        portableTeleporterChargeRate = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_PORTABLE_TELEPORTER_CHARGE_RATE, "chargeRate", 5_000L);
        portableTeleporterDelay = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_PORTABLE_TELEPORTER_DELAY.applyToBuilder(builder)
              .defineInRange("delay", 0, 0, 5 * SharedConstants.TICKS_PER_MINUTE));
        builder.pop();

        MekanismConfigTranslations.GEAR_SCUBA_TANK.applyToBuilder(builder).push("scuba_tank");
        scubaTankCapacity = CachedLongValue.wrap(this, MekanismConfigTranslations.GEAR_SCUBA_TANK_CAPACITY.applyToBuilder(builder)
              .defineInRange("capacity", 24 * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        scubaFillRate = CachedLongValue.wrap(this, MekanismConfigTranslations.GEAR_SCUBA_TANK_FILL_RATE.applyToBuilder(builder)
              .defineInRange("fillRate", 16, 1, Long.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GEAR_SEISMIC_READER.applyToBuilder(builder).push("seismic_reader");
        seismicReaderMaxEnergy = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_SEISMIC_READER_MAX_ENERGY, "maxEnergy", 12_000L);
        seismicReaderChargeRate = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_SEISMIC_READER_CHARGE_RATE, "chargeRate", 60L);
        seismicReaderEnergyUsage = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_SEISMIC_READER_ENERGY_USAGE, "energyUsage", 250L);
        builder.pop();

        MekanismConfigTranslations.GEAR_CANTEEN.applyToBuilder(builder).push("canteen");
        canteenMaxStorage = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_CANTEEN_CAPACITY.applyToBuilder(builder)
              .defineInRange("maxStorage", 64 * FluidType.BUCKET_VOLUME, 1, Integer.MAX_VALUE));
        canteenTransferRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_CANTEEN_TRANSFER_RATE.applyToBuilder(builder)
              .defineInRange("transferRate", 128, 1, Integer.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GEAR_MEKA_TOOL.applyToBuilder(builder).push("mekatool");
        mekaToolBaseEnergyCapacity = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_TOOL_CAPACITY, "baseEnergyCapacity", 16_000_000L);
        mekaToolBaseChargeRate = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_TOOL_CHARGE_RATE, "chargeRate", 100_000L);
        mekaToolBaseDamage = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_TOOL_DAMAGE.applyToBuilder(builder)
              .defineInRange("baseDamage", 4, 0, 100_000));
        mekaToolAttackSpeed = CachedDoubleValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_TOOL_ATTACK_SPEED.applyToBuilder(builder)
              .defineInRange("attackSpeed", -2.4, -Attributes.ATTACK_SPEED.value().getDefaultValue(), 100));
        mekaToolBaseEfficiency = CachedFloatValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_TOOL_EFFICIENCY.applyToBuilder(builder)
              .defineInRange("baseEfficiency", 4, 0.1, 100));
        mekaToolExtendedMining = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_TOOL_EXTENDED_VEIN.applyToBuilder(builder)
              .define("extendedMining", true));
        mekaToolMaxTeleportReach = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_TOOL_TELEPORTATION_DISTANCE.applyToBuilder(builder)
              .defineInRange("maxTeleportReach", 100, 3, 1_024));

        MekanismConfigTranslations.GEAR_MEKA_TOOL_ENERGY_USAGE.applyToBuilder(builder).push("energy_usage");
        mekaToolEnergyUsage = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_TOOL_ENERGY_USAGE_BASE, "base", 10L);
        mekaToolEnergyUsageSilk = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_TOOL_ENERGY_USAGE_SILK, "silk", 100L);
        mekaToolEnergyUsageWeapon = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_TOOL_ENERGY_USAGE_WEAPON, "weapon", 2_000L);
        mekaToolEnergyUsageHoe = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_TOOL_HOE, "hoe", 10L);
        mekaToolEnergyUsageShovel = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_TOOL_SHOVEL, "shovel", 10L);
        mekaToolEnergyUsageAxe = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_TOOL_AXE, "axe", 10L);
        mekaToolEnergyUsageShearEntity = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_TOOL_SHEAR_ENTITY, "shearEntity", 10L);
        mekaToolEnergyUsageShearTrim = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_TOOL_SHEAR_BLOCK, "shearTrim", 10L);
        mekaToolEnergyUsageTeleport = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_TOOL_ENERGY_USAGE_TELEPORT, "teleport", 1_000L);
        builder.pop(2);

        MekanismConfigTranslations.GEAR_MEKA_SUIT.applyToBuilder(builder).push(MEKASUIT_CATEGORY);
        mekaSuitBaseEnergyCapacity = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_CAPACITY, "baseEnergyCapacity", 16_000_000L);
        mekaSuitBaseChargeRate = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_CHARGE_RATE, "chargeRate", 100_000L);
        mekaSuitInventoryChargeRate = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_CHARGE_RATE_INVENTORY, "inventoryChargeRate", 10_000L);
        mekaSuitSolarRechargingRate = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_CHARGE_RATE_SOLAR, "solarRechargingRate", 500L);
        mekaSuitGravitationalVibrations = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_FLIGHT_VIBRATIONS.applyToBuilder(builder)
              .define("gravitationalVibrations", true));
        mekaSuitNutritionalMaxStorage = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_PASTE_CAPACITY.applyToBuilder(builder)
              .defineInRange("nutritionalMaxStorage", 128 * FluidType.BUCKET_VOLUME, 1, Integer.MAX_VALUE));
        mekaSuitNutritionalTransferRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_PASTE_TRANSFER_RATE.applyToBuilder(builder)
              .defineInRange("nutritionalTransferRate", 256, 1, Integer.MAX_VALUE));
        mekaSuitJetpackMaxStorage = CachedLongValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_JETPACK_CAPACITY.applyToBuilder(builder)
              .defineInRange("jetpackMaxStorage", 24 * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        mekaSuitJetpackTransferRate = CachedLongValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_JETPACK_TRANSFER_RATE.applyToBuilder(builder)
              .defineInRange("jetpackTransferRate", 256, 1, Long.MAX_VALUE));

        MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE.applyToBuilder(builder).push("energy_usage");
        mekaSuitEnergyUsageDamage = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_DAMAGE, "damage", 100_000L);
        mekaSuitEnergyUsageMagicReduce = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_MAGIC, "magicReduce", 1_000L);
        mekaSuitEnergyUsageFall = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_FALL, "fall", 50L);
        mekaSuitBaseJumpEnergyUsage = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_JUMP, "jump", 1_000L);
        mekaSuitElytraEnergyUsage = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_ELYTRA, "elytra", 32_000L);
        mekaSuitEnergyUsagePotionTick = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_POTION, "energyUsagePotionTick", 40_000L);
        mekaSuitEnergyUsageSprintBoost = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_SPRINT, "sprintBoost", 100L);
        mekaSuitEnergyUsageGravitationalModulation = CachedLongValue.define(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_FLIGHT,
              "gravitationalModulation", 1_000L, 0, Long.MAX_VALUE / ModuleGravitationalModulatingUnit.BOOST_ENERGY_MULTIPLIER);
        mekaSuitEnergyUsageVisionEnhancement = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_VISION, "visionEnhancement", 500L);
        mekaSuitEnergyUsageHydrostaticRepulsion = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_SWIM, "hydrostaticRepulsion", 500L);
        mekaSuitEnergyUsageNutritionalInjection = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_FOOD, "nutritionalInjection", 20_000L);
        mekaSuitEnergyUsageItemAttraction = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_MAGNET, "itemAttraction", 250L);
        builder.pop();

        MekanismConfigTranslations.GEAR_MEKA_SUIT_DAMAGE_ABSORPTION.applyToBuilder(builder).push(MEKASUIT_DAMAGE_CATEGORY);
        mekaSuitFallDamageRatio = CachedFloatValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_ABSORPTION_FALL.applyToBuilder(builder)
              .defineInRange("fallDamageReductionRatio", 1D, 0, 1));
        mekaSuitMagicDamageRatio = CachedFloatValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_ABSORPTION_MAGIC.applyToBuilder(builder)
              .defineInRange("magicDamageReductionRatio", 1D, 0, 1));
        mekaSuitUnspecifiedDamageRatio = CachedFloatValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_ABSORPTION_UNSPECIFIED.applyToBuilder(builder)
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
