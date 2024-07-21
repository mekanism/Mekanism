package mekanism.common.config;

import mekanism.common.config.value.CachedLongValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class StorageConfig extends BaseMekanismConfig {

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
    public final CachedLongValue electrolyticSeparator;
    public final CachedLongValue precisionSawmill;
    public final CachedLongValue chemicalDissolutionChamber;
    public final CachedLongValue chemicalWasher;
    public final CachedLongValue chemicalCrystallizer;
    public final CachedLongValue seismicVibrator;
    public final CachedLongValue pressurizedReactionBase;
    public final CachedLongValue fluidicPlenisher;
    public final CachedLongValue laser;
    public final CachedLongValue laserAmplifier;
    public final CachedLongValue laserTractorBeam;
    public final CachedLongValue formulaicAssemblicator;
    public final CachedLongValue teleporter;
    public final CachedLongValue modificationStation;
    public final CachedLongValue isotopicCentrifuge;
    public final CachedLongValue nutritionalLiquifier;
    public final CachedLongValue antiprotonicNucleosynthesizer;
    public final CachedLongValue pigmentExtractor;
    public final CachedLongValue pigmentMixer;
    public final CachedLongValue paintingMachine;
    public final CachedLongValue spsPort;
    public final CachedLongValue dimensionalStabilizer;

    StorageConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Machine Energy Storage Config. This config is synced from server to client.").push("storage");

        enrichmentChamber = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "enrichmentChamber",
              20_000L, 1);
        osmiumCompressor = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "osmiumCompressor",
              80_000L, 1);
        combiner = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "combiner",
              40_000L, 1);
        crusher = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "crusher",
              20_000L, 1);
        metallurgicInfuser = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "metallurgicInfuser",
              20_000L, 1);
        purificationChamber = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "purificationChamber",
              80_000L, 1);
        energizedSmelter = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "energizedSmelter",
              20_000L, 1);
        digitalMiner = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "digitalMiner",
              50_000L, 1);
        electricPump = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "electricPump",
              40_000L, 1);
        chargePad = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "chargePad", 2_048_000L, 1);
        rotaryCondensentrator = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "rotaryCondensentrator",
              20_000L, 1);
        chemicalOxidizer = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "chemicalOxidizer",
              80_000L, 1);
        chemicalInfuser = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "chemicalInfuser",
              80_000L, 1);
        chemicalInjectionChamber = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "chemicalInjectionChamber",
              160_000L, 1);
        electrolyticSeparator = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "electrolyticSeparator",
              160_000L, 1);
        precisionSawmill = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "precisionSawmill",
              20_000L, 1);
        chemicalDissolutionChamber = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "chemicalDissolutionChamber",
              160_000L, 1);
        chemicalWasher = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "chemicalWasher",
              80_000L, 1);
        chemicalCrystallizer = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "chemicalCrystallizer",
              160_000L, 1);
        seismicVibrator = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "seismicVibrator",
              20_000L, 1);
        pressurizedReactionBase = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "pressurizedReactionBase",
              2_000L, 1);
        fluidicPlenisher = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "fluidicPlenisher",
              40_000L, 1);
        laser = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "laser",
              2_000_000L, 1);
        laserAmplifier = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "laserAmplifier",
              5_000_000_000L, 1);
        laserTractorBeam = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "laserTractorBeam",
              5_000_000_000L, 1);
        formulaicAssemblicator = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "formulaicAssemblicator",
              40_000L, 1);
        teleporter = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "teleporter",
              5_000_000L, 1);
        modificationStation = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "modificationStation",
              40_000L, 1);
        isotopicCentrifuge = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "isotopicCentrifuge",
              80_000L, 1);
        nutritionalLiquifier = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "nutritionalLiquifier",
              40_000L, 1);
        antiprotonicNucleosynthesizer = CachedLongValue.definedMin(this, builder, "Base energy storage (Joules). Also defines max process rate.", "antiprotonicNucleosynthesizer",
              1_000_000_000L, 1);
        pigmentExtractor = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "pigmentExtractor",
              40_000L, 1);
        pigmentMixer = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "pigmentMixer",
              80_000L, 1);
        paintingMachine = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "paintingMachine",
              40_000L, 1);
        spsPort = CachedLongValue.definedMin(this, builder, "Base energy storage (Joules). Also defines max output rate.", "spsPort",
              1_000_000_000L, 1);
        dimensionalStabilizer = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "dimensionalStabilizer",
              40_000L, 1);

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "machine-storage";
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