package mekanism.common.config;

import mekanism.common.config.options.DoubleOption;

public class StorageConfig extends BaseConfig {

    public final DoubleOption enrichmentChamber = new DoubleOption(this, "storage",
            "EnrichmentChamberStorage", 20000D, "Base energy storage (Joules).");

    public final DoubleOption osmiumCompressor = new DoubleOption(this, "storage",
            "OsmiumCompressorStorage", 80000D, "Base energy storage (Joules).");

    public final DoubleOption combiner = new DoubleOption(this, "storage",
            "CombinerStorage", 40000D, "Base energy storage (Joules).");

    public final DoubleOption crusher = new DoubleOption(this, "storage",
            "CrusherStorage", 20000D, "Base energy storage (Joules).");

    public final DoubleOption metallurgicInfuser = new DoubleOption(this, "storage",
            "MetallurgicInfuserStorage", 20000D, "Base energy storage (Joules).");

    public final DoubleOption purificationChamber = new DoubleOption(this, "storage",
            "PurificationChamberStorage", 80000D, "Base energy storage (Joules).");

    public final DoubleOption energizedSmelter = new DoubleOption(this, "storage",
            "EnergizedSmelterStorage", 20000D, "Base energy storage (Joules).");

    public final DoubleOption digitalMiner = new DoubleOption(this, "storage",
            "DigitalMinerStorage", 40000D, "Base energy storage (Joules).");

    public final DoubleOption electricPump = new DoubleOption(this, "storage",
            "ElectricPumpStorage", 40000D, "Base energy storage (Joules).");

    public final DoubleOption chargePad = new DoubleOption(this, "storage",
            "ChargePadStorage", 40000D, "Base energy storage (Joules).");

    public final DoubleOption rotaryCondensentrator = new DoubleOption(this, "storage",
            "RotaryCondensentratorStorage", 20000D, "Base energy storage (Joules).");

    public final DoubleOption oxidationChamber = new DoubleOption(this, "storage",
            "OxidationChamberStorage", 80000D, "Base energy storage (Joules).");

    public final DoubleOption chemicalInfuser = new DoubleOption(this, "storage",
            "ChemicalInfuserStorage", 80000D, "Base energy storage (Joules).");

    public final DoubleOption chemicalInjectionChamber = new DoubleOption(this, "storage",
            "ChemicalInjectionChamberStorage", 160000D, "Base energy storage (Joules).");

    public final DoubleOption electrolyticSeparator = new DoubleOption(this, "storage",
            "ElectrolyticSeparatorStorage", 160000D, "Base energy storage (Joules).");

    public final DoubleOption precisionSawmill = new DoubleOption(this, "storage",
            "PrecisionSawmillStorage", 20000D, "Base energy storage (Joules).");

    public final DoubleOption chemicalDissolutionChamber = new DoubleOption(this, "storage",
            "ChemicalDissolutionChamberStorage", 160000D, "Base energy storage (Joules).");

    public final DoubleOption chemicalWasher = new DoubleOption(this, "storage",
            "ChemicalWasherStorage", 80000D, "Base energy storage (Joules).");

    public final DoubleOption chemicalCrystallizer = new DoubleOption(this, "storage",
            "ChemicalCrystallizerStorage", 160000D, "Base energy storage (Joules).");

    public final DoubleOption seismicVibrator = new DoubleOption(this, "storage",
            "SeismicVibratorStorage", 20000D, "Base energy storage (Joules).");

    public final DoubleOption pressurizedReactionBase = new DoubleOption(this, "storage",
            "PressurizedReactionBaseStorage", 2000D, "Base energy storage (Joules).");

    public final DoubleOption fluidicPlenisher = new DoubleOption(this, "storage",
            "FluidicPlenisherStorage", 40000D, "Base energy storage (Joules).");

    public final DoubleOption laser = new DoubleOption(this, "storage",
            "LaserStorage", 2000000D, "Base energy storage (Joules).");

    public final DoubleOption formulaicAssemblicator = new DoubleOption(this, "storage",
            "FormulaicAssemblicatorStorage", 40000D, "Base energy storage (Joules).");

    public final DoubleOption teleporter = new DoubleOption(this, "storage",
            "TeleporterStorage", 5000000D, "Base energy storage (Joules).");

}
