package mekanism.common.config;

import mekanism.common.config.value.CachedBooleanValue;
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
    public final CachedBooleanValue randomizedConsumption;
    public final CachedLongValue teleporterBase;
    public final CachedLongValue teleporterDistance;
    public final CachedLongValue teleporterDimensionPenalty;

    UsageConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        enrichmentChamber = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_ENRICHMENT_CHAMBER, "enrichmentChamber", 5L);
        osmiumCompressor = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_COMPRESSOR, "osmiumCompressor", 15L);
        combiner = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_COMBINER, "combiner", 5L);
        crusher = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_CRUSHER, "crusher", 8L);
        metallurgicInfuser = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_METALLURGIC_INFUSER, "metallurgicInfuser", 5L);
        purificationChamber = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_PURIFICATION_CHAMBER, "purificationChamber", 10L);
        energizedSmelter = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_SMELTER, "energizedSmelter", 5L);
        digitalMiner = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_MINER, "digitalMiner", 40L);
        electricPump = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_PUMP, "electricPump", 2L);
        chargePad = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_CHARGEPAD, "chargepad", 1_024L);
        rotaryCondensentrator = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_CONDENSENTRATOR, "rotaryCondensentrator", 2L);
        chemicalOxidizer = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_OXIDIZER, "chemicalOxidizer", 15L);
        chemicalInfuser = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_CHEMICAL_INFUSER, "chemicalInfuser", 15L);
        chemicalInjectionChamber = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_INJECTION_CHAMBER, "chemicalInjectionChamber", 15L);
        precisionSawmill = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_SAWMILL, "precisionSawmill", 5L);
        chemicalDissolutionChamber = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_CDC, "chemicalDissolutionChamber", 16L);
        chemicalWasher = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_WASHER, "chemicalWasher", 5L);
        chemicalCrystallizer = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_CRYSTALLIZER, "chemicalCrystallizer", 15L);
        seismicVibrator = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_VIBRATOR, "seismicVibrator", 5L);
        pressurizedReactionBase = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_PRC, "pressurizedReactionBase", 2L);
        fluidicPlenisher = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_PLENISHER, "fluidicPlenisher", 2L);
        laser = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_LASER, "laser", 20L);
        formulaicAssemblicator = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_ASSEMBLICATOR, "formulaicAssemblicator", 5L);
        modificationStation = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_MODIFICATION_STATION, "modificationStation", 10L);
        isotopicCentrifuge = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_CENTRIFUGE, "isotopicCentrifuge", 8L);
        nutritionalLiquifier = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_LIQUIFIER, "nutritionalLiquifier", 8L);
        antiprotonicNucleosynthesizer = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_NUCLEOSYNTHESIZER, "antiprotonicNucleosynthesizer", 15L);
        pigmentExtractor = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_PIGMENT_EXTRACTOR, "pigmentExtractor", 5L);
        pigmentMixer = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_PIGMENT_MIXER, "pigmentMixer", 5L);
        paintingMachine = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_PAINTING, "paintingMachine", 5L);
        dimensionalStabilizer = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.ENERGY_USAGE_DIMENSIONAL_STABILIZER, "dimensionalStabilizer", 15L);
        randomizedConsumption = CachedBooleanValue.wrap(this, MekanismConfigTranslations.SECONDARY_CHEMICAL_USAGE_RANDOMIZED.applyToBuilder(builder)
              .worldRestart()
              .define("randomizedConsumption", true));

        MekanismConfigTranslations.USAGE_TELEPORTER.applyToBuilder(builder).push("teleporter");
        teleporterBase = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.USAGE_TELEPORTER_BASE, "base", 100L);
        teleporterDistance = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.USAGE_TELEPORTER_DISTANCE, "distance", 2L);
        teleporterDimensionPenalty = CachedLongValue.definePositive(this, builder, MekanismConfigTranslations.USAGE_TELEPORTER_PENALTY, "dimensionPenalty", 1000L);
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