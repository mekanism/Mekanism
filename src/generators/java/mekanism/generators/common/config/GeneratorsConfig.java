package mekanism.generators.common.config;

import java.util.ArrayList;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedResourceLocationListValue;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class GeneratorsConfig extends BaseMekanismConfig {

    private static final String TURBINE_CATEGORY = "turbine";
    private static final String WIND_CATEGORY = "wind_generator";
    private static final String HEAT_CATEGORY = "heat_generator";

    private final ForgeConfigSpec configSpec;

    //TODO: Limits on remaining things?
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

    GeneratorsConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Generators Config. This config is synced between server and client.").push("generators");

        bioGeneration = CachedFloatingLongValue.define(this, builder, "Amount of energy in Joules the Bio Generator produces per tick.",
              "bioGeneration", FloatingLong.createConst(350));
        energyPerFusionFuel = CachedFloatingLongValue.define(this, builder, "Affects the Injection Rate, Max Temp, and Ignition Temp.",
              "energyPerFusionFuel", FloatingLong.createConst(5_000_000));
        solarGeneration = CachedFloatingLongValue.define(this, builder, "Peak output for the Solar Generator. Note: It can go higher than this value in some extreme environments.",
              "solarGeneration", FloatingLong.createConst(50));
        advancedSolarGeneration = CachedFloatingLongValue.define(this, builder, "Peak output for the Advanced Solar Generator. Note: It can go higher than this value in some extreme environments.",
              "advancedSolarGeneration", FloatingLong.createConst(300));

        builder.comment("Heat Generator Settings").push(HEAT_CATEGORY);
        heatGeneration = CachedFloatingLongValue.define(this, builder, "Amount of energy in Joules the Heat Generator produces per tick. (heatGenerationLava * heatGenerationLava) + heatGenerationNether",
              "heatGeneration", FloatingLong.createConst(150));
        heatGenerationLava = CachedFloatingLongValue.define(this, builder, "Multiplier of effectiveness of Lava in the Heat Generator.",
              "heatGenerationLava", FloatingLong.createConst(15));
        heatGenerationNether = CachedFloatingLongValue.define(this, builder, "Add this amount of Joules to the energy produced by a heat generator if it is in the Nether.",
              "heatGenerationNether", FloatingLong.createConst(100));
        builder.pop();

        builder.comment("Turbine Settings").push(TURBINE_CATEGORY);
        turbineBladesPerCoil = CachedIntValue.wrap(this, builder.comment("The number of blades on each turbine coil per blade applied.")
              .define("turbineBladesPerCoil", 4));
        turbineVentGasFlow = CachedDoubleValue.wrap(this, builder.comment("The rate at which steam is vented into the turbine.")
              .define("turbineVentGasFlow", 16_000D));
        turbineDisperserGasFlow = CachedDoubleValue.wrap(this, builder.comment("The rate at which steam is dispersed into the turbine.")
              .define("turbineDisperserGasFlow", 640D));
        condenserRate = CachedIntValue.wrap(this, builder.comment("The rate at which steam is condensed in the turbine.")
              .define("condenserRate", 32_000));
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
        windGenerationMaxY = CachedIntValue.wrap(this, builder.comment("The maximum Y value that affects the Wind Generators Power generation.")
              .define("windGenerationMaxY", 255,
                    value -> value instanceof Integer && (Integer) value > windGenerationMinY.get()));
        windGenerationDimBlacklist = CachedResourceLocationListValue.wrap(this, builder.comment("The list of dimension ids that the Wind Generator will not generate power in.")
              .defineList("windGenerationDimBlacklist", new ArrayList<>(), o -> {
                  if (o instanceof String) {
                      String string = ((String) o).toLowerCase();
                      if (ResourceLocation.isResouceNameValid(string)) {
                          ResourceLocation dim = new ResourceLocation(string);
                          DimensionType dimensionType = DimensionType.byName(dim);
                          //byName defaults to overworld if it does not match. So make sure overworld was actually the one specified
                          if (dimensionType == DimensionType.OVERWORLD) {
                              return dim.equals(DimensionType.OVERWORLD.getRegistryName());
                          }
                          return true;
                      }
                  }
                  return false;
              }));
        builder.pop(2);
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