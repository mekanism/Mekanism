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
    public final CachedLongValue chargepad;
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

        enrichmentChamber = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_ENRICHMENT_CHAMBER, "enrichmentChamber",
              20_000L, 1);
        osmiumCompressor = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_COMPRESSOR, "osmiumCompressor",
              80_000L, 1);
        combiner = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_COMBINER, "combiner",
              40_000L, 1);
        crusher = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_CRUSHER, "crusher",
              20_000L, 1);
        metallurgicInfuser = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_METALLURGIC_INFUSER, "metallurgicInfuser",
              20_000L, 1);
        purificationChamber = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_PURIFICATION_CHAMBER, "purificationChamber",
              80_000L, 1);
        energizedSmelter = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_SMELTER, "energizedSmelter",
              20_000L, 1);
        digitalMiner = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_MINER, "digitalMiner",
              50_000L, 1);
        electricPump = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_PUMP, "electricPump",
              40_000L, 1);
        chargepad = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_CHARGEPAD, "chargepad", 2_048_000L, 1);
        rotaryCondensentrator = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_CONDENSENTRATOR, "rotaryCondensentrator",
              20_000L, 1);
        chemicalOxidizer = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_OXIDIZER, "chemicalOxidizer",
              80_000L, 1);
        chemicalInfuser = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_CHEMICAL_INFUSER, "chemicalInfuser",
              80_000L, 1);
        chemicalInjectionChamber = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_INJECTION_CHAMBER, "chemicalInjectionChamber",
              160_000L, 1);
        electrolyticSeparator = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_SEPARATOR, "electrolyticSeparator",
              160_000L, 1);
        precisionSawmill = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_SAWMILL, "precisionSawmill",
              20_000L, 1);
        chemicalDissolutionChamber = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_CDC, "chemicalDissolutionChamber",
              160_000L, 1);
        chemicalWasher = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_WASHER, "chemicalWasher",
              80_000L, 1);
        chemicalCrystallizer = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_CRYSTALLIZER, "chemicalCrystallizer",
              160_000L, 1);
        seismicVibrator = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_VIBRATOR, "seismicVibrator",
              20_000L, 1);
        pressurizedReactionBase = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_PRC, "pressurizedReactionBase",
              2_000L, 1);
        fluidicPlenisher = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_PLENISHER, "fluidicPlenisher",
              40_000L, 1);
        laser = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_LASER, "laser",
              2_000_000L, 1);
        laserAmplifier = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_LASER_AMPLIFIER, "laserAmplifier",
              5_000_000_000L, 1);
        laserTractorBeam = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_TRACTOR_BEAM, "laserTractorBeam",
              5_000_000_000L, 1);
        formulaicAssemblicator = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_ASSEMBLICATOR, "formulaicAssemblicator",
              40_000L, 1);
        teleporter = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_TELEPORTER, "teleporter",
              5_000_000L, 1);
        modificationStation = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_MODIFICATION_STATION, "modificationStation",
              40_000L, 1);
        isotopicCentrifuge = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_CENTRIFUGE, "isotopicCentrifuge",
              80_000L, 1);
        nutritionalLiquifier = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_LIQUIFIER, "nutritionalLiquifier",
              40_000L, 1);
        antiprotonicNucleosynthesizer = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_NUCLEOSYNTHESIZER, "antiprotonicNucleosynthesizer",
              1_000_000_000L, 1);
        pigmentExtractor = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_PIGMENT_EXTRACTOR, "pigmentExtractor",
              40_000L, 1);
        pigmentMixer = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_PIGMENT_MIXER, "pigmentMixer",
              80_000L, 1);
        paintingMachine = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_PAINTING, "paintingMachine",
              40_000L, 1);
        spsPort = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_SPS_PORT, "spsPort",
              1_000_000_000L, 1);
        dimensionalStabilizer = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.ENERGY_STORAGE_DIMENSIONAL_STABILIZER, "dimensionalStabilizer",
              40_000L, 1);

        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "machine-storage";
    }


    @Override
    public String getTranslation() {
        return "Storage Config";
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