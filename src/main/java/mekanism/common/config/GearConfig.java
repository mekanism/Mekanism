package mekanism.common.config;

import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.content.gear.mekasuit.ModuleGravitationalModulatingUnit;
import net.minecraft.SharedConstants;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodConstants;
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
    public final CachedIntValue disassemblerEnergyUsage;
    public final CachedIntValue disassemblerEnergyUsageWeapon;
    public final CachedIntValue disassemblerMiningCount;
    public final CachedBooleanValue disassemblerSlowMode;
    public final CachedBooleanValue disassemblerFastMode;
    public final CachedBooleanValue disassemblerVeinMining;
    public final CachedIntValue disassemblerMinDamage;
    public final CachedIntValue disassemblerMaxDamage;
    public final CachedDoubleValue disassemblerAttackSpeed;
    public final CachedLongValue disassemblerMaxEnergy;
    public final CachedIntValue disassemblerChargeRate;

    //Electric Bow
    public final CachedLongValue electricBowMaxEnergy;
    public final CachedIntValue electricBowChargeRate;
    public final CachedIntValue electricBowEnergyUsage;
    public final CachedIntValue electricBowEnergyUsageFire;
    //Energy Tablet
    public final CachedLongValue tabletMaxEnergy;
    public final CachedIntValue tabletChargeRate;
    //Gauge Dropper
    public final CachedIntValue gaugeDroppedTransferRate;
    public final CachedLongValue gaugeDropperCapacity;
    //Flamethrower
    public final CachedLongValue flamethrowerCapacity;
    public final CachedIntValue flamethrowerFillRate;
    public final CachedBooleanValue flamethrowerDestroyItems;
    //Free runner
    public final CachedIntValue freeRunnerFallEnergyCost;
    public final CachedFloatValue freeRunnerFallDamageRatio;
    public final CachedLongValue freeRunnerMaxEnergy;
    public final CachedIntValue freeRunnerChargeRate;
    //Jetpack
    public final CachedLongValue jetpackCapacity;
    public final CachedIntValue jetpackFillRate;
    //Portable Teleporter
    public final CachedLongValue portableTeleporterMaxEnergy;
    public final CachedIntValue portableTeleporterChargeRate;
    public final CachedIntValue portableTeleporterDelay;
    //Network Reader
    public final CachedLongValue networkReaderMaxEnergy;
    public final CachedIntValue networkReaderChargeRate;
    public final CachedIntValue networkReaderEnergyUsage;
    //Scuba Tank
    public final CachedLongValue scubaTankCapacity;
    public final CachedIntValue scubaFillRate;
    //Seismic Reader
    public final CachedLongValue seismicReaderMaxEnergy;
    public final CachedIntValue seismicReaderChargeRate;
    public final CachedIntValue seismicReaderEnergyUsage;
    //Canteen
    public final CachedLongValue canteenMaxStorage;
    public final CachedIntValue canteenTransferRate;
    //Meka-Tool
    public final CachedIntValue mekaToolEnergyUsageWeapon;
    public final CachedIntValue mekaToolEnergyUsageTeleport;
    public final CachedIntValue mekaToolEnergyUsage;
    public final CachedIntValue mekaToolEnergyUsageSilk;
    public final CachedIntValue mekaToolMaxTeleportReach;
    public final CachedIntValue mekaToolBaseDamage;
    public final CachedDoubleValue mekaToolAttackSpeed;
    public final CachedFloatValue mekaToolBaseEfficiency;
    public final CachedLongValue mekaToolBaseEnergyCapacity;
    public final CachedIntValue mekaToolBaseChargeRate;
    public final CachedIntValue mekaToolEnergyUsageHoe;
    public final CachedIntValue mekaToolEnergyUsageShovel;
    public final CachedIntValue mekaToolEnergyUsageAxe;
    public final CachedIntValue mekaToolEnergyUsageShearEntity;
    public final CachedIntValue mekaToolEnergyUsageShearTrim;
    public final CachedBooleanValue mekaToolExtendedMining;
    //MekaSuit
    public final CachedLongValue mekaSuitBaseEnergyCapacity;
    public final CachedIntValue mekaSuitBaseChargeRate;
    public final CachedIntValue mekaSuitBaseJumpEnergyUsage;
    public final CachedIntValue mekaSuitElytraEnergyUsage;
    public final CachedIntValue mekaSuitEnergyUsagePotionTick;
    public final CachedIntValue mekaSuitEnergyUsageMagicReduce;
    public final CachedIntValue mekaSuitEnergyUsageFall;
    public final CachedIntValue mekaSuitEnergyUsageSprintBoost;
    public final CachedIntValue mekaSuitEnergyUsageGravitationalModulation;
    public final CachedIntValue mekaSuitInventoryChargeRate;
    public final CachedIntValue mekaSuitSolarRechargingRate;
    public final CachedIntValue mekaSuitEnergyUsageVisionEnhancement;
    public final CachedIntValue mekaSuitEnergyUsageHydrostaticRepulsion;
    public final CachedIntValue mekaSuitEnergyUsageNutritionalInjection;
    public final CachedIntValue mekaSuitEnergyUsageDamage;
    public final CachedIntValue mekaSuitEnergyUsageItemAttraction;
    public final CachedBooleanValue mekaSuitGravitationalVibrations;
    public final CachedLongValue mekaSuitNutritionalMaxStorage;
    public final CachedIntValue mekaSuitNutritionalTransferRate;
    public final CachedLongValue mekaSuitJetpackMaxStorage;
    public final CachedIntValue mekaSuitJetpackTransferRate;

    public final CachedFloatValue mekaSuitFallDamageRatio;
    public final CachedFloatValue mekaSuitMagicDamageRatio;
    public final CachedFloatValue mekaSuitUnspecifiedDamageRatio;

    GearConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        MekanismConfigTranslations.GEAR_DISASSEMBLER.applyToBuilder(builder).push("atomic_disassembler");
        disassemblerMaxEnergy = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_DISASSEMBLER_MAX_ENERGY, "maxEnergy", 1_000_000);
        disassemblerChargeRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_DISASSEMBLER_CHARGE_RATE.applyToBuilder(builder)
              .defineInRange("chargeRate", 5_000, 0, Integer.MAX_VALUE));
        disassemblerEnergyUsage = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_DISASSEMBLER_ENERGY_USAGE.applyToBuilder(builder)
              .defineInRange("energyUsage", 10, 0, Integer.MAX_VALUE));
        disassemblerEnergyUsageWeapon = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_DISASSEMBLER_ENERGY_USAGE_WEAPON.applyToBuilder(builder)
              .defineInRange("energyUsageWeapon", 2_000, 0, Integer.MAX_VALUE));
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
        electricBowChargeRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_BOW_CHARGE_RATE.applyToBuilder(builder)
              .defineInRange("chargeRate", 600, 0, Integer.MAX_VALUE));
        electricBowEnergyUsage = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_BOW_ENERGY_USAGE.applyToBuilder(builder)
              .defineInRange("energyUsage", 120, 0, Integer.MAX_VALUE));
        electricBowEnergyUsageFire = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_BOW_ENERGY_USAGE_FLAME.applyToBuilder(builder)
              .defineInRange("energyUsageFlame", 1_200, 0, Integer.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GEAR_ENERGY_TABLET.applyToBuilder(builder).push("energy_tablet");
        tabletMaxEnergy = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_ENERGY_TABLET_MAX_ENERGY, "maxEnergy", 1_000_000);
        tabletChargeRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_ENERGY_TABLET_CHARGE_RATE.applyToBuilder(builder)
              .defineInRange("chargeRate", 5_000, 0, Integer.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GEAR_GAUGE_DROPPER.applyToBuilder(builder).push("gauge_dropper");
        gaugeDropperCapacity = CachedLongValue.wrap(this, MekanismConfigTranslations.GEAR_GAUGE_DROPPER_CAPACITY.applyToBuilder(builder)
              .defineInRange("capacity", 16L * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        gaugeDroppedTransferRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_GAUGE_DROPPER_TRANSFER_RATE.applyToBuilder(builder)
              .defineInRange("transferRate", 250, 1, Integer.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GEAR_FLAMETHROWER.applyToBuilder(builder).push("flamethrower");
        flamethrowerCapacity = CachedLongValue.wrap(this, MekanismConfigTranslations.GEAR_FLAMETHROWER_CAPACITY.applyToBuilder(builder)
              .defineInRange("capacity", 24L * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        flamethrowerFillRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_FLAMETHROWER_FILL_RATE.applyToBuilder(builder)
              .defineInRange("fillRate", 16, 1, Integer.MAX_VALUE));
        flamethrowerDestroyItems = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GEAR_FLAMETHROWER_DESTROY_ITEMS.applyToBuilder(builder)
              .define("destroyItems", true));
        builder.pop();

        MekanismConfigTranslations.GEAR_FREE_RUNNERS.applyToBuilder(builder).push(FREE_RUNNER_CATEGORY);
        freeRunnerMaxEnergy = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_FREE_RUNNERS_MAX_ENERGY, "maxEnergy", 64_000L);
        freeRunnerChargeRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_FREE_RUNNERS_CHARGE_RATE.applyToBuilder(builder)
              .defineInRange("chargeRate", 320, 0, Integer.MAX_VALUE));
        freeRunnerFallEnergyCost = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_FREE_RUNNERS_FALL_COST.applyToBuilder(builder)
              .defineInRange("fallEnergyCost", 50, 0, Integer.MAX_VALUE));
        freeRunnerFallDamageRatio = CachedFloatValue.wrap(this, MekanismConfigTranslations.GEAR_FREE_RUNNERS_FALL_DAMAGE.applyToBuilder(builder)
              .defineInRange("fallDamageReductionRatio", 1D, 0, 1));
        builder.pop();

        MekanismConfigTranslations.GEAR_JETPACK.applyToBuilder(builder).push(JETPACK_CATEGORY);
        jetpackCapacity = CachedLongValue.wrap(this, MekanismConfigTranslations.GEAR_JETPACK_CAPACITY.applyToBuilder(builder)
              .defineInRange("capacity", 24L * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        jetpackFillRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_JETPACK_FILL_RATE.applyToBuilder(builder)
              .defineInRange("fillRate", 16, 1, Integer.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GEAR_NETWORK_READER.applyToBuilder(builder).push("network_reader");
        networkReaderMaxEnergy = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_NETWORK_READER_MAX_ENERGY, "maxEnergy", 60_000L);
        networkReaderChargeRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_NETWORK_READER_CHARGE_RATE.applyToBuilder(builder)
              .defineInRange("chargeRate", 300, 0, Integer.MAX_VALUE));
        networkReaderEnergyUsage = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_NETWORK_READER_ENERGY_USAGE.applyToBuilder(builder)
              .defineInRange("energyUsage", 400, 0, Integer.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GEAR_PORTABLE_TELEPORTER.applyToBuilder(builder).push("portable_teleporter");
        portableTeleporterMaxEnergy = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_PORTABLE_TELEPORTER_MAX_ENERGY, "maxEnergy", 1_000_000L);
        portableTeleporterChargeRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_PORTABLE_TELEPORTER_CHARGE_RATE.applyToBuilder(builder)
              .defineInRange("chargeRate", 5_000, 0, Integer.MAX_VALUE));
        portableTeleporterDelay = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_PORTABLE_TELEPORTER_DELAY.applyToBuilder(builder)
              .defineInRange("delay", 0, 0, 5 * SharedConstants.TICKS_PER_MINUTE));
        builder.pop();

        MekanismConfigTranslations.GEAR_SCUBA_TANK.applyToBuilder(builder).push("scuba_tank");
        scubaTankCapacity = CachedLongValue.wrap(this, MekanismConfigTranslations.GEAR_SCUBA_TANK_CAPACITY.applyToBuilder(builder)
              .defineInRange("capacity", 24L * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        scubaFillRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_SCUBA_TANK_FILL_RATE.applyToBuilder(builder)
              .defineInRange("fillRate", 16, 1, Integer.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GEAR_SEISMIC_READER.applyToBuilder(builder).push("seismic_reader");
        seismicReaderMaxEnergy = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_SEISMIC_READER_MAX_ENERGY, "maxEnergy", 12_000L);
        seismicReaderChargeRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_SEISMIC_READER_CHARGE_RATE.applyToBuilder(builder)
              .defineInRange("chargeRate", 60, 0, Integer.MAX_VALUE));
        seismicReaderEnergyUsage = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_SEISMIC_READER_ENERGY_USAGE.applyToBuilder(builder)
              .defineInRange("energyUsage", 250, 0, Integer.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GEAR_CANTEEN.applyToBuilder(builder).push("canteen");
        canteenMaxStorage = CachedLongValue.wrap(this, MekanismConfigTranslations.GEAR_CANTEEN_CAPACITY.applyToBuilder(builder)
              .defineInRange("maxStorage", 64L * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        canteenTransferRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_CANTEEN_TRANSFER_RATE.applyToBuilder(builder)
              .defineInRange("transferRate", 128, 1, Integer.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GEAR_MEKA_TOOL.applyToBuilder(builder).push("mekatool");
        mekaToolBaseEnergyCapacity = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_TOOL_CAPACITY, "baseEnergyCapacity", 16_000_000L);
        mekaToolBaseChargeRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_TOOL_CHARGE_RATE.applyToBuilder(builder)
              .defineInRange("chargeRate", 100_000, 0, Integer.MAX_VALUE));
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
        mekaToolEnergyUsage = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_TOOL_ENERGY_USAGE_BASE.applyToBuilder(builder)
              .defineInRange("base", 10, 0, Integer.MAX_VALUE));
        mekaToolEnergyUsageSilk = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_TOOL_ENERGY_USAGE_SILK.applyToBuilder(builder)
              .defineInRange("silk", 100, 0, Integer.MAX_VALUE));
        mekaToolEnergyUsageWeapon = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_TOOL_ENERGY_USAGE_WEAPON.applyToBuilder(builder)
              .defineInRange("weapon", 2_000, 0, Integer.MAX_VALUE));
        mekaToolEnergyUsageHoe = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_TOOL_HOE.applyToBuilder(builder)
              .defineInRange("hoe", 10, 0, Integer.MAX_VALUE));
        mekaToolEnergyUsageShovel = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_TOOL_SHOVEL.applyToBuilder(builder)
              .defineInRange("shovel", 10, 0, Integer.MAX_VALUE));
        mekaToolEnergyUsageAxe = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_TOOL_AXE.applyToBuilder(builder)
              .defineInRange("axe", 10, 0, Integer.MAX_VALUE));
        mekaToolEnergyUsageShearEntity = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_TOOL_SHEAR_ENTITY.applyToBuilder(builder)
              .defineInRange("shearEntity", 10, 0, Integer.MAX_VALUE));
        mekaToolEnergyUsageShearTrim = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_TOOL_SHEAR_BLOCK.applyToBuilder(builder)
              .defineInRange("shearTrim", 10, 0, Integer.MAX_VALUE));
        mekaToolEnergyUsageTeleport = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_TOOL_ENERGY_USAGE_TELEPORT.applyToBuilder(builder)
              .defineInRange("teleport", 1_000, 0, Integer.MAX_VALUE));
        builder.pop(2);

        MekanismConfigTranslations.GEAR_MEKA_SUIT.applyToBuilder(builder).push(MEKASUIT_CATEGORY);
        mekaSuitBaseEnergyCapacity = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GEAR_MEKA_SUIT_CAPACITY, "baseEnergyCapacity", 16_000_000L);
        mekaSuitBaseChargeRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_CHARGE_RATE.applyToBuilder(builder)
              .defineInRange("chargeRate", 100_000, 0, Integer.MAX_VALUE));
        mekaSuitInventoryChargeRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_CHARGE_RATE_INVENTORY.applyToBuilder(builder)
              .defineInRange("inventoryChargeRate", 10_000, 0, Integer.MAX_VALUE));
        mekaSuitSolarRechargingRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_CHARGE_RATE_SOLAR.applyToBuilder(builder)
              .defineInRange("solarRechargingRate", 500, 0, Integer.MAX_VALUE));
        mekaSuitGravitationalVibrations = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_FLIGHT_VIBRATIONS.applyToBuilder(builder)
              .define("gravitationalVibrations", true));
        mekaSuitNutritionalMaxStorage = CachedLongValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_PASTE_CAPACITY.applyToBuilder(builder)
              .defineInRange("nutritionalMaxStorage", 128L * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        mekaSuitNutritionalTransferRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_PASTE_TRANSFER_RATE.applyToBuilder(builder)
              .defineInRange("nutritionalTransferRate", 256, 1, Integer.MAX_VALUE));
        mekaSuitJetpackMaxStorage = CachedLongValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_JETPACK_CAPACITY.applyToBuilder(builder)
              .defineInRange("jetpackMaxStorage", 24L * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        mekaSuitJetpackTransferRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_JETPACK_TRANSFER_RATE.applyToBuilder(builder)
              .defineInRange("jetpackTransferRate", 256, 1, Integer.MAX_VALUE));

        MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE.applyToBuilder(builder).push("energy_usage");
        mekaSuitEnergyUsageDamage = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_DAMAGE.applyToBuilder(builder)
              .defineInRange("damage", 100_000, 0, Integer.MAX_VALUE));
        mekaSuitEnergyUsageMagicReduce = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_MAGIC.applyToBuilder(builder)
              .defineInRange("magicReduce", 1_000, 0, Integer.MAX_VALUE));
        mekaSuitEnergyUsageFall = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_FALL.applyToBuilder(builder)
              .defineInRange("fall", 50, 0, Integer.MAX_VALUE));
        mekaSuitBaseJumpEnergyUsage = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_JUMP.applyToBuilder(builder)
              .defineInRange("jump", 1_000, 0, Integer.MAX_VALUE));
        mekaSuitElytraEnergyUsage = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_ELYTRA.applyToBuilder(builder)
              .defineInRange("elytra", 32_000, 0, Integer.MAX_VALUE));
        mekaSuitEnergyUsagePotionTick = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_POTION.applyToBuilder(builder)
              .defineInRange("energyUsagePotionTick", 40_000, 0, Integer.MAX_VALUE));
        mekaSuitEnergyUsageSprintBoost = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_SPRINT.applyToBuilder(builder)
              .defineInRange("sprintBoost", 100, 0, Integer.MAX_VALUE));
        mekaSuitEnergyUsageGravitationalModulation = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_FLIGHT.applyToBuilder(builder)
              .defineInRange("gravitationalModulation", 1_000, 0, Integer.MAX_VALUE / ModuleGravitationalModulatingUnit.BOOST_ENERGY_MULTIPLIER));
        mekaSuitEnergyUsageVisionEnhancement = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_VISION.applyToBuilder(builder)
              .defineInRange("visionEnhancement", 500, 0, Integer.MAX_VALUE));
        mekaSuitEnergyUsageHydrostaticRepulsion = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_SWIM.applyToBuilder(builder)
              .defineInRange("hydrostaticRepulsion", 500, 0, Integer.MAX_VALUE));
        mekaSuitEnergyUsageNutritionalInjection = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_FOOD.applyToBuilder(builder)
              .defineInRange("nutritionalInjection", 20_000, 0, Integer.MAX_VALUE / FoodConstants.MAX_FOOD));
        mekaSuitEnergyUsageItemAttraction = CachedIntValue.wrap(this, MekanismConfigTranslations.GEAR_MEKA_SUIT_ENERGY_USAGE_MAGNET.applyToBuilder(builder)
              .defineInRange("itemAttraction", 250, 0, Integer.MAX_VALUE));
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
