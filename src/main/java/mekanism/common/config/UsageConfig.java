package mekanism.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.config.ModConfig.Type;

public class UsageConfig implements IMekanismConfig {

    private static final String TELEPORTER_CATEGORY = "teleporter";

    private final ForgeConfigSpec configSpec;

    public final ConfigValue<Double> enrichmentChamber;
    public final ConfigValue<Double> osmiumCompressor;
    public final ConfigValue<Double> combiner;
    public final ConfigValue<Double> crusher;
    public final ConfigValue<Double> metallurgicInfuser;
    public final ConfigValue<Double> purificationChamber;
    public final ConfigValue<Double> energizedSmelter;
    public final ConfigValue<Double> digitalMiner;
    public final ConfigValue<Double> electricPump;
    public final ConfigValue<Double> rotaryCondensentrator;
    public final ConfigValue<Double> oxidationChamber;
    public final ConfigValue<Double> chemicalInfuser;
    public final ConfigValue<Double> chemicalInjectionChamber;
    public final ConfigValue<Double> precisionSawmill;
    public final ConfigValue<Double> chemicalDissolutionChamber;
    public final ConfigValue<Double> chemicalWasher;
    public final ConfigValue<Double> chemicalCrystallizer;
    public final ConfigValue<Double> seismicVibrator;
    public final ConfigValue<Double> pressurizedReactionBase;
    public final ConfigValue<Double> fluidicPlenisher;
    public final ConfigValue<Double> laser;
    public final ConfigValue<Double> heavyWaterElectrolysis;
    public final ConfigValue<Double> formulaicAssemblicator;

    public final ConfigValue<Integer> teleporterBase;
    public final ConfigValue<Integer> teleporterDistance;
    public final ConfigValue<Integer> teleporterDimensionPenalty;

    UsageConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Machine Energy Usage Config. This config is synced from server to client.").push("usage");

        enrichmentChamber = builder.comment("Energy per operation tick (Joules).").define("enrichmentChamber", 50D);
        osmiumCompressor = builder.comment("Energy per operation tick (Joules).").define("osmiumCompressor", 100D);
        combiner = builder.comment("Energy per operation tick (Joules).").define("combiner", 50D);
        crusher = builder.comment("Energy per operation tick (Joules).").define("crusher", 50D);
        metallurgicInfuser = builder.comment("Energy per operation tick (Joules).").define("metallurgicInfuser", 50D);
        purificationChamber = builder.comment("Energy per operation tick (Joules).").define("purificationChamber", 200D);
        energizedSmelter = builder.comment("Energy per operation tick (Joules).").define("energizedSmelter", 50D);
        digitalMiner = builder.comment("Energy per operation tick (Joules).").define("digitalMiner", 100D);
        electricPump = builder.comment("Energy per operation tick (Joules).").define("electricPump", 100D);
        rotaryCondensentrator = builder.comment("Energy per operation tick (Joules).").define("rotaryCondensentrator", 50D);
        oxidationChamber = builder.comment("Energy per operation tick (Joules).").define("oxidationChamber", 200D);
        chemicalInfuser = builder.comment("Energy per operation tick (Joules).").define("chemicalInfuser", 200D);
        chemicalInjectionChamber = builder.comment("Energy per operation tick (Joules).").define("chemicalInjectionChamber", 400D);
        precisionSawmill = builder.comment("Energy per operation tick (Joules).").define("precisionSawmill", 50D);
        chemicalDissolutionChamber = builder.comment("Energy per operation tick (Joules).").define("chemicalDissolutionChamber", 400D);
        chemicalWasher = builder.comment("Energy per operation tick (Joules).").define("chemicalWasher", 200D);
        chemicalCrystallizer = builder.comment("Energy per operation tick (Joules).").define("chemicalCrystallizer", 400D);
        seismicVibrator = builder.comment("Energy per operation tick (Joules).").define("seismicVibrator", 50D);
        pressurizedReactionBase = builder.comment("Energy per operation tick (Joules).").define("pressurizedReactionBase", 5D);
        fluidicPlenisher = builder.comment("Energy per operation tick (Joules).").define("fluidicPlenisher", 100D);
        laser = builder.comment("Energy per operation tick (Joules).").define("laser", 5_000D);
        formulaicAssemblicator = builder.comment("Energy per operation tick (Joules).").define("formulaicAssemblicator", 100D);
        heavyWaterElectrolysis = builder.comment("Energy needed for one [recipe unit] of heavy water production (Joules).").define("heavyWaterElectrolysis", 800D);

        builder.comment("Teleporter").push(TELEPORTER_CATEGORY);

        teleporterBase = builder.comment("Base Joules cost for a teleportation.").define("teleporterBase", 1_000);
        teleporterDistance = builder.comment("Joules per unit of distance travelled during teleportation - sqrt(xDiff^2 + yDiff^2 + zDiff^2).")
              .define("teleporterDistance", 10);
        teleporterDimensionPenalty = builder.comment("Flat additional cost for interdimensional teleportation.").define("teleporterDimensionPenalty", 10_000);

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