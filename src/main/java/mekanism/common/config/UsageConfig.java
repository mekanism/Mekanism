package mekanism.common.config;

import mekanism.common.config.options.DoubleOption;
import mekanism.common.config.options.IntOption;

/**
 * Created by Thiakil on 15/03/2019.
 */
public class UsageConfig extends BaseConfig {

    public final DoubleOption enrichmentChamber = new DoubleOption(this, "usage", "EnrichmentChamberUsage", 50D,
          "Energy per operation tick (Joules).");

    public final DoubleOption osmiumCompressor = new DoubleOption(this, "usage", "OsmiumCompressorUsage", 100D,
          "Energy per operation tick (Joules).");

    public final DoubleOption combiner = new DoubleOption(this, "usage", "CombinerUsage", 50D,
          "Energy per operation tick (Joules).");

    public final DoubleOption crusher = new DoubleOption(this, "usage", "CrusherUsage", 50D,
          "Energy per operation tick (Joules).");

    public final DoubleOption metallurgicInfuser = new DoubleOption(this, "usage", "MetallurgicInfuserUsage", 50D,
          "Energy per operation tick (Joules).");

    public final DoubleOption purificationChamber = new DoubleOption(this, "usage", "PurificationChamberUsage", 200D,
          "Energy per operation tick (Joules).");

    public final DoubleOption energizedSmelter = new DoubleOption(this, "usage", "EnergizedSmelterUsage", 50D,
          "Energy per operation tick (Joules).");

    public final DoubleOption digitalMiner = new DoubleOption(this, "usage", "DigitalMinerUsage", 100D,
          "Energy per operation tick (Joules).");

    public final DoubleOption electricPump = new DoubleOption(this, "usage", "ElectricPumpUsage", 100D,
          "Energy per operation tick (Joules).");

    public final DoubleOption rotaryCondensentrator = new DoubleOption(this, "usage", "RotaryCondensentratorUsage", 50D,
          "Energy per operation tick (Joules).");

    public final DoubleOption oxidationChamber = new DoubleOption(this, "usage", "OxidationChamberUsage", 200D,
          "Energy per operation tick (Joules).");

    public final DoubleOption chemicalInfuser = new DoubleOption(this, "usage", "ChemicalInfuserUsage", 200D,
          "Energy per operation tick (Joules).");

    public final DoubleOption chemicalInjectionChamber = new DoubleOption(this, "usage", "ChemicalInjectionChamberUsage", 400D,
          "Energy per operation tick (Joules).");

    public final DoubleOption precisionSawmill = new DoubleOption(this, "usage", "PrecisionSawmillUsage", 50D,
          "Energy per operation tick (Joules).");

    public final DoubleOption chemicalDissolutionChamber = new DoubleOption(this, "usage", "ChemicalDissolutionChamberUsage", 400D,
          "Energy per operation tick (Joules).");

    public final DoubleOption chemicalWasher = new DoubleOption(this, "usage", "ChemicalWasherUsage", 200D,
          "Energy per operation tick (Joules).");

    public final DoubleOption chemicalCrystallizer = new DoubleOption(this, "usage", "ChemicalCrystallizerUsage", 400D,
          "Energy per operation tick (Joules).");

    public final DoubleOption seismicVibrator = new DoubleOption(this, "usage", "SeismicVibratorUsage", 50D,
          "Energy per operation tick (Joules).");

    public final DoubleOption pressurizedReactionBase = new DoubleOption(this, "usage", "PressurizedReactionBaseUsage", 5D,
          "Energy per operation tick (Joules).");

    public final DoubleOption fluidicPlenisher = new DoubleOption(this, "usage", "FluidicPlenisherUsage", 100D,
          "Energy per operation tick (Joules).");

    public final DoubleOption laser = new DoubleOption(this, "usage", "LaserUsage", 5000D,
          "Energy per operation tick (Joules).");

    public final DoubleOption heavyWaterElectrolysis = new DoubleOption(this, "usage", "HeavyWaterElectrolysisUsage", 800D,
          "Energy needed for one [recipe unit] of heavy water production (Joules).");

    public final DoubleOption formulaicAssemblicator = new DoubleOption(this, "usage", "FormulaicAssemblicatorUsage", 100D,
          "Energy per operation tick (Joules).");

    public final IntOption teleporterBase = new IntOption(this, "usage", "TeleporterBaseUsage", 1000,
          "Base Joules cost for a teleportation.");

    public final IntOption teleporterDistance = new IntOption(this, "usage", "TeleporterDistanceUsage", 10,
          "Joules per unit of distance travelled during teleportation - sqrt(xDiff^2 + yDiff^2 + zDiff^2).");

    public final IntOption teleporterDimensionPenalty = new IntOption(this, "usage", "TeleporterDimensionPenalty", 10000,
          "Flat additional cost for interdimensional teleportation.");
}