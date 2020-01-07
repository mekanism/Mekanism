package mekanism.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.config.ModConfig.Type;

public class StorageConfig implements IMekanismConfig {

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
    public final ConfigValue<Double> chargePad;
    public final ConfigValue<Double> rotaryCondensentrator;
    public final ConfigValue<Double> oxidationChamber;
    public final ConfigValue<Double> chemicalInfuser;
    public final ConfigValue<Double> chemicalInjectionChamber;
    public final ConfigValue<Double> electrolyticSeparator;
    public final ConfigValue<Double> precisionSawmill;
    public final ConfigValue<Double> chemicalDissolutionChamber;
    public final ConfigValue<Double> chemicalWasher;
    public final ConfigValue<Double> chemicalCrystallizer;
    public final ConfigValue<Double> seismicVibrator;
    public final ConfigValue<Double> pressurizedReactionBase;
    public final ConfigValue<Double> fluidicPlenisher;
    public final ConfigValue<Double> laser;
    public final ConfigValue<Double> formulaicAssemblicator;
    public final ConfigValue<Double> teleporter;

    StorageConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Machine Energy Storage Config. This config is synced from server to client.").push("storage");

        enrichmentChamber = builder.comment("Base energy storage (Joules).").define("enrichmentChamber", 20_000D);
        osmiumCompressor = builder.comment("Base energy storage (Joules).").define("osmiumCompressor", 80_000D);
        combiner = builder.comment("Base energy storage (Joules).").define("combiner", 40_000D);
        crusher = builder.comment("Base energy storage (Joules).").define("crusher", 20_000D);
        metallurgicInfuser = builder.comment("Base energy storage (Joules).").define("metallurgicInfuser", 20_000D);
        purificationChamber = builder.comment("Base energy storage (Joules).").define("purificationChamber", 80_000D);
        energizedSmelter = builder.comment("Base energy storage (Joules).").define("energizedSmelter", 20_000D);
        digitalMiner = builder.comment("Base energy storage (Joules).").define("digitalMiner", 40_000D);
        electricPump = builder.comment("Base energy storage (Joules).").define("electricPump", 40_000D);
        chargePad = builder.comment("Base energy storage (Joules).").define("chargePad", 40_000D);
        rotaryCondensentrator = builder.comment("Base energy storage (Joules).").define("rotaryCondensentrator", 20_000D);
        oxidationChamber = builder.comment("Base energy storage (Joules).").define("oxidationChamber", 80_000D);
        chemicalInfuser = builder.comment("Base energy storage (Joules).").define("chemicalInfuser", 80_000D);
        chemicalInjectionChamber = builder.comment("Base energy storage (Joules).").define("chemicalInjectionChamber", 160_000D);
        electrolyticSeparator = builder.comment("Base energy storage (Joules).").define("electrolyticSeparator", 160_000D);
        precisionSawmill = builder.comment("Base energy storage (Joules).").define("precisionSawmill", 20_000D);
        chemicalDissolutionChamber = builder.comment("Base energy storage (Joules).").define("chemicalDissolutionChamber", 160_000D);
        chemicalWasher = builder.comment("Base energy storage (Joules).").define("chemicalWasher", 80_000D);
        chemicalCrystallizer = builder.comment("Base energy storage (Joules).").define("chemicalCrystallizer", 160_000D);
        seismicVibrator = builder.comment("Base energy storage (Joules).").define("seismicVibrator", 20_000D);
        pressurizedReactionBase = builder.comment("Base energy storage (Joules).").define("pressurizedReactionBase", 2_000D);
        fluidicPlenisher = builder.comment("Base energy storage (Joules).").define("fluidicPlenisher", 40_000D);
        laser = builder.comment("Base energy storage (Joules).").define("laser", 2_000_000D);
        formulaicAssemblicator = builder.comment("Base energy storage (Joules).").define("formulaicAssemblicator", 40_000D);
        teleporter = builder.comment("Base energy storage (Joules).").define("teleporter", 5_000_000D);

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "machine-storage";
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