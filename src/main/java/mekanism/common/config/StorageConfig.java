package mekanism.common.config;

import mekanism.common.config.options.DoubleOption;

public class StorageConfig extends BaseConfig {

    public final DoubleOption enrichmentChamberStorage = new DoubleOption(this, "storage",
            "EnrichmentChamberStorage", 20000D, "Base energy storage (Joules).");

    public final DoubleOption osmiumCompressorStorage = new DoubleOption(this, "storage",
            "OsmiumCompressorStorage", 80000D, "Base energy storage (Joules).");

    public final DoubleOption combinerStorage = new DoubleOption(this, "storage",
            "CombinerStorage", 40000D, "Base energy storage (Joules).");

    public final DoubleOption crusherStorage = new DoubleOption(this, "storage",
            "CrusherStorage", 20000D, "Base energy storage (Joules).");

    public final DoubleOption metallurgicInfuserStorage = new DoubleOption(this, "storage",
            "MetallurgicInfuserStorage", 20000D, "Base energy storage (Joules).");

    public final DoubleOption purificationChamberStorage = new DoubleOption(this, "storage",
            "PurificationChamberStorage", 80000D, "Base energy storage (Joules).");

    public final DoubleOption energizedSmelterStorage = new DoubleOption(this, "storage",
            "EnergizedSmelterStorage", 20000D, "Base energy storage (Joules).");

    public final DoubleOption digitalMinerStorage = new DoubleOption(this, "storage",
            "DigitalMinerStorage", 40000D, "Base energy storage (Joules).");

    public final DoubleOption electricPumpStorage = new DoubleOption(this, "storage",
            "ElectricPumpStorage", 40000D, "Base energy storage (Joules).");

    public final DoubleOption chargePadStorage = new DoubleOption(this, "storage",
            "ChargePadStorage", 40000D, "Base energy storage (Joules).");

    public final DoubleOption rotaryCondensentratorStorage = new DoubleOption(this, "storage",
            "RotaryCondensentratorStorage", 20000D, "Base energy storage (Joules).");

    public final DoubleOption oxidationChamberStorage = new DoubleOption(this, "storage",
            "OxidationChamberStorage", 80000D, "Base energy storage (Joules).");

    public final DoubleOption chemicalInfuserStorage = new DoubleOption(this, "storage",
            "ChemicalInfuserStorage", 80000D, "Base energy storage (Joules).");

    public final DoubleOption chemicalInjectionChamberStorage = new DoubleOption(this, "storage",
            "ChemicalInjectionChamberStorage", 160000D, "Base energy storage (Joules).");

    public final DoubleOption electrolyticSeparatorStorage = new DoubleOption(this, "storage",
            "ElectrolyticSeparatorStorage", 160000D, "Base energy storage (Joules).");

    public final DoubleOption precisionSawmillStorage = new DoubleOption(this, "storage",
            "PrecisionSawmillStorage", 20000D, "Base energy storage (Joules).");

    public final DoubleOption chemicalDissolutionChamberStorage = new DoubleOption(this, "storage",
            "ChemicalDissolutionChamberStorage", 160000D, "Base energy storage (Joules).");

    public final DoubleOption chemicalWasherStorage = new DoubleOption(this, "storage",
            "ChemicalWasherStorage", 80000D, "Base energy storage (Joules).");

    public final DoubleOption chemicalCrystallizerStorage = new DoubleOption(this, "storage",
            "ChemicalCrystallizerStorage", 160000D, "Base energy storage (Joules).");

    public final DoubleOption seismicVibratorStorage = new DoubleOption(this, "storage",
            "SeismicVibratorStorage", 20000D, "Base energy storage (Joules).");

    public final DoubleOption pressurizedReactionBaseStorage = new DoubleOption(this, "storage",
            "PressurizedReactionBaseStorage", 2000D, "Base energy storage (Joules).");

    public final DoubleOption fluidicPlenisherStorage = new DoubleOption(this, "storage",
            "FluidicPlenisherStorage", 40000D, "Base energy storage (Joules).");

    public final DoubleOption laserStorage = new DoubleOption(this, "storage",
            "LaserStorage", 2000000D, "Base energy storage (Joules).");

    public final DoubleOption formulaicAssemblicatorStorage = new DoubleOption(this, "storage",
            "FormulaicAssemblicatorStorage", 40000D, "Base energy storage (Joules).");

    public final DoubleOption teleporterStorage = new DoubleOption(this, "storage",
            "TeleporterStorage", 5000000D, "Base energy storage (Joules).");

}
