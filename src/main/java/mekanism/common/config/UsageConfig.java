package mekanism.common.config;

import mekanism.api.math.FloatingLong;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedLongValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class UsageConfig extends BaseMekanismConfig {

    private static final String TELEPORTER_CATEGORY = "teleporter";

    private final ModConfigSpec configSpec;

    public final CachedLongValue enrichmentChamber;
    public final CachedLongValue osmiumCompressor;
    public final CachedLongValue combiner;
    public final CachedLongValue crusher;
    public final CachedLongValue metallurgicInfuser;
    public final CachedLongValue purificationChamber;
    public final CachedLongValue energizedSmelter;
    public final CachedLongValue digitalMiner;
    public final CachedLongValue electricPump;
    public final CachedLongValue chargePad;
    public final CachedLongValue rotaryCondensentrator;
    public final CachedLongValue chemicalOxidizer;
    public final CachedLongValue chemicalInfuser;
    public final CachedLongValue chemicalInjectionChamber;
    public final CachedLongValue precisionSawmill;
    public final CachedLongValue chemicalDissolutionChamber;
    public final CachedLongValue chemicalWasher;
    public final CachedLongValue chemicalCrystallizer;
    public final CachedLongValue seismicVibrator;
    public final CachedLongValue pressurizedReactionBase;
    public final CachedLongValue fluidicPlenisher;
    public final CachedLongValue laser;
    public final CachedLongValue formulaicAssemblicator;
    public final CachedLongValue modificationStation;
    public final CachedLongValue isotopicCentrifuge;
    public final CachedLongValue nutritionalLiquifier;
    public final CachedLongValue antiprotonicNucleosynthesizer;
    public final CachedLongValue pigmentExtractor;
    public final CachedLongValue pigmentMixer;
    public final CachedLongValue paintingMachine;
    public final CachedLongValue dimensionalStabilizer;

    public final CachedLongValue teleporterBase;
    public final CachedLongValue teleporterDistance;
    public final CachedLongValue teleporterDimensionPenalty;

    UsageConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Machine Energy Usage Config. This config is synced from server to client.").push("usage");

        enrichmentChamber = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "enrichmentChamber",
              FloatingLong.createConst(50));
        osmiumCompressor = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "osmiumCompressor",
              FloatingLong.createConst(100));
        combiner = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "combiner", FloatingLong.createConst(50));
        crusher = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "crusher", FloatingLong.createConst(50));
        metallurgicInfuser = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "metallurgicInfuser",
              FloatingLong.createConst(50));
        purificationChamber = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "purificationChamber",
              FloatingLong.createConst(200));
        energizedSmelter = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "energizedSmelter",
              FloatingLong.createConst(50));
        digitalMiner = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "digitalMiner",
              FloatingLong.createConst(1_000));
        electricPump = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "electricPump",
              FloatingLong.createConst(100));
        chargePad = CachedFloatingLongValue.define(this, builder, "Energy that can be transferred at once per charge operation (Joules).", "chargePad",
              FloatingLong.createConst(1_024_000));
        rotaryCondensentrator = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "rotaryCondensentrator",
              FloatingLong.createConst(50));
        chemicalOxidizer = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "chemicalOxidizer",
              FloatingLong.createConst(200));
        chemicalInfuser = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "chemicalInfuser",
              FloatingLong.createConst(200));
        chemicalInjectionChamber = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "chemicalInjectionChamber",
              FloatingLong.createConst(400));
        precisionSawmill = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "precisionSawmill",
              FloatingLong.createConst(50));
        chemicalDissolutionChamber = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "chemicalDissolutionChamber",
              FloatingLong.createConst(400));
        chemicalWasher = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "chemicalWasher",
              FloatingLong.createConst(200));
        chemicalCrystallizer = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "chemicalCrystallizer",
              FloatingLong.createConst(400));
        seismicVibrator = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "seismicVibrator",
              FloatingLong.createConst(50));
        pressurizedReactionBase = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "pressurizedReactionBase",
              FloatingLong.createConst(5));
        fluidicPlenisher = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "fluidicPlenisher",
              FloatingLong.createConst(100));
        laser = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "laser", FloatingLong.createConst(10_000));
        formulaicAssemblicator = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "formulaicAssemblicator",
              FloatingLong.createConst(100));
        modificationStation = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "modificationStation",
              FloatingLong.createConst(400));
        isotopicCentrifuge = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "isotopicCentrifuge",
              FloatingLong.createConst(200));
        nutritionalLiquifier = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "nutritionalLiquifier",
              FloatingLong.createConst(200));
        antiprotonicNucleosynthesizer = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "antiprotonicNucleosynthesizer",
              FloatingLong.createConst(100_000));
        pigmentExtractor = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "pigmentExtractor",
              FloatingLong.createConst(200));
        pigmentMixer = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "pigmentMixer",
              FloatingLong.createConst(200));
        paintingMachine = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "paintingMachine",
              FloatingLong.createConst(100));
        dimensionalStabilizer = CachedFloatingLongValue.define(this, builder, "Energy per chunk per tick (Joules).", "dimensionalStabilizer",
              FloatingLong.createConst(5_000));

        builder.comment("Teleporter").push(TELEPORTER_CATEGORY);

        teleporterBase = CachedFloatingLongValue.define(this, builder, "Base Joules cost for a teleportation.", "teleporterBase", FloatingLong.createConst(1_000));
        teleporterDistance = CachedFloatingLongValue.define(this, builder, "Joules per unit of distance travelled during teleportation - sqrt(xDiff^2 + yDiff^2 + zDiff^2).",
              "teleporterDistance", FloatingLong.createConst(10));
        teleporterDimensionPenalty = CachedFloatingLongValue.define(this, builder, "Flat additional cost for interdimensional teleportation. Distance is still taken into account minimizing energy cost based on dimension scales.",
              "teleporterDimensionPenalty", FloatingLong.createConst(10_000));

        builder.pop();

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "machine-usage";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }

    @Override
    public boolean addToContainer() {
        return false;
    }
}