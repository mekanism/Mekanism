package mekanism.common.config;

import mekanism.api.math.FloatingLong;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedUnsignedLongValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class UsageConfig extends BaseMekanismConfig {

    private static final String TELEPORTER_CATEGORY = "teleporter";

    private final ModConfigSpec configSpec;

    public final CachedUnsignedLongValue enrichmentChamber;
    public final CachedUnsignedLongValue osmiumCompressor;
    public final CachedUnsignedLongValue combiner;
    public final CachedUnsignedLongValue crusher;
    public final CachedUnsignedLongValue metallurgicInfuser;
    public final CachedUnsignedLongValue purificationChamber;
    public final CachedUnsignedLongValue energizedSmelter;
    public final CachedUnsignedLongValue digitalMiner;
    public final CachedUnsignedLongValue electricPump;
    public final CachedUnsignedLongValue chargePad;
    public final CachedUnsignedLongValue rotaryCondensentrator;
    public final CachedUnsignedLongValue chemicalOxidizer;
    public final CachedUnsignedLongValue chemicalInfuser;
    public final CachedUnsignedLongValue chemicalInjectionChamber;
    public final CachedUnsignedLongValue precisionSawmill;
    public final CachedUnsignedLongValue chemicalDissolutionChamber;
    public final CachedUnsignedLongValue chemicalWasher;
    public final CachedUnsignedLongValue chemicalCrystallizer;
    public final CachedUnsignedLongValue seismicVibrator;
    public final CachedUnsignedLongValue pressurizedReactionBase;
    public final CachedUnsignedLongValue fluidicPlenisher;
    public final CachedUnsignedLongValue laser;
    public final CachedUnsignedLongValue formulaicAssemblicator;
    public final CachedUnsignedLongValue modificationStation;
    public final CachedUnsignedLongValue isotopicCentrifuge;
    public final CachedUnsignedLongValue nutritionalLiquifier;
    public final CachedUnsignedLongValue antiprotonicNucleosynthesizer;
    public final CachedUnsignedLongValue pigmentExtractor;
    public final CachedUnsignedLongValue pigmentMixer;
    public final CachedUnsignedLongValue paintingMachine;
    public final CachedUnsignedLongValue dimensionalStabilizer;

    public final CachedUnsignedLongValue teleporterBase;
    public final CachedUnsignedLongValue teleporterDistance;
    public final CachedUnsignedLongValue teleporterDimensionPenalty;

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