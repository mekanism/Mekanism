package mekanism.generators.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.util.EnumUtils;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.fluids.FluidType;

public class GeneratorsConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final CachedLongValue advancedSolarGeneration;

    public final CachedLongValue bioGeneration;
    public final CachedIntValue bioTankCapacity;

    public final CachedLongValue heatGeneration;
    public final CachedLongValue heatGenerationLava;
    public final CachedLongValue heatGenerationNether;
    public final CachedIntValue heatTankCapacity;
    public final CachedIntValue heatGenerationFluidRate;

    public final CachedLongValue gbgTankCapacity;

    public final CachedLongValue solarGeneration;
    public final CachedIntValue turbineBladesPerCoil;
    public final CachedDoubleValue turbineVentChemicalFlow;
    public final CachedDoubleValue turbineDisperserChemicalFlow;
    public final CachedLongValue turbineEnergyCapacityPerVolume;
    public final CachedLongValue turbineChemicalPerTank;
    public final CachedIntValue condenserRate;

    public final CachedLongValue energyPerFusionFuel;
    public final CachedLongValue windGenerationMin;
    public final CachedLongValue windGenerationMax;
    public final CachedIntValue windGenerationMinY;
    public final CachedIntValue windGenerationMaxY;

    public final CachedLongValue energyPerFissionFuel;
    public final CachedDoubleValue fissionCasingHeatCapacity;
    public final CachedDoubleValue fissionSurfaceAreaTarget;
    public final CachedBooleanValue fissionMeltdownsEnabled;
    public final CachedFloatValue fissionMeltdownRadius;
    public final CachedDoubleValue fissionMeltdownChance;
    public final CachedDoubleValue fissionMeltdownRadiationMultiplier;
    public final CachedDoubleValue fissionPostMeltdownDamage;
    public final CachedDoubleValue defaultBurnRate;
    public final CachedLongValue burnPerAssembly;
    public final CachedLongValue maxFuelPerAssembly;
    public final CachedIntValue fissionCooledCoolantPerTank;
    public final CachedLongValue fissionHeatedCoolantPerTank;
    public final CachedDoubleValue fissionExcessWasteRatio;

    public final CachedLongValue hohlraumMaxGas;
    public final CachedLongValue hohlraumFillRate;

    public final CachedDoubleValue fusionThermocoupleEfficiency;
    public final CachedDoubleValue fusionCasingThermalConductivity;
    public final CachedDoubleValue fusionWaterHeatingRatio;
    public final CachedLongValue fusionFuelCapacity;
    public final CachedLongValue fusionEnergyCapacity;
    public final CachedIntValue fusionWaterPerInjection;
    public final CachedLongValue fusionSteamPerInjection;

    GeneratorsConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        GeneratorsConfigTranslations.SERVER_HOHLRAUM.applyToBuilder(builder).push("hohlraum");
        hohlraumMaxGas = CachedLongValue.wrap(this, GeneratorsConfigTranslations.SERVER_HOHLRAUM_CAPACITY.applyToBuilder(builder)
              .defineInRange("capacity", 10, 1, Long.MAX_VALUE));
        hohlraumFillRate = CachedLongValue.wrap(this, GeneratorsConfigTranslations.SERVER_HOHLRAUM_FILL_RATE.applyToBuilder(builder)
              .defineInRange("fillRate", 1, 1, Long.MAX_VALUE));
        builder.pop();

        GeneratorsConfigTranslations.SERVER_GENERATOR_SOLAR.applyToBuilder(builder).push("solar_generator");
        solarGeneration = CachedLongValue.definePositive(this, builder, GeneratorsConfigTranslations.SERVER_SOLAR_GENERATION, "solarGeneration", 50L);
        advancedSolarGeneration = CachedLongValue.definePositive(this, builder, GeneratorsConfigTranslations.SERVER_SOLAR_GENERATION_ADVANCED, "advancedSolarGeneration", 300L);
        builder.pop();

        GeneratorsConfigTranslations.SERVER_GENERATOR_BIO.applyToBuilder(builder).push("bio_generator");
        bioGeneration = CachedLongValue.definePositive(this, builder, GeneratorsConfigTranslations.SERVER_GENERATOR_BIO_GENERATION,
              "bioGeneration", 350L);
        bioTankCapacity = CachedIntValue.wrap(this, GeneratorsConfigTranslations.SERVER_GENERATOR_BIO_TANK_CAPACITY.applyToBuilder(builder)
              .defineInRange("tankCapacity", 24 * FluidType.BUCKET_VOLUME, 1, Integer.MAX_VALUE));
        builder.pop();

        GeneratorsConfigTranslations.SERVER_GENERATOR_HEAT.applyToBuilder(builder).push("heat_generator");
        heatGeneration = CachedLongValue.definePositive(this, builder, GeneratorsConfigTranslations.SERVER_GENERATOR_HEAT_GENERATION,
              "heatGeneration", 200L);
        heatGenerationLava = CachedLongValue.define(this, builder, GeneratorsConfigTranslations.SERVER_GENERATOR_HEAT_GEN_LAVA,
              "heatGenerationLava", 30L, 0, Long.MAX_VALUE / (EnumUtils.DIRECTIONS.length + 1));
        heatGenerationNether = CachedLongValue.definePositive(this, builder, GeneratorsConfigTranslations.SERVER_GENERATOR_HEAT_GEN_NETHER,
              "heatGenerationNether", 100L);
        heatTankCapacity = CachedIntValue.wrap(this, GeneratorsConfigTranslations.SERVER_GENERATOR_HEAT_TANK_CAPACITY.applyToBuilder(builder)
              .defineInRange("tankCapacity", 24 * FluidType.BUCKET_VOLUME, 1, Integer.MAX_VALUE));
        heatGenerationFluidRate = CachedIntValue.wrap(this, GeneratorsConfigTranslations.SERVER_GENERATOR_HEAT_FLUID_RATE.applyToBuilder(builder)
              .define("heatGenerationFluidRate", 10, value -> value instanceof Integer i && i > 0 && i <= heatTankCapacity.getOrDefault()));
        builder.pop();

        GeneratorsConfigTranslations.SERVER_GENERATOR_GAS.applyToBuilder(builder).push("gas_generator");
        gbgTankCapacity = CachedLongValue.wrap(this, GeneratorsConfigTranslations.SERVER_GENERATOR_GAS_TANK_CAPACITY.applyToBuilder(builder)
              .defineInRange("tankCapacity", 18L * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE));
        builder.pop();

        GeneratorsConfigTranslations.SERVER_GENERATOR_WIND.applyToBuilder(builder).push("wind_generator");
        windGenerationMin = CachedLongValue.definePositive(this, builder, GeneratorsConfigTranslations.SERVER_GENERATOR_WIND_GEN_MIN, "generationMin", 60L);
        //TODO: Should this be capped by the min generator?
        windGenerationMax = CachedLongValue.definePositive(this, builder, GeneratorsConfigTranslations.SERVER_GENERATOR_WIND_GEN_MAX, "generationMax", 480L);
        windGenerationMinY = CachedIntValue.wrap(this, GeneratorsConfigTranslations.SERVER_GENERATOR_WIND_GEN_MIN_Y.applyToBuilder(builder)
              .defineInRange("minY", 24, DimensionType.MIN_Y, DimensionType.MAX_Y - 1));
        //Note: We just require that the maxY is greater than the minY, nothing goes badly if it is set above the max y of the world though
        // as it is just used for range clamping
        windGenerationMaxY = CachedIntValue.wrap(this, GeneratorsConfigTranslations.SERVER_GENERATOR_WIND_GEN_MAX_Y.applyToBuilder(builder)
              .define("maxY", DimensionType.MAX_Y, value -> value instanceof Integer && (Integer) value > windGenerationMinY.getOrDefault()));
        builder.pop();

        GeneratorsConfigTranslations.SERVER_TURBINE.applyToBuilder(builder).push("turbine");
        turbineBladesPerCoil = CachedIntValue.wrap(this, GeneratorsConfigTranslations.SERVER_TURBINE_BLADES.applyToBuilder(builder)
              .defineInRange("bladesPerCoil", 4, 1, 12));
        turbineVentChemicalFlow = CachedDoubleValue.wrap(this, GeneratorsConfigTranslations.SERVER_TURBINE_RATE_VENT.applyToBuilder(builder)
              .defineInRange("ventChemicalFlow", 32D * FluidType.BUCKET_VOLUME, 0.1, 1_024 * FluidType.BUCKET_VOLUME));
        turbineDisperserChemicalFlow = CachedDoubleValue.wrap(this, GeneratorsConfigTranslations.SERVER_TURBINE_RATE_DISPERSER.applyToBuilder(builder)
              .defineInRange("disperserChemicalFlow", 1_280D, 0.1, 1_024 * FluidType.BUCKET_VOLUME));
        condenserRate = CachedIntValue.wrap(this, GeneratorsConfigTranslations.SERVER_TURBINE_RATE_CONDENSER.applyToBuilder(builder)
              .defineInRange("condenserRate", 64 * FluidType.BUCKET_VOLUME, 1, 2_000 * FluidType.BUCKET_VOLUME));
        turbineEnergyCapacityPerVolume = CachedLongValue.define(this, builder, GeneratorsConfigTranslations.SERVER_TURBINE_ENERGY_CAPACITY,
              "energyCapacityPerVolume", 16_000_000L, 1L, 1_000_000_000_000L);
        //Note: We use maxVolume as it still is a large number, and we have no reason to go higher even if some things we technically could
        int maxTurbine = 17 * 17 * 18;
        turbineChemicalPerTank = CachedLongValue.wrap(this, GeneratorsConfigTranslations.SERVER_TURBINE_CHEMICAL_CAPACITY.applyToBuilder(builder)
              .defineInRange("chemicalPerTank", 64L * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE / maxTurbine));
        builder.pop();

        GeneratorsConfigTranslations.SERVER_FISSION.applyToBuilder(builder).push("fission_reactor");
        energyPerFissionFuel = CachedLongValue.definePositive(this, builder, GeneratorsConfigTranslations.SERVER_FISSION_FUEL_ENERGY, "energyPerFissionFuel", 1_000_000L);
        fissionCasingHeatCapacity = CachedDoubleValue.wrap(this, GeneratorsConfigTranslations.SERVER_FISSION__CASING_HEAT_CAPACITY.applyToBuilder(builder)
              .defineInRange("casingHeatCapacity", 1_000D, 1, 1_000_000));
        fissionSurfaceAreaTarget = CachedDoubleValue.wrap(this, GeneratorsConfigTranslations.SERVER_FISSION_SURFACE_AREA.applyToBuilder(builder)
              .defineInRange("surfaceAreaTarget", 4D, 1D, Double.MAX_VALUE));
        defaultBurnRate = CachedDoubleValue.wrap(this, GeneratorsConfigTranslations.SERVER_FISSION_DEFAULT_BURN_RATE.applyToBuilder(builder)
              .defineInRange("defaultBurnRate", 0.1D, 0.001D, 1D));
        burnPerAssembly = CachedLongValue.wrap(this, GeneratorsConfigTranslations.SERVER_FISSION_BURN_PER_ASSEMBLY.applyToBuilder(builder)
              .defineInRange("burnPerAssembly", 1L, 1, 1_000_000));
        maxFuelPerAssembly = CachedLongValue.wrap(this, GeneratorsConfigTranslations.SERVER_FISSION_FUEL_CAPACITY.applyToBuilder(builder)
              .defineInRange("maxFuelPerAssembly", 8L * FluidType.BUCKET_VOLUME, 1, Long.MAX_VALUE / 4_096));
        int maxVolume = 18 * 18 * 18;
        fissionCooledCoolantPerTank = CachedIntValue.wrap(this, GeneratorsConfigTranslations.SERVER_FISSION_COOLED_COOLANT_CAPACITY.applyToBuilder(builder)
              .defineInRange("cooledCoolantPerTank", 100 * FluidType.BUCKET_VOLUME, 1, Integer.MAX_VALUE / maxVolume));
        fissionHeatedCoolantPerTank = CachedLongValue.wrap(this, GeneratorsConfigTranslations.SERVER_FISSION_HEATED_COOLANT_CAPACITY.applyToBuilder(builder)
              .defineInRange("heatedCoolantPerTank", 1_000L * FluidType.BUCKET_VOLUME, 1_000, Long.MAX_VALUE / maxVolume));
        fissionExcessWasteRatio = CachedDoubleValue.wrap(this, GeneratorsConfigTranslations.SERVER_FISSION_EXCESS_WASTE.applyToBuilder(builder)
              .defineInRange("excessWaste", 0.9D, 0.001D, 1D));

        GeneratorsConfigTranslations.SERVER_FISSION_MELTDOWNS.applyToBuilder(builder).push("meltdowns");
        fissionMeltdownsEnabled = CachedBooleanValue.wrap(this, GeneratorsConfigTranslations.SERVER_FISSION_MELTDOWNS_ENABLED.applyToBuilder(builder)
              .define("enabled", true));
        fissionMeltdownRadius = CachedFloatValue.wrap(this, GeneratorsConfigTranslations.SERVER_FISSION_MELTDOWNS_RADIUS.applyToBuilder(builder)
              .defineInRange("radius", 8D, 1, 500));
        fissionMeltdownChance = CachedDoubleValue.wrap(this, GeneratorsConfigTranslations.SERVER_FISSION_MELTDOWNS_CHANCE.applyToBuilder(builder)
              .defineInRange("chance", 0.001D, 0D, 1D));
        fissionMeltdownRadiationMultiplier = CachedDoubleValue.wrap(this, GeneratorsConfigTranslations.SERVER_FISSION_MELTDOWNS_RADIATION_MULTIPLIER.applyToBuilder(builder)
              .defineInRange("radiationMultiplier", 50, 0.1, 1_000_000));
        fissionPostMeltdownDamage = CachedDoubleValue.wrap(this, GeneratorsConfigTranslations.SERVER_FISSION_POST_MELTDOWN_DAMAGE.applyToBuilder(builder)
              .defineInRange("postMeltdownDamage", 0.75 * FissionReactorMultiblockData.MAX_DAMAGE, 0, FissionReactorMultiblockData.MAX_DAMAGE));
        builder.pop();
        builder.pop();

        GeneratorsConfigTranslations.SERVER_FUSION.applyToBuilder(builder).push("fusion_reactor");
        energyPerFusionFuel = CachedLongValue.definePositive(this, builder, GeneratorsConfigTranslations.SERVER_FUSION_FUEL_ENERGY,
              "fuelEnergy", 10_000_000L);
        fusionThermocoupleEfficiency = CachedDoubleValue.wrap(this, GeneratorsConfigTranslations.SERVER_FUSION_THERMOCOUPLE_EFFICIENCY.applyToBuilder(builder)
              .defineInRange("thermocoupleEfficiency", 0.05D, 0D, 1D));
        fusionCasingThermalConductivity = CachedDoubleValue.wrap(this, GeneratorsConfigTranslations.SERVER_FUSION_THERMAL_CONDUCTIVITY.applyToBuilder(builder)
              .defineInRange("casingThermalConductivity", 0.1D, 0.001D, 1D));
        fusionWaterHeatingRatio = CachedDoubleValue.wrap(this, GeneratorsConfigTranslations.SERVER_FUSION_HEATING_RATE.applyToBuilder(builder)
              .defineInRange("waterHeatingRatio", 0.3D, 0D, 1D));
        fusionFuelCapacity = CachedLongValue.wrap(this, GeneratorsConfigTranslations.SERVER_FUSION_FUEL_CAPACITY.applyToBuilder(builder)
              .defineInRange("fuelCapacity", FluidType.BUCKET_VOLUME, 2, 1_000L * FluidType.BUCKET_VOLUME));
        fusionEnergyCapacity = CachedLongValue.define(this, builder, GeneratorsConfigTranslations.SERVER_FUSION_ENERGY_CAPACITY,
              "energyCapacity", 1_000_000_000, 1, Long.MAX_VALUE);
        int baseMaxWater = 1_000 * FluidType.BUCKET_VOLUME;
        fusionWaterPerInjection = CachedIntValue.wrap(this, GeneratorsConfigTranslations.SERVER_FUSION_WATER_INJECTION.applyToBuilder(builder)
              .defineInRange("waterPerInjection", 1_000 * FluidType.BUCKET_VOLUME, 1, Integer.MAX_VALUE / FusionReactorMultiblockData.MAX_INJECTION));
        fusionSteamPerInjection = CachedLongValue.wrap(this, GeneratorsConfigTranslations.SERVER_FUSION_STEAM_INJECTION.applyToBuilder(builder)
              .defineInRange("steamPerInjection", 100L * baseMaxWater, 1, Long.MAX_VALUE / FusionReactorMultiblockData.MAX_INJECTION));
        builder.pop();

        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "generators";
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
