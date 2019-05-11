package mekanism.common.config;

import io.netty.buffer.ByteBuf;
import mekanism.common.config.options.DoubleOption;
import mekanism.common.config.options.IntOption;
import mekanism.common.config.options.IntSetOption;
import mekanism.generators.common.block.states.BlockStateGenerator;
import net.minecraftforge.common.config.Configuration;

/**
 * Created by Thiakil on 15/03/2019.
 */
public class GeneratorsConfig extends BaseConfig {

    public final DoubleOption advancedSolarGeneration = new DoubleOption(this, "generation", "AdvancedSolarGeneration", 300D,
          "Peak output for the Advanced Solar Generator. Note: It can go higher than this value in some extreme environments.");

    public final DoubleOption bioGeneration = new DoubleOption(this, "generation", "BioGeneration", 350D,
          "Amount of energy in Joules the Bio Generator produces per tick.");

    public final DoubleOption heatGeneration = new DoubleOption(this, "generation", "HeatGeneration", 150D,
          "Amount of energy in Joules the Heat Generator produces per tick. (heatGenerationLava * heatGenerationLava) + heatGenerationNether");

    public final DoubleOption heatGenerationLava = new DoubleOption(this, "generation", "HeatGenerationLava", 5D,
          "Multiplier of effectiveness of Lava in the Heat Generator.");

    public final DoubleOption heatGenerationNether = new DoubleOption(this, "generation", "HeatGenerationNether", 100D,
          "Add this amount of Joules to the energy produced by a heat generator if it is in the Nether.");

    public final DoubleOption solarGeneration = new DoubleOption(this, "generation", "SolarGeneration", 50D,
          "Peak output for the Solar Generator. Note: It can go higher than this value in some extreme environments.");

    public final IntOption turbineBladesPerCoil = new IntOption(this, "generation", "TurbineBladesPerCoil", 4,
          "The number of blades on each turbine coil per blade applied.");

    public final DoubleOption turbineVentGasFlow = new DoubleOption(this, "generation", "TurbineVentGasFlow", 16000D,
          "The rate at which steam is vented into the turbine.");

    public final DoubleOption turbineDisperserGasFlow = new DoubleOption(this, "generation", "TurbineDisperserGasFlow", 640D,
          "The rate at which steam is dispersed into the turbine.");

    public final IntOption condenserRate = new IntOption(this, "generation", "TurbineCondenserFlowRate", 32000,
          "The rate at which steam is condensed in the turbine.");

    public final DoubleOption energyPerFusionFuel = new DoubleOption(this, "generation", "EnergyPerFusionFuel", 5E6D,
          "Affects the Injection Rate, Max Temp, and Ignition Temp.");

    public final DoubleOption windGenerationMin = new DoubleOption(this, "generation", "WindGenerationMin", 60D,
          "Minimum base generation value of the Wind Generator.");

    public final DoubleOption windGenerationMax = new DoubleOption(this, "generation", "WindGenerationMax", 480D,
          "Maximum base generation value of the Wind Generator.");

    public final IntOption windGenerationMinY = new IntOption(this, "generation", "WindGenerationMinY", 24,
          "The minimum Y value that affects the Wind Generators Power generation.");

    public final IntOption windGenerationMaxY = new IntOption(this, "generation", "WindGenerationMaxY", 255,
          "The maximum Y value that affects the Wind Generators Power generation.");

    public final IntSetOption windGenerationDimBlacklist = new IntSetOption(this, "generation", "WindGenerationDimBlacklist", new int[0],
          "The list of dimension ids that the Wind Generator will not generate power in.");

    public TypeConfigManager<BlockStateGenerator.GeneratorType> generatorsManager = new TypeConfigManager<>(this, "generators", BlockStateGenerator.GeneratorType.class,
          BlockStateGenerator.GeneratorType::getGeneratorsForConfig, t -> t.blockName);

    @Override
    public void load(Configuration config) {
        super.load(config);
        validate();
    }

    @Override
    public void read(ByteBuf config) {
        super.read(config);
        validate();
    }

    private void validate() {
        //ensure windGenerationMaxY is > windGenerationMinY
        windGenerationMaxY.set(Math.max(windGenerationMinY.val() + 1, windGenerationMaxY.val()));
    }
}