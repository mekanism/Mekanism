package mekanism.generators.common.config;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.MekanismConfig;
import mekanism.generators.common.GeneratorsBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.config.ModConfig.Type;

public class GeneratorsConfig implements IMekanismConfig {

    private static final String ENABLED_CATEGORY = "enabled_generators";
    private static final String TURBINE_CATEGORY = "turbine";
    private static final String WIND_CATEGORY = "wind_generator";
    private static final String HEAT_CATEGORY = "heat_generator";

    private final ForgeConfigSpec configSpec;

    //TODO: Limits
    public final ConfigValue<Float> advancedSolarGeneration;
    public final ConfigValue<Double> bioGeneration;
    public final ConfigValue<Double> heatGeneration;
    public final ConfigValue<Double> heatGenerationLava;
    public final ConfigValue<Double> heatGenerationNether;
    public final ConfigValue<Double> solarGeneration;
    public final ConfigValue<Integer> turbineBladesPerCoil;
    public final ConfigValue<Double> turbineVentGasFlow;
    public final ConfigValue<Double> turbineDisperserGasFlow;
    public final ConfigValue<Integer> condenserRate;
    public final ConfigValue<Double> energyPerFusionFuel;
    public final ConfigValue<Double> windGenerationMin;
    public final ConfigValue<Double> windGenerationMax;
    public final ConfigValue<Integer> windGenerationMinY;
    public final ConfigValue<Integer> windGenerationMaxY;
    public final ConfigValue<List<? extends String>> windGenerationDimBlacklist;

    GeneratorsConfig() {
        Builder builder = new Builder();
        builder.comment("Mekanism Generators Config");

        bioGeneration = builder.comment("Amount of energy in Joules the Bio Generator produces per tick.").define("bioGeneration", 350D);
        energyPerFusionFuel = builder.comment("Affects the Injection Rate, Max Temp, and Ignition Temp.").define("energyPerFusionFuel", 5E6D);
        solarGeneration = builder.comment("Peak output for the Solar Generator. Note: It can go higher than this value in some extreme environments.")
              .define("solarGeneration", 50D);
        advancedSolarGeneration = builder.comment("Peak output for the Advanced Solar Generator. Note: It can go higher than this value in some extreme environments.")
              .define("advancedSolarGeneration", 300F);

        builder.comment("Heat Generator Settings").push(HEAT_CATEGORY);
        heatGeneration = builder.comment("Amount of energy in Joules the Heat Generator produces per tick. (heatGenerationLava * heatGenerationLava) + heatGenerationNether")
              .define("heatGeneration", 150D);
        heatGenerationLava = builder.comment("Multiplier of effectiveness of Lava in the Heat Generator.").define("heatGenerationLava", 5D);
        heatGenerationNether = builder.comment("Add this amount of Joules to the energy produced by a heat generator if it is in the Nether.")
              .define("heatGenerationNether", 100D);
        builder.pop();

        builder.comment("Turbine Settings").push(TURBINE_CATEGORY);
        turbineBladesPerCoil = builder.comment("The number of blades on each turbine coil per blade applied.").define("turbineBladesPerCoil", 4);
        turbineVentGasFlow = builder.comment("The rate at which steam is vented into the turbine.").define("turbineVentGasFlow", 16_000D);
        turbineDisperserGasFlow = builder.comment("The rate at which steam is dispersed into the turbine.").define("turbineDisperserGasFlow", 640D);
        condenserRate = builder.comment("The rate at which steam is condensed in the turbine.").define("condenserRate", 32_000);
        builder.pop();

        builder.comment("Wind Generator Settings").push(WIND_CATEGORY);
        windGenerationMin = builder.comment("Minimum base generation value of the Wind Generator.").define("windGenerationMin", 60D);
        windGenerationMax = builder.comment("Maximum base generation value of the Wind Generator.").define("windGenerationMax", 480D);
        windGenerationMinY = builder.comment("The minimum Y value that affects the Wind Generators Power generation.").define("windGenerationMinY", 24);
        //TODO: Test this, maybe make default supplier be 255 OR 1 higher than minY
        windGenerationMaxY = builder.comment("The maximum Y value that affects the Wind Generators Power generation.").define("windGenerationMaxY", 255,
              value -> value instanceof Integer && (Integer) value > windGenerationMinY.get());
        windGenerationDimBlacklist = builder.comment("The list of dimension ids that the Wind Generator will not generate power in.")
              .defineList("windGenerationDimBlacklist", new ArrayList<>(), o -> {
                  if (o instanceof String) {
                      String string = (String) o;
                      ResourceLocation dim = new ResourceLocation((string).toLowerCase());
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

        builder.comment("Enabled Generators").push(ENABLED_CATEGORY);
        MekanismConfig.addEnabledBlocksCategory(builder, GeneratorsBlock.values());
        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "mekanism-generators.toml";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.COMMON;
    }
}