package mekanism.common.config;

import mekanism.common.config.value.CachedLongValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class UsageConfig extends BaseMekanismConfig {

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

        enrichmentChamber = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_ENRICHMENT_CHAMBER, "enrichmentChamber", 50L);
        osmiumCompressor = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_COMPRESSOR, "osmiumCompressor", 100L);
        combiner = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_COMBINER, "combiner", 50L);
        crusher = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_CRUSHER, "crusher", 50L);
        metallurgicInfuser = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_METALLURGIC_INFUSER, "metallurgicInfuser", 50L);
        purificationChamber = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_PURIFICATION_CHAMBER, "purificationChamber", 200L);
        energizedSmelter = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_SMELTER, "energizedSmelter", 50L);
        digitalMiner = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_MINER, "digitalMiner", 1_000L);
        electricPump = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_PUMP, "electricPump", 100L);
        chargePad = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_CHARGEPAD, "chargepad", 1_024_000L);
        rotaryCondensentrator = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_CONDENSENTRATOR, "rotaryCondensentrator", 50L);
        chemicalOxidizer = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_OXIDIZER, "chemicalOxidizer", 200L);
        chemicalInfuser = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_CHEMICAL_INFUSER, "chemicalInfuser", 200L);
        chemicalInjectionChamber = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_INJECTION_CHAMBER, "chemicalInjectionChamber", 400L);
        precisionSawmill = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_SAWMILL, "precisionSawmill", 50L);
        chemicalDissolutionChamber = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_CDC, "chemicalDissolutionChamber", 400L);
        chemicalWasher = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_WASHER, "chemicalWasher", 200L);
        chemicalCrystallizer = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_CRYSTALLIZER, "chemicalCrystallizer", 400L);
        seismicVibrator = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_VIBRATOR, "seismicVibrator", 50L);
        pressurizedReactionBase = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_PRC, "pressurizedReactionBase", 5L);
        fluidicPlenisher = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_PLENISHER, "fluidicPlenisher", 100L);
        laser = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_LASER, "laser", 10_000L);
        formulaicAssemblicator = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_ASSEMBLICATOR, "formulaicAssemblicator", 100L);
        modificationStation = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_MODIFICATION_STATION, "modificationStation", 400L);
        isotopicCentrifuge = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_CENTRIFUGE, "isotopicCentrifuge", 200L);
        nutritionalLiquifier = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_LIQUIFIER, "nutritionalLiquifier", 200L);
        antiprotonicNucleosynthesizer = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_NUCLEOSYNTHESIZER, "antiprotonicNucleosynthesizer", 100_000L);
        pigmentExtractor = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_PIGMENT_EXTRACTOR, "pigmentExtractor", 200L);
        pigmentMixer = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_PIGMENT_MIXER, "pigmentMixer", 200L);
        paintingMachine = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_PAINTING, "paintingMachine", 100L);
        dimensionalStabilizer = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_DIMENSIONAL_STABILIZER, "dimensionalStabilizer", 5_000L);

        MekanismConfigTranslations.USAGE_TELEPORTER.applyToBuilder(builder).push("teleporter");
        teleporterBase = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.USAGE_TELEPORTER_BASE, "base", 1_000L);
        teleporterDistance = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.USAGE_TELEPORTER_DISTANCE, "distance", 10L);
        teleporterDimensionPenalty = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.USAGE_TELEPORTER_PENALTY, "dimensionPenalty", 10_000L);
        builder.pop();

        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "machine-usage";
    }


    @Override
    public String getTranslation() {
        return "Usage Config";
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