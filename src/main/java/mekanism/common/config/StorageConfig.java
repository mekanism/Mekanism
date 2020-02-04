package mekanism.common.config;

import mekanism.common.config.value.CachedDoubleValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class StorageConfig extends BaseMekanismConfig {

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
    public final CachedDoubleValue chargePad;
    public final CachedDoubleValue rotaryCondensentrator;
    public final CachedDoubleValue oxidationChamber;
    public final CachedDoubleValue chemicalInfuser;
    public final CachedDoubleValue chemicalInjectionChamber;
    public final CachedDoubleValue electrolyticSeparator;
    public final CachedDoubleValue precisionSawmill;
    public final CachedDoubleValue chemicalDissolutionChamber;
    public final CachedDoubleValue chemicalWasher;
    public final CachedDoubleValue chemicalCrystallizer;
    public final CachedDoubleValue seismicVibrator;
    public final CachedDoubleValue pressurizedReactionBase;
    public final CachedDoubleValue fluidicPlenisher;
    public final CachedDoubleValue laser;
    public final CachedDoubleValue formulaicAssemblicator;
    public final CachedDoubleValue teleporter;

    StorageConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Machine Energy Storage Config. This config is synced from server to client.").push("storage");

        enrichmentChamber = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("enrichmentChamber", 20_000D));
        osmiumCompressor = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("osmiumCompressor", 80_000D));
        combiner = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("combiner", 40_000D));
        crusher = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("crusher", 20_000D));
        metallurgicInfuser = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("metallurgicInfuser", 20_000D));
        purificationChamber = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("purificationChamber", 80_000D));
        energizedSmelter = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("energizedSmelter", 20_000D));
        digitalMiner = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("digitalMiner", 40_000D));
        electricPump = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("electricPump", 40_000D));
        chargePad = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("chargePad", 40_000D));
        rotaryCondensentrator = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("rotaryCondensentrator", 20_000D));
        oxidationChamber = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("oxidationChamber", 80_000D));
        chemicalInfuser = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("chemicalInfuser", 80_000D));
        chemicalInjectionChamber = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("chemicalInjectionChamber", 160_000D));
        electrolyticSeparator = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("electrolyticSeparator", 160_000D));
        precisionSawmill = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("precisionSawmill", 20_000D));
        chemicalDissolutionChamber = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("chemicalDissolutionChamber", 160_000D));
        chemicalWasher = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("chemicalWasher", 80_000D));
        chemicalCrystallizer = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("chemicalCrystallizer", 160_000D));
        seismicVibrator = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("seismicVibrator", 20_000D));
        pressurizedReactionBase = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("pressurizedReactionBase", 2_000D));
        fluidicPlenisher = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("fluidicPlenisher", 40_000D));
        laser = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("laser", 2_000_000D));
        formulaicAssemblicator = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("formulaicAssemblicator", 40_000D));
        teleporter = CachedDoubleValue.wrap(this, builder.comment("Base energy storage (Joules).")
              .define("teleporter", 5_000_000D));

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