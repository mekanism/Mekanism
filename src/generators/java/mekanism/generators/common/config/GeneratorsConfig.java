package mekanism.generators.common.config;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.config.IMekanismConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.fml.config.ModConfig.Type;

public class GeneratorsConfig implements IMekanismConfig {

    private static final String TURBINE_CATEGORY = "turbine";
    private static final String WIND_CATEGORY = "wind_generator";
    private static final String HEAT_CATEGORY = "heat_generator";

    private final ForgeConfigSpec configSpec;

    //TODO: Limits on remaining things?
    public final DoubleValue advancedSolarGeneration;
    public final DoubleValue bioGeneration;
    public final DoubleValue heatGeneration;
    public final DoubleValue heatGenerationLava;
    public final DoubleValue heatGenerationNether;
    public final DoubleValue solarGeneration;
    public final ConfigValue<Integer> turbineBladesPerCoil;
    public final ConfigValue<Double> turbineVentGasFlow;
    public final ConfigValue<Double> turbineDisperserGasFlow;
    public final ConfigValue<Integer> condenserRate;
    public final DoubleValue energyPerFusionFuel;
    public final DoubleValue windGenerationMin;
    public final DoubleValue windGenerationMax;
    public final ConfigValue<Integer> windGenerationMinY;
    public final ConfigValue<Integer> windGenerationMaxY;
    public final ConfigValue<List<? extends String>> windGenerationDimBlacklist;

    GeneratorsConfig() {
        Builder builder = new Builder();
        builder.comment("Mekanism Generators Config");

        bioGeneration = builder.comment("Amount of energy in Joules the Bio Generator produces per tick.")
              .defineInRange("bioGeneration", 350, 0, Double.MAX_VALUE);
        energyPerFusionFuel = builder.comment("Affects the Injection Rate, Max Temp, and Ignition Temp.")
              .defineInRange("energyPerFusionFuel", 5E6, 0, Double.MAX_VALUE);
        solarGeneration = builder.comment("Peak output for the Solar Generator. Note: It can go higher than this value in some extreme environments.")
              .defineInRange("solarGeneration", 50, 0, Double.MAX_VALUE);
        advancedSolarGeneration = builder.comment("Peak output for the Advanced Solar Generator. Note: It can go higher than this value in some extreme environments.")
              .defineInRange("advancedSolarGeneration", 300, 0, Double.MAX_VALUE);

        builder.comment("Heat Generator Settings").push(HEAT_CATEGORY);
        heatGeneration = builder.comment("Amount of energy in Joules the Heat Generator produces per tick. (heatGenerationLava * heatGenerationLava) + heatGenerationNether")
              .defineInRange("heatGeneration", 150, 0, Double.MAX_VALUE);
        heatGenerationLava = builder.comment("Multiplier of effectiveness of Lava in the Heat Generator.")
              .defineInRange("heatGenerationLava", 5, 0, Double.MAX_VALUE);
        heatGenerationNether = builder.comment("Add this amount of Joules to the energy produced by a heat generator if it is in the Nether.")
              .defineInRange("heatGenerationNether", 100, 0, Double.MAX_VALUE);
        builder.pop();

        builder.comment("Turbine Settings").push(TURBINE_CATEGORY);
        turbineBladesPerCoil = builder.comment("The number of blades on each turbine coil per blade applied.").define("turbineBladesPerCoil", 4);
        turbineVentGasFlow = builder.comment("The rate at which steam is vented into the turbine.").define("turbineVentGasFlow", 16_000D);
        turbineDisperserGasFlow = builder.comment("The rate at which steam is dispersed into the turbine.").define("turbineDisperserGasFlow", 640D);
        condenserRate = builder.comment("The rate at which steam is condensed in the turbine.").define("condenserRate", 32_000);
        builder.pop();

        builder.comment("Wind Generator Settings").push(WIND_CATEGORY);
        windGenerationMin = builder.comment("Minimum base generation value of the Wind Generator.")
              .defineInRange("windGenerationMin", 60, 0, Double.MAX_VALUE);
        //TODO: Should this be capped by the min generator?
        windGenerationMax = builder.comment("Maximum base generation value of the Wind Generator.")
              .defineInRange("windGenerationMax", 480, 0, Double.MAX_VALUE);
        windGenerationMinY = builder.comment("The minimum Y value that affects the Wind Generators Power generation.").define("windGenerationMinY", 24);
        //TODO: Test this, maybe make default supplier be 255 OR 1 higher than minY
        windGenerationMaxY = builder.comment("The maximum Y value that affects the Wind Generators Power generation.").define("windGenerationMaxY", 255,
              value -> value instanceof Integer && (Integer) value > windGenerationMinY.get());
        windGenerationDimBlacklist = builder.comment("The list of dimension ids that the Wind Generator will not generate power in.")
              .defineList("windGenerationDimBlacklist", new ArrayList<>(), o -> {
                  if (o instanceof String) {
                      String string = (String) o;
                      ResourceLocation dim = new ResourceLocation(string.toLowerCase());
                      DimensionType dimensionType = DimensionType.byName(dim);
                      //byName defaults to overworld if it does not match. So make sure overworld was actually the one specified
                      if (dimensionType == DimensionType.OVERWORLD) {
                          return dim.equals(DimensionType.OVERWORLD.getRegistryName());
                      }
                      return true;
                  }
                  return false;
              });
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