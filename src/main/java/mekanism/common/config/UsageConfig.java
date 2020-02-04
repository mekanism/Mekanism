package mekanism.common.config;

import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedIntValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class UsageConfig extends BaseMekanismConfig {

    private static final String TELEPORTER_CATEGORY = "teleporter";

    private final ForgeConfigSpec configSpec;

    public final CachedDoubleValue enrichmentChamber;
    public final CachedDoubleValue osmiumCompressor;
    public final CachedDoubleValue combiner;
    public final CachedDoubleValue crusher;
    public final CachedDoubleValue metallurgicInfuser;
    public final CachedDoubleValue purificationChamber;
    public final CachedDoubleValue energizedSmelter;
    public final CachedDoubleValue digitalMiner;
    public final CachedDoubleValue electricPump;
    public final CachedDoubleValue rotaryCondensentrator;
    public final CachedDoubleValue oxidationChamber;
    public final CachedDoubleValue chemicalInfuser;
    public final CachedDoubleValue chemicalInjectionChamber;
    public final CachedDoubleValue precisionSawmill;
    public final CachedDoubleValue chemicalDissolutionChamber;
    public final CachedDoubleValue chemicalWasher;
    public final CachedDoubleValue chemicalCrystallizer;
    public final CachedDoubleValue seismicVibrator;
    public final CachedDoubleValue pressurizedReactionBase;
    public final CachedDoubleValue fluidicPlenisher;
    public final CachedDoubleValue laser;
    public final CachedDoubleValue formulaicAssemblicator;

    public final CachedIntValue teleporterBase;
    public final CachedIntValue teleporterDistance;
    public final CachedIntValue teleporterDimensionPenalty;

    UsageConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Machine Energy Usage Config. This config is synced from server to client.").push("usage");

        enrichmentChamber = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("enrichmentChamber", 50D));
        osmiumCompressor = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("osmiumCompressor", 100D));
        combiner = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("combiner", 50D));
        crusher = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("crusher", 50D));
        metallurgicInfuser = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("metallurgicInfuser", 50D));
        purificationChamber = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("purificationChamber", 200D));
        energizedSmelter = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("energizedSmelter", 50D));
        digitalMiner = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("digitalMiner", 100D));
        electricPump = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("electricPump", 100D));
        rotaryCondensentrator = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("rotaryCondensentrator", 50D));
        oxidationChamber = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("oxidationChamber", 200D));
        chemicalInfuser = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("chemicalInfuser", 200D));
        chemicalInjectionChamber = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("chemicalInjectionChamber", 400D));
        precisionSawmill = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("precisionSawmill", 50D));
        chemicalDissolutionChamber = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("chemicalDissolutionChamber", 400D));
        chemicalWasher = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("chemicalWasher", 200D));
        chemicalCrystallizer = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("chemicalCrystallizer", 400D));
        seismicVibrator = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("seismicVibrator", 50D));
        pressurizedReactionBase = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("pressurizedReactionBase", 5D));
        fluidicPlenisher = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("fluidicPlenisher", 100D));
        laser = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("laser", 5_000D));
        formulaicAssemblicator = CachedDoubleValue.wrap(this, builder.comment("Energy per operation tick (Joules).")
              .define("formulaicAssemblicator", 100D));

        builder.comment("Teleporter").push(TELEPORTER_CATEGORY);

        teleporterBase = CachedIntValue.wrap(this, builder.comment("Base Joules cost for a teleportation.")
              .define("teleporterBase", 1_000));
        teleporterDistance = CachedIntValue.wrap(this, builder.comment("Joules per unit of distance travelled during teleportation - sqrt(xDiff^2 + yDiff^2 + zDiff^2).")
              .define("teleporterDistance", 10));
        teleporterDimensionPenalty = CachedIntValue.wrap(this, builder.comment("Flat additional cost for interdimensional teleportation.")
              .define("teleporterDimensionPenalty", 10_000));

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