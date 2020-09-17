package mekanism.generators.common.config;

import java.util.ArrayList;
import java.util.Locale;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.config.value.CachedResourceLocationListValue;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class GeneratorsConfig extends BaseMekanismConfig {

    private static final String TURBINE_CATEGORY = "turbine";
    private static final String WIND_CATEGORY = "wind_generator";
    private static final String HEAT_CATEGORY = "heat_generator";
    private static final String HOHLRAUM_CATEGORY = "hohlraum";
    private static final String FISSION_CATEGORY = "fission_reactor";

    private final ForgeConfigSpec configSpec;

    public final CachedFloatingLongValue advancedSolarGeneration;
    public final CachedFloatingLongValue bioGeneration;
    public final CachedFloatingLongValue heatGeneration;
    public final CachedFloatingLongValue heatGenerationLava;
    public final CachedFloatingLongValue heatGenerationNether;
    public final CachedFloatingLongValue solarGeneration;
    public final CachedIntValue turbineBladesPerCoil;
    public final CachedDoubleValue turbineVentGasFlow;
    public final CachedDoubleValue turbineDisperserGasFlow;
    public final CachedIntValue condenserRate;
    public final CachedFloatingLongValue energyPerFusionFuel;
    public final CachedFloatingLongValue windGenerationMin;
    public final CachedFloatingLongValue windGenerationMax;
    public final CachedIntValue windGenerationMinY;
    public final CachedIntValue windGenerationMaxY;
    public final CachedResourceLocationListValue windGenerationDimBlacklist;

    public final CachedFloatingLongValue energyPerFissionFuel;
    public final CachedDoubleValue fissionCasingHeatCapacity;
    public final CachedDoubleValue fissionSurfaceAreaTarget;
    public final CachedBooleanValue fissionMeltdownsEnabled;
    public final CachedDoubleValue fissionMeltdownChance;
    public final CachedDoubleValue fissionMeltdownRadiationMultiplier;

    public final CachedLongValue hohlraumMaxGas;
    public final CachedLongValue hohlraumFillRate;

    GeneratorsConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Generators Config. This config is synced between server and client.").push("generators");

        bioGeneration = CachedFloatingLongValue.define(this, builder, "Amount of energy in Joules the Bio Generator produces per tick.",
              "bioGeneration", FloatingLong.createConst(350));
        energyPerFusionFuel = CachedFloatingLongValue.define(this, builder, "Affects the Injection Rate, Max Temp, and Ignition Temp.",
              "energyPerFusionFuel", FloatingLong.createConst(10_000_000));
        solarGeneration = CachedFloatingLongValue.define(this, builder, "Peak output for the Solar Generator. Note: It can go higher than this value in some extreme environments.",
              "solarGeneration", FloatingLong.createConst(50));
        advancedSolarGeneration = CachedFloatingLongValue.define(this, builder, "Peak output for the Advanced Solar Generator. Note: It can go higher than this value in some extreme environments.",
              "advancedSolarGeneration", FloatingLong.createConst(300));

        builder.comment("Heat Generator Settings").push(HEAT_CATEGORY);
        heatGeneration = CachedFloatingLongValue.define(this, builder, "Amount of energy in Joules the Heat Generator produces per tick. (heatGenerationLava * heatGenerationLava) + heatGenerationNether",
              "heatGeneration", FloatingLong.createConst(200));
        heatGenerationLava = CachedFloatingLongValue.define(this, builder, "Multiplier of effectiveness of Lava in the Heat Generator.",
              "heatGenerationLava", FloatingLong.createConst(30));
        heatGenerationNether = CachedFloatingLongValue.define(this, builder, "Add this amount of Joules to the energy produced by a heat generator if it is in an 'ultrawarm' dimension, in vanilla this is just the Nether.",
              "heatGenerationNether", FloatingLong.createConst(100));
        builder.pop();

        builder.comment("Turbine Settings").push(TURBINE_CATEGORY);
        turbineBladesPerCoil = CachedIntValue.wrap(this, builder.comment("The number of blades on each turbine coil per blade applied.")
              .define("turbineBladesPerCoil", 4));
        turbineVentGasFlow = CachedDoubleValue.wrap(this, builder.comment("The rate at which steam is vented into the turbine.")
              .define("turbineVentGasFlow", 32_000D));
        turbineDisperserGasFlow = CachedDoubleValue.wrap(this, builder.comment("The rate at which steam is dispersed into the turbine.")
              .define("turbineDisperserGasFlow", 1_280D));
        condenserRate = CachedIntValue.wrap(this, builder.comment("The rate at which steam is condensed in the turbine.")
              .define("condenserRate", 64_000));
        builder.pop();

        builder.comment("Wind Generator Settings").push(WIND_CATEGORY);
        windGenerationMin = CachedFloatingLongValue.define(this, builder, "Minimum base generation value of the Wind Generator.",
              "windGenerationMin", FloatingLong.createConst(60));
        //TODO: Should this be capped by the min generator?
        windGenerationMax = CachedFloatingLongValue.define(this, builder, "Maximum base generation value of the Wind Generator.",
              "windGenerationMax", FloatingLong.createConst(480));
        windGenerationMinY = CachedIntValue.wrap(this, builder.comment("The minimum Y value that affects the Wind Generators Power generation.")
              .define("windGenerationMinY", 24));
        //TODO: Test this, maybe make default supplier be 255 OR 1 higher than minY
        //TODO: Also see if we can somehow check world.getHeight()
        windGenerationMaxY = CachedIntValue.wrap(this, builder.comment("The maximum Y value that affects the Wind Generators Power generation.")
              .define("windGenerationMaxY", 255, value -> value instanceof Integer && (Integer) value > windGenerationMinY.get()));
        //Note: Unlike in 1.15 we don't verify the dimension exists as dimensions are a lot more dynamic now
        windGenerationDimBlacklist = CachedResourceLocationListValue.wrap(this, builder.comment("The list of dimension ids that the Wind Generator will not generate power in.")
              .defineList("windGenerationDimBlacklist", new ArrayList<>(), o -> o instanceof String && ResourceLocation.tryCreate(((String) o).toLowerCase(Locale.ROOT)) != null));
        builder.pop();

        builder.comment("Hohlraum Settings").push(HOHLRAUM_CATEGORY);
        hohlraumMaxGas = CachedLongValue.wrap(this, builder.comment("Hohlraum capacity in mB.")
              .defineInRange("maxGas", 10, 1, Long.MAX_VALUE));
        hohlraumFillRate = CachedLongValue.wrap(this, builder.comment("Amount of DT-Fuel Hohlraum can accept per tick.")
              .defineInRange("fillRate", 1, 1, Long.MAX_VALUE));
        builder.pop();

        builder.comment("Fission Reactor Settings").push(FISSION_CATEGORY);
        energyPerFissionFuel = CachedFloatingLongValue.define(this, builder, "Amount of energy created (in heat) from each whole mB of fission fuel.",
              "energyPerFissionFuel", FloatingLong.createConst(1_000_000));
        fissionCasingHeatCapacity = CachedDoubleValue.wrap(this, builder.comment("The heat capacity added to a Fission Reactor by a single casing block. Increase to require more energy to raise the reactor temperature.")
              .define("casingHeatCapacity", 1_000D));
        fissionSurfaceAreaTarget = CachedDoubleValue.wrap(this, builder.comment("The average surface area of a Fission Reactor's fuel assemblies to reach 100% boil efficiency. Higher values make it harder to cool the reactor.")
              .defineInRange("surfaceAreaTarget", 4D, 1D, Double.MAX_VALUE));
        fissionMeltdownsEnabled = CachedBooleanValue.wrap(this, builder.comment("Whether catastrophic meltdowns can occur from Fission Reactors.")
              .define("meltdownsEnabled", true));
        fissionMeltdownChance = CachedDoubleValue.wrap(this, builder.comment("The chance of a meltdown occurring once damage passes 100%. Will linearly scale as damage continues increasing.")
              .defineInRange("meltdownChance", 0.001D, 0D, 1D));
        fissionMeltdownRadiationMultiplier = CachedDoubleValue.wrap(this, builder.comment("How much radioactivity of fuel/waste contents are multiplied during a meltdown.")
              .define("meltdownRadiationMultiplier", 50D));
        builder.pop();

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "generators";
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