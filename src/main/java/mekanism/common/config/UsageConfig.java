package mekanism.common.config;

import mekanism.api.math.FloatingLong;
import mekanism.common.config.value.CachedFloatingLongValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class UsageConfig extends BaseMekanismConfig {

    private static final String TELEPORTER_CATEGORY = "teleporter";

    private final ForgeConfigSpec configSpec;

    public final CachedFloatingLongValue enrichmentChamber;
    public final CachedFloatingLongValue osmiumCompressor;
    public final CachedFloatingLongValue combiner;
    public final CachedFloatingLongValue crusher;
    public final CachedFloatingLongValue metallurgicInfuser;
    public final CachedFloatingLongValue purificationChamber;
    public final CachedFloatingLongValue energizedSmelter;
    public final CachedFloatingLongValue digitalMiner;
    public final CachedFloatingLongValue electricPump;
    public final CachedFloatingLongValue rotaryCondensentrator;
    public final CachedFloatingLongValue oxidationChamber;
    public final CachedFloatingLongValue chemicalInfuser;
    public final CachedFloatingLongValue chemicalInjectionChamber;
    public final CachedFloatingLongValue precisionSawmill;
    public final CachedFloatingLongValue chemicalDissolutionChamber;
    public final CachedFloatingLongValue chemicalWasher;
    public final CachedFloatingLongValue chemicalCrystallizer;
    public final CachedFloatingLongValue seismicVibrator;
    public final CachedFloatingLongValue pressurizedReactionBase;
    public final CachedFloatingLongValue fluidicPlenisher;
    public final CachedFloatingLongValue laser;
    public final CachedFloatingLongValue formulaicAssemblicator;
    public final CachedFloatingLongValue modificationStation;
    public final CachedFloatingLongValue isotopicCentrifuge;
    public final CachedFloatingLongValue nutritionalLiquifier;
    public final CachedFloatingLongValue antiprotonicNucleosynthesizer;

    public final CachedFloatingLongValue teleporterBase;
    public final CachedFloatingLongValue teleporterDistance;
    public final CachedFloatingLongValue teleporterDimensionPenalty;

    UsageConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
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
              FloatingLong.createConst(100));
        electricPump = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "electricPump",
              FloatingLong.createConst(100));
        rotaryCondensentrator = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "rotaryCondensentrator",
              FloatingLong.createConst(50));
        oxidationChamber = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "oxidationChamber",
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
              FloatingLong.createConst(100));
        isotopicCentrifuge = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "isotopicCentrifuge",
              FloatingLong.createConst(200));
        nutritionalLiquifier = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "nutritionalLiquifier",
              FloatingLong.createConst(200));
        antiprotonicNucleosynthesizer = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "antiprotonicNucleosynthesizer",
              FloatingLong.createConst(100_000));

        builder.comment("Teleporter").push(TELEPORTER_CATEGORY);

        teleporterBase = CachedFloatingLongValue.define(this, builder, "Base Joules cost for a teleportation.", "teleporterBase", FloatingLong.createConst(1_000));
        teleporterDistance = CachedFloatingLongValue.define(this, builder, "Joules per unit of distance travelled during teleportation - sqrt(xDiff^2 + yDiff^2 + zDiff^2).",
              "teleporterDistance", FloatingLong.createConst(10));
        teleporterDimensionPenalty = CachedFloatingLongValue.define(this, builder, "Flat additional cost for interdimensional teleportation.",
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
    public ForgeConfigSpec getConfigSpec() {
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