package mekanism.common.config;

/**
 * Created by Thiakil on 15/03/2019.
 */
public class UsageConfig extends BaseConfig {

    public final DoubleOption enrichmentChamberUsage = new DoubleOption(this, "usage", "EnrichmentChamberUsage", 50D,
          "Energy per operation tick (Joules)");

    public final DoubleOption osmiumCompressorUsage = new DoubleOption(this, "usage", "OsmiumCompressorUsage", 100D,
          "Energy per operation tick (Joules)");

    public final DoubleOption combinerUsage = new DoubleOption(this, "usage", "CombinerUsage", 50D,
          "Energy per operation tick (Joules)");

    public final DoubleOption crusherUsage = new DoubleOption(this, "usage", "CrusherUsage", 50D,
          "Energy per operation tick (Joules)");

    public final DoubleOption metallurgicInfuserUsage = new DoubleOption(this, "usage", "MetallurgicInfuserUsage", 50D,
          "Energy per operation tick (Joules)");

    public final DoubleOption purificationChamberUsage = new DoubleOption(this, "usage", "PurificationChamberUsage",
          200D, "Energy per operation tick (Joules)");

    public final DoubleOption energizedSmelterUsage = new DoubleOption(this, "usage", "EnergizedSmelterUsage", 50D,
          "Energy per operation tick (Joules)");

    public final DoubleOption digitalMinerUsage = new DoubleOption(this, "usage", "DigitalMinerUsage", 100D,
          "Energy per operation tick (Joules)");

    public final DoubleOption electricPumpUsage = new DoubleOption(this, "usage", "ElectricPumpUsage", 100D,
          "Energy per operation tick (Joules)");

    public final DoubleOption rotaryCondensentratorUsage = new DoubleOption(this, "usage", "RotaryCondensentratorUsage",
          50D, "Energy per operation tick (Joules)");

    public final DoubleOption oxidationChamberUsage = new DoubleOption(this, "usage", "OxidationChamberUsage", 200D,
          "Energy per operation tick (Joules)");

    public final DoubleOption chemicalInfuserUsage = new DoubleOption(this, "usage", "ChemicalInfuserUsage", 200D,
          "Energy per operation tick (Joules)");

    public final DoubleOption chemicalInjectionChamberUsage = new DoubleOption(this, "usage",
          "ChemicalInjectionChamberUsage", 400D, "Energy per operation tick (Joules)");

    public final DoubleOption precisionSawmillUsage = new DoubleOption(this, "usage", "PrecisionSawmillUsage", 50D,
          "Energy per operation tick (Joules)");

    public final DoubleOption chemicalDissolutionChamberUsage = new DoubleOption(this, "usage",
          "ChemicalDissolutionChamberUsage", 400D, "Energy per operation tick (Joules)");

    public final DoubleOption chemicalWasherUsage = new DoubleOption(this, "usage", "ChemicalWasherUsage", 200D,
          "Energy per operation tick (Joules)");

    public final DoubleOption chemicalCrystallizerUsage = new DoubleOption(this, "usage", "ChemicalCrystallizerUsage",
          400D, "Energy per operation tick (Joules)");

    public final DoubleOption seismicVibratorUsage = new DoubleOption(this, "usage", "SeismicVibratorUsage", 50D,
          "Energy per operation tick (Joules)");

    public final DoubleOption pressurizedReactionBaseUsage = new DoubleOption(this, "usage",
          "PressurizedReactionBaseUsage", 5D, "Energy per operation tick (Joules)");

    public final DoubleOption fluidicPlenisherUsage = new DoubleOption(this, "usage", "FluidicPlenisherUsage", 100D,
          "Energy per operation tick (Joules)");

    public final DoubleOption laserUsage = new DoubleOption(this, "usage", "LaserUsage", 5000D,
          "Energy per operation tick (Joules)");

    public final DoubleOption heavyWaterElectrolysisUsage = new DoubleOption(this, "usage",
          "HeavyWaterElectrolysisUsage", 800D,
          "Energy needed for one [recipe unit] of heavy water production (Joules)");

    public final DoubleOption formulaicAssemblicatorUsage = new DoubleOption(this, "usage",
          "FormulaicAssemblicatorUsage", 100D, "Energy per operation tick (Joules)");

    public final IntOption teleporterBaseUsage = new IntOption(this, "usage", "TeleporterBaseUsage", 1000,
          "Base Joules cost for a teleportation (basically flagfall)");

    public final IntOption teleporterDistanceUsage = new IntOption(this, "usage", "TeleporterDistanceUsage", 10,
          "Joules per unit of distance travelled in teleport - sqrt(xDiff^2 + yDiff^2 + zDiff^2)");

    public final IntOption teleporterDimensionPenalty = new IntOption(this, "usage", "TeleporterDimensionPenalty",
          10000, "Flat additional cost for interdimensional teleport");
}
