package mekanism.common.config;

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

        enrichmentChamber = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "enrichmentChamber",
              50L);
        osmiumCompressor = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "osmiumCompressor",
              100L);
        combiner = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "combiner", 50L);
        crusher = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "crusher", 50L);
        metallurgicInfuser = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "metallurgicInfuser",
              50L);
        purificationChamber = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "purificationChamber",
              200L);
        energizedSmelter = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "energizedSmelter",
              50L);
        digitalMiner = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "digitalMiner",
              1_000L);
        electricPump = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "electricPump",
              100L);
        chargePad = CachedLongValue.definePositive(this, builder, "Energy that can be transferred at once per charge operation (Joules).", "chargePad",
              1_024_000L);
        rotaryCondensentrator = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "rotaryCondensentrator",
              50L);
        chemicalOxidizer = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "chemicalOxidizer",
              200L);
        chemicalInfuser = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "chemicalInfuser",
              200L);
        chemicalInjectionChamber = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "chemicalInjectionChamber",
              400L);
        precisionSawmill = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "precisionSawmill",
              50L);
        chemicalDissolutionChamber = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "chemicalDissolutionChamber",
              400L);
        chemicalWasher = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "chemicalWasher",
              200L);
        chemicalCrystallizer = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "chemicalCrystallizer",
              400L);
        seismicVibrator = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "seismicVibrator",
              50L);
        pressurizedReactionBase = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "pressurizedReactionBase",
              5L);
        fluidicPlenisher = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "fluidicPlenisher",
              100L);
        laser = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "laser", 10_000L);
        formulaicAssemblicator = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "formulaicAssemblicator",
              100L);
        modificationStation = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "modificationStation",
              400L);
        isotopicCentrifuge = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "isotopicCentrifuge",
              200L);
        nutritionalLiquifier = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "nutritionalLiquifier",
              200L);
        antiprotonicNucleosynthesizer = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "antiprotonicNucleosynthesizer",
              100_000L);
        pigmentExtractor = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "pigmentExtractor",
              200L);
        pigmentMixer = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "pigmentMixer",
              200L);
        paintingMachine = CachedLongValue.definePositive(this, builder, "Energy per operation tick (Joules).", "paintingMachine",
              100L);
        dimensionalStabilizer = CachedLongValue.definePositive(this, builder, "Energy per chunk per tick (Joules).", "dimensionalStabilizer",
              5_000L);

        builder.comment("Teleporter").push(TELEPORTER_CATEGORY);

        teleporterBase = CachedLongValue.definePositive(this, builder, "Base Joules cost for a teleportation.", "teleporterBase", 1_000L);
        teleporterDistance = CachedLongValue.definePositive(this, builder, "Joules per unit of distance travelled during teleportation - sqrt(xDiff^2 + yDiff^2 + zDiff^2).",
              "teleporterDistance", 10L);
        teleporterDimensionPenalty = CachedLongValue.definePositive(this, builder, "Flat additional cost for interdimensional teleportation. Distance is still taken into account minimizing energy cost based on dimension scales.",
              "teleporterDimensionPenalty", 10_000L);

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
}