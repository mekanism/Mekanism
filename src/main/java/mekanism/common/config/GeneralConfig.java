package mekanism.common.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.Mekanism;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedConfigValue;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.config.value.CachedOredictionificatorConfigValue;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FluidTankTier;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.fluids.FluidType;

public class GeneralConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final BooleanSupplier enableAlphaWarning;

    public final CachedBooleanValue logPackets;
    public final CachedBooleanValue allowChunkloading;
    public final CachedIntValue blockDeactivationDelay;
    public final CachedBooleanValue strictUnboxing;
    public final CachedConfigValue<List<? extends String>> cardboardModBlacklist;
    public final CachedBooleanValue transmitterAlloyUpgrade;
    public final CachedIntValue maxUpgradeMultiplier;
    public final CachedDoubleValue boilerWaterConductivity;
    public final CachedDoubleValue heatPerFuelTick;
    public final CachedIntValue fuelwoodTickMultiplier;
    public final CachedDoubleValue resistiveHeaterEfficiency;
    public final CachedDoubleValue superheatingHeatTransfer;
    public final CachedIntValue maxSolarNeutronActivatorRate;
    public final CachedIntValue fluidItemFillRate;
    public final CachedLongValue chemicalItemFillRate;
    //Auto eject
    public final CachedIntValue fluidAutoEjectRate;
    public final CachedLongValue chemicalAutoEjectRate;
    public final CachedDoubleValue dumpExcessKeepRatio;
    //Dynamic Tank
    public final CachedIntValue dynamicTankFluidPerTank;
    public final CachedLongValue dynamicTankChemicalPerTank;
    //Prefilled
    public final CachedBooleanValue prefilledFluidTanks;
    public final CachedBooleanValue prefilledChemicalTanks;
    //Energy Conversion
    public final CachedBooleanValue blacklistForge;
    public final CachedDoubleValue forgeConversionRate;
    public final CachedBooleanValue blacklistFluxNetworks;
    public final CachedBooleanValue blacklistGrandPower;
    public final CachedLongValue maxEnergyPerSteam;
    //Radiation
    public final CachedBooleanValue radiationEnabled;
    public final CachedIntValue radiationChunkCheckRadius;
    public final CachedDoubleValue radiationSourceDecayRate;
    public final CachedDoubleValue radiationTargetDecayRate;
    public final CachedDoubleValue radiationNegativeEffectsMinSeverity;
    public final CachedLongValue radioactiveWasteBarrelMaxChemical;
    public final CachedIntValue radioactiveWasteBarrelProcessTicks;
    public final CachedLongValue radioactiveWasteBarrelDecayAmount;
    //Digital Miner
    public final CachedIntValue minerSilkMultiplier;
    public final CachedIntValue minerMaxRadius;
    public final CachedIntValue minerTicksPerMine;
    public final CachedBooleanValue easyMinerFilters;
    //Laser
    public final CachedBooleanValue aestheticWorldDamage;
    public final CachedIntValue laserRange;
    public final CachedLongValue laserEnergyPerHardness;
    public final CachedLongValue laserEnergyPerDamage;
    //Oredictionificator
    public final CachedOredictionificatorConfigValue validOredictionificatorFilters;
    //Pump
    public final CachedIntValue maxPumpRange;
    public final CachedBooleanValue pumpInfiniteFluidSources;
    public final CachedIntValue pumpHeavyWaterAmount;
    public final CachedIntValue maxPlenisherNodes;
    //Quantum Entangloporter
    public final CachedLongValue entangloporterEnergyBuffer;
    public final CachedIntValue entangloporterFluidBuffer;
    public final CachedLongValue entangloporterChemicalBuffer;
    //Security
    public final CachedBooleanValue allowProtection;
    public final CachedBooleanValue opsBypassRestrictions;
    //Nutritional Paste
    public final CachedFloatValue nutritionalPasteSaturation;
    public final CachedIntValue nutritionalPasteMBPerFood;
    //Boiler
    public final CachedIntValue boilerWaterPerTank;
    public final CachedLongValue boilerSteamPerTank;
    public final CachedLongValue boilerHeatedCoolantPerTank;
    public final CachedLongValue boilerCooledCoolantPerTank;
    //Thermal Evaporation Tower
    public final CachedDoubleValue evaporationHeatDissipation;
    public final CachedDoubleValue evaporationTempMultiplier;
    public final CachedDoubleValue evaporationSolarMultiplier;
    public final CachedDoubleValue evaporationHeatCapacity;
    public final CachedIntValue evaporationFluidPerTank;
    public final CachedIntValue evaporationOutputTankCapacity;
    //SPS
    public final CachedIntValue spsInputPerAntimatter;
    public final CachedLongValue spsOutputTankCapacity;
    public final CachedLongValue spsEnergyPerInput;

    GeneralConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        //Note: We only enable this config option in dev mode
        if (FMLEnvironment.production) {
            enableAlphaWarning = ConstantPredicates.ALWAYS_TRUE;
        } else {
            enableAlphaWarning = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_ALPHA_WARNING.applyToBuilder(builder).define("alphaWarning", true));
        }

        logPackets = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_PACKET_LOGGING.applyToBuilder(builder)
              .define("logPackets", false));
        allowChunkloading = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_CHUNKLOADING.applyToBuilder(builder)
              .define("allowChunkloading", true));
        blockDeactivationDelay = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_DEACTIVATION_DELAY.applyToBuilder(builder)
              .defineInRange("blockDeactivationDelay", 3 * SharedConstants.TICKS_PER_SECOND, 0, SharedConstants.TICKS_PER_MINUTE));
        aestheticWorldDamage = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_AESTHETIC_DAMAGE.applyToBuilder(builder)
              .define("aestheticWorldDamage", true));
        transmitterAlloyUpgrade = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_ALLOY_UPGRADING.applyToBuilder(builder)
              .define("transmitterAlloyUpgrade", true));
        //If this is less than 1, upgrades make machines worse. If less than 0, I don't even know.
        maxUpgradeMultiplier = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_UPGRADE_MULTIPLIER.applyToBuilder(builder)
              .defineInRange("maxUpgradeMultiplier", 10, 1, Integer.MAX_VALUE));
        maxSolarNeutronActivatorRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_RATE_SNA.applyToBuilder(builder)
              .defineInRange("maxSolarNeutronActivatorRate", 64, 1, 1_024));

        MekanismConfigTranslations.GENERAL_HEATER.applyToBuilder(builder).push("heater");
        heatPerFuelTick = CachedDoubleValue.wrap(this, MekanismConfigTranslations.GENERAL_FUELWOOD_HEAT.applyToBuilder(builder)
              .defineInRange("heatPerFuelTick", 400, 0.1, 4_000_000));
        fuelwoodTickMultiplier = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_FUELWOOD_DURATION.applyToBuilder(builder)
              .defineInRange("fuelwoodTickMultiplier", 1, 1, 1_000));
        resistiveHeaterEfficiency = CachedDoubleValue.wrap(this, MekanismConfigTranslations.GENERAL_RESISTIVE_EFFICIENCY.applyToBuilder(builder)
              .defineInRange("resistiveEfficiency", 0.6, 0, 1));
        builder.pop();

        MekanismConfigTranslations.GENERAL_CARDBOARD.applyToBuilder(builder).push("cardboard_box");
        strictUnboxing = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_CARDBOARD_STRICT_UNBOXING.applyToBuilder(builder)
              .define("strictUnboxing", false));
        cardboardModBlacklist = CachedConfigValue.wrap(this, MekanismConfigTranslations.GENERAL_CARDBOARD_MOD_BLACKLIST.applyToBuilder(builder)
              .defineListAllowEmpty("modBlacklist", ArrayList::new, () -> Mekanism.MODID, e -> e instanceof String modid && ResourceLocation.isValidNamespace(modid)));
        builder.pop();

        MekanismConfigTranslations.GENERAL_FILL_RATE.applyToBuilder(builder).push("item_fill_rate");
        fluidItemFillRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_FILL_RATE_FLUID.applyToBuilder(builder)
              .defineInRange("fluid", 1_024, 1, Integer.MAX_VALUE));
        chemicalItemFillRate = CachedLongValue.wrap(this, MekanismConfigTranslations.GENERAL_FILL_RATE_CHEMICAL.applyToBuilder(builder)
              .defineInRange("chemical", 1_024, 1, Long.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GENERAL_DYNAMIC_TANK.applyToBuilder(builder).push("dynamic_tank");
        int maxVolume = 18 * 18 * 18;
        dynamicTankFluidPerTank = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_DYNAMIC_TANK_FLUID_CAPACITY.applyToBuilder(builder)
              .defineInRange("fluidPerTank", 350 * FluidType.BUCKET_VOLUME, 1, Integer.MAX_VALUE / maxVolume));
        dynamicTankChemicalPerTank = CachedLongValue.wrap(this, MekanismConfigTranslations.GENERAL_DYNAMIC_TANK_CHEMICAL_CAPACITY.applyToBuilder(builder)
              .defineInRange("chemicalPerTank", 16_000L * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE / maxVolume));
        builder.pop();

        MekanismConfigTranslations.GENERAL_AUTO_EJECT.applyToBuilder(builder).push("auto_eject");
        fluidAutoEjectRate = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_AUTO_EJECT_RATE_FLUID.applyToBuilder(builder)
              .defineInRange("fluid", 1_024, 1, Integer.MAX_VALUE));
        chemicalAutoEjectRate = CachedLongValue.wrap(this, MekanismConfigTranslations.GENERAL_AUTO_EJECT_RATE_CHEMICAL.applyToBuilder(builder)
              .defineInRange("chemical", 1_024L, 1, Long.MAX_VALUE));
        dumpExcessKeepRatio = CachedDoubleValue.wrap(this, MekanismConfigTranslations.GENERAL_AUTO_EJECT_EXCESS.applyToBuilder(builder)
              .defineInRange("dumpExcessKeepRatio", 0.85D, 0.001D, 1D));
        builder.pop();

        MekanismConfigTranslations.GENERAL_PREFILLED_TANKS.applyToBuilder(builder).push("prefilled");
        prefilledFluidTanks = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_PREFILLED_TANKS_FLUID.applyToBuilder(builder)
              .define("fluidTanks", true));
        prefilledChemicalTanks = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_PREFILLED_TANKS_CHEMICAL.applyToBuilder(builder)
              .define("chemicalTanks", true));
        builder.pop();

        MekanismConfigTranslations.GENERAL_ENERGY_CONVERSION.applyToBuilder(builder).push("energy_conversion");
        blacklistForge = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_ENERGY_CONVERSION_BLACKLIST_FE.applyToBuilder(builder)
              .worldRestart()
              .define("blacklistForge", false));
        forgeConversionRate = CachedDoubleValue.wrap(this, MekanismConfigTranslations.GENERAL_ENERGY_CONVERSION_FE.applyToBuilder(builder)
              .defineInRange("feConversionRate", 2.5, 0.0001, 10_000 /* Inverse of min positive value */));
        blacklistFluxNetworks = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_ENERGY_CONVERSION_BLACKLIST_FN.applyToBuilder(builder)
              .worldRestart()
              .define("blacklistFluxNetworks", false));
        blacklistGrandPower = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_ENERGY_CONVERSION_BLACKLIST_GP.applyToBuilder(builder)
              .worldRestart()
              .define("blacklistGrandPower", false));
        maxEnergyPerSteam = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GENERAL_ENERGY_CONVERSION_STEAM, "maxEnergyPerSteam", 10);
        builder.pop();

        MekanismConfigTranslations.GENERAL_RADIATION.applyToBuilder(builder).push("radiation");
        radiationEnabled = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_RADIATION_ENABLED.applyToBuilder(builder)
              .define("enabled", true));
        radiationChunkCheckRadius = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_RADIATION_CHUNK_RADIUS.applyToBuilder(builder)
              .defineInRange("chunkCheckRadius", 5, 1, 100));
        radiationSourceDecayRate = CachedDoubleValue.wrap(this, MekanismConfigTranslations.GENERAL_RADIATION_DECAY_RATE_SOURCE.applyToBuilder(builder)
              .defineInRange("sourceDecayRate", 0.9995D, 0, 1));
        radiationTargetDecayRate = CachedDoubleValue.wrap(this, MekanismConfigTranslations.GENERAL_RADIATION_DECAY_RATE_TARGET.applyToBuilder(builder)
              .defineInRange("targetDecayRate", 0.9995D, 0, 1));
        radiationNegativeEffectsMinSeverity = CachedDoubleValue.wrap(this, MekanismConfigTranslations.GENERAL_RADIATION_MIN_SEVERITY.applyToBuilder(builder)
              .defineInRange("negativeEffectsMinSeverity", 0.1D, 0, 1));
        radioactiveWasteBarrelMaxChemical = CachedLongValue.wrap(this, MekanismConfigTranslations.GENERAL_RADIATION_BARREL_CAPACITY.applyToBuilder(builder)
              .defineInRange("wasteBarrelCapacity", 512L * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        radioactiveWasteBarrelProcessTicks = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_RADIATION_BARREL_DECAY_FREQUENCY.applyToBuilder(builder)
              .defineInRange("wasteBarrelProcessTicks", SharedConstants.TICKS_PER_SECOND, 1, Integer.MAX_VALUE));
        radioactiveWasteBarrelDecayAmount = CachedLongValue.wrap(this, MekanismConfigTranslations.GENERAL_RADIATION_BARREL_DECAY_AMOUNT.applyToBuilder(builder)
              .defineInRange("wasteBarrelDecayAmount", 1, 0, Long.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GENERAL_MINER.applyToBuilder(builder).push("digital_miner");
        minerSilkMultiplier = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_MINER_SILK_MULTIPLIER.applyToBuilder(builder)
              .defineInRange("silkMultiplier", 12, 1, Integer.MAX_VALUE));
        minerMaxRadius = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_MINER_MAX_RADIUS.applyToBuilder(builder)
              .defineInRange("maxRadius", 32, 1, Integer.MAX_VALUE));
        minerTicksPerMine = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_MINER_TICK_RATE.applyToBuilder(builder)
              .defineInRange("ticksPerMine", 80, 1, Integer.MAX_VALUE));
        easyMinerFilters = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_MINER_EASY_FILTERS.applyToBuilder(builder)
              .define("easyMinerFilters", false));
        builder.pop();

        MekanismConfigTranslations.GENERAL_LASER.applyToBuilder(builder).push("laser");
        laserRange = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_LASER_RANGE.applyToBuilder(builder)
              .defineInRange("range", 64, 1, 1_024));
        laserEnergyPerHardness = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GENERAL_LASER_ENERGY_HARDNESS, "energyPerHardness", 100_000);
        laserEnergyPerDamage = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.GENERAL_LASER_ENERGY_DAMAGE, "energyPerDamage", 2_500, 1);
        builder.pop();

        MekanismConfigTranslations.GENERAL_OREDICTIONIFICATOR.applyToBuilder(builder).push("oredictionificator");
        validOredictionificatorFilters = CachedOredictionificatorConfigValue.define(this, MekanismConfigTranslations.GENERAL_OREDICTIONIFICATOR_VALID_ITEMS.applyToBuilder(builder),
              "validItemFilters", () -> Collections.singletonMap("c", List.of("ingots/", "ores/", "dusts/", "nuggets/", "storage_blocks/", "raw_materials/")));
        builder.pop();

        MekanismConfigTranslations.GENERAL_PUMP.applyToBuilder(builder).push("pump");
        maxPumpRange = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_PUMP_RANGE.applyToBuilder(builder)
              .defineInRange("range", 80, 1, 512));
        pumpInfiniteFluidSources = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_PUMP_INFINITE_FLUIDS.applyToBuilder(builder)
              .define("pumpInfiniteFluidSources", false));
        pumpHeavyWaterAmount = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_PUMP_HEAVY_WATER.applyToBuilder(builder)
              .defineInRange("heavyWaterAmount", FluidType.BUCKET_VOLUME / 100, 1, FluidType.BUCKET_VOLUME));
        maxPlenisherNodes = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_PUMP_PLENISHER_MAX_NODES.applyToBuilder(builder)
              .defineInRange("maxPlenisherNodes", 4_000, 1, 1_000_000));
        builder.pop();

        MekanismConfigTranslations.GENERAL_QE.applyToBuilder(builder).push("quantum_entangloporter");
        entangloporterEnergyBuffer = CachedLongValue.wrap(this, MekanismConfigTranslations.GENERAL_QE_BUFFER_ENERGY.applyToBuilder(builder)
              .worldRestart()
              .defineInRange("energyBuffer", EnergyCubeTier.ULTIMATE.getBaseMaxEnergy(), 1, Long.MAX_VALUE));
        entangloporterFluidBuffer = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_QE_BUFFER_FLUID.applyToBuilder(builder)
              .worldRestart()
              .defineInRange("fluidBuffer", FluidTankTier.ULTIMATE.getBaseStorage(), 1, Integer.MAX_VALUE));
        entangloporterChemicalBuffer = CachedLongValue.wrap(this, MekanismConfigTranslations.GENERAL_QE_BUFFER_CHEMICAL.applyToBuilder(builder)
              .worldRestart()
              .defineInRange("chemicalBuffer", ChemicalTankTier.ULTIMATE.getBaseStorage(), 1, Long.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GENERAL_SECURITY.applyToBuilder(builder).push("security");
        allowProtection = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_SECURITY_ENABLED.applyToBuilder(builder)
              .define("allowProtection", true));
        opsBypassRestrictions = CachedBooleanValue.wrap(this, MekanismConfigTranslations.GENERAL_SECURITY_OPS_BYPASS.applyToBuilder(builder)
              .define("opsBypassRestrictions", false));
        builder.pop();

        MekanismConfigTranslations.GENERAL_PASTE.applyToBuilder(builder).push("nutritional_paste");
        nutritionalPasteSaturation = CachedFloatValue.wrap(this, MekanismConfigTranslations.GENERAL_PASTE_SATURATION.applyToBuilder(builder)
              .defineInRange("saturation", 0.8, 0, 100));
        nutritionalPasteMBPerFood = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_PASTE_PER_FOOD.applyToBuilder(builder)
              .defineInRange("mbPerFood", 50, 1, Integer.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GENERAL_BOILER.applyToBuilder(builder).push("boiler");
        //Note: We use maxVolume as it still is a large number, and we have no reason to go higher even if some things we technically could
        boilerWaterPerTank = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_BOILER_CAPACITY_WATER.applyToBuilder(builder)
              .defineInRange("waterPerTank", 16 * FluidType.BUCKET_VOLUME, 1, Integer.MAX_VALUE / maxVolume));
        boilerSteamPerTank = CachedLongValue.wrap(this, MekanismConfigTranslations.GENERAL_BOILER_CAPACITY_STEAM.applyToBuilder(builder)
              .defineInRange("steamPerTank", 160L * FluidType.BUCKET_VOLUME, 10, Long.MAX_VALUE / maxVolume));
        boilerHeatedCoolantPerTank = CachedLongValue.wrap(this, MekanismConfigTranslations.GENERAL_BOILER_CAPACITY_HEATED_COOLANT.applyToBuilder(builder)
              .defineInRange("heatedCoolantPerTank", 256L * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE / maxVolume));
        boilerCooledCoolantPerTank = CachedLongValue.wrap(this, MekanismConfigTranslations.GENERAL_BOILER_CAPACITY_COOLED_COOLANT.applyToBuilder(builder)
              .defineInRange("cooledCoolantPerTank", 256L * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE / maxVolume));
        boilerWaterConductivity = CachedDoubleValue.wrap(this, MekanismConfigTranslations.GENERAL_BOILER_WATER_CONDUCTIVITY.applyToBuilder(builder)
              .defineInRange("waterConductivity", 0.7, 0.01, 1));
        superheatingHeatTransfer = CachedDoubleValue.wrap(this, MekanismConfigTranslations.GENERAL_BOILER_HEAT_TRANSFER.applyToBuilder(builder)
              .defineInRange("superheatingHeatTransfer", 16_000_000, 0.1, 1_024_000_000));
        builder.pop();

        MekanismConfigTranslations.GENERAL_TEP.applyToBuilder(builder).push("thermal_evaporation");
        evaporationHeatDissipation = CachedDoubleValue.wrap(this, MekanismConfigTranslations.GENERAL_TEP_HEAT_LOSS.applyToBuilder(builder)
              .defineInRange("heatDissipation", 0.02, 0.001, 1_000));
        evaporationSolarMultiplier = CachedDoubleValue.wrap(this, MekanismConfigTranslations.GENERAL_TEP_HEAT_SOLAR.applyToBuilder(builder)
              .defineInRange("solarMultiplier", 0.2, 0.001, 1_000_000));
        evaporationTempMultiplier = CachedDoubleValue.wrap(this, MekanismConfigTranslations.GENERAL_TEP_TEMP_MULT.applyToBuilder(builder)
              .defineInRange("tempMultiplier", 0.4, 0.001, 1_000_000));
        evaporationHeatCapacity = CachedDoubleValue.wrap(this, MekanismConfigTranslations.GENERAL_TEP_CAPACITY_HEAT.applyToBuilder(builder)
              .defineInRange("heatCapacity", 100D, 1, 1_000_000));
        evaporationFluidPerTank = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_TEP_CAPACITY_INPUT.applyToBuilder(builder)
              .defineInRange("fluidPerTank", 64 * FluidType.BUCKET_VOLUME, 1, Integer.MAX_VALUE / (EvaporationMultiblockData.MAX_HEIGHT * 4)));
        evaporationOutputTankCapacity = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_TEP_CAPACITY_OUTPUT.applyToBuilder(builder)
              .defineInRange("outputTankCapacity", 10 * FluidType.BUCKET_VOLUME, 1, Integer.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GENERAL_SPS.applyToBuilder(builder).push("sps");
        spsInputPerAntimatter = CachedIntValue.wrap(this, MekanismConfigTranslations.GENERAL_SPS_ANTIMATTER_COST.applyToBuilder(builder)
              .defineInRange("inputPerAntimatter", FluidType.BUCKET_VOLUME, 1, Integer.MAX_VALUE));
        spsOutputTankCapacity = CachedLongValue.wrap(this, MekanismConfigTranslations.GENERAL_SPS_CAPACITY_OUTPUT.applyToBuilder(builder)
              .defineInRange("outputTankCapacity", FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        spsEnergyPerInput = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.GENERAL_SPS_ENERGY_PER, "energyPerInput", 1_000_000);
        builder.pop();

        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "general";
    }


    @Override
    public String getTranslation() {
        return "General Config";
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
