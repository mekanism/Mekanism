package mekanism.common.config;

import io.netty.buffer.ByteBuf;
import mekanism.generators.common.block.states.BlockStateGenerator;
import net.minecraftforge.common.config.Configuration;

/**
 * Created by Thiakil on 15/03/2019.
 */
public class GeneratorsConfig extends BaseConfig
{
	public final DoubleOption advancedSolarGeneration = new DoubleOption(this, "generation", "AdvancedSolarGeneration", 300D);

	public final DoubleOption bioGeneration = new DoubleOption(this, "generation", "BioGeneration", 350D);

	public final DoubleOption heatGeneration = new DoubleOption(this, "generation", "HeatGeneration", 150D);

	public final DoubleOption heatGenerationLava = new DoubleOption(this, "generation", "HeatGenerationLava", 5D);

	public final DoubleOption heatGenerationNether = new DoubleOption(this, "generation", "HeatGenerationNether", 100D);

	public final DoubleOption solarGeneration = new DoubleOption(this, "generation", "SolarGeneration", 50D);

	public final IntOption turbineBladesPerCoil = new IntOption(this, "generation", "TurbineBladesPerCoil", 4);

	public final DoubleOption turbineVentGasFlow = new DoubleOption(this, "generation", "TurbineVentGasFlow", 16000D);

	public final DoubleOption turbineDisperserGasFlow = new DoubleOption(this, "generation", "TurbineDisperserGasFlow", 640D);

	public final IntOption condenserRate = new IntOption(this, "generation", "TurbineCondenserFlowRate", 32000);

	public final DoubleOption energyPerFusionFuel = new DoubleOption(this, "generation", "EnergyPerFusionFuel", 5E6D);

	public final DoubleOption windGenerationMin = new DoubleOption(this, "generation", "WindGenerationMin", 60D);

	public final DoubleOption windGenerationMax = new DoubleOption(this, "generation", "WindGenerationMax", 480D);

	public final IntOption windGenerationMinY = new IntOption(this, "generation", "WindGenerationMinY", 24);

	public final IntOption windGenerationMaxY = new IntOption(this, "generation", "WindGenerationMaxY", 255);

	public final IntSetOption windGenerationBlacklist = new IntSetOption(this, "generation", "WindGenerationDimBlacklist", new int[0], "List of dimension ids where Wind Generator will not function and instead report that there is no wind");

	public TypeConfigManager<BlockStateGenerator.GeneratorType> generatorsManager = new TypeConfigManager<>(this, "generators", BlockStateGenerator.GeneratorType.class, BlockStateGenerator.GeneratorType::getGeneratorsForConfig, t->t.blockName);

	@Override
	public void load(Configuration config)
	{
		super.load(config);
		validate();
	}

	@Override
	public void read(ByteBuf config)
	{
		super.read(config);
		validate();
	}

	private void validate()
	{
		//ensure windGenerationMaxY is > windGenerationMinY
		windGenerationMaxY.set(Math.max(windGenerationMinY.val() + 1, windGenerationMaxY.val()));
	}
}
