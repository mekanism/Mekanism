package mekanism.common.config;

import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedIntValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class UsageConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final CachedIntValue enrichmentChamber;
    public final CachedIntValue osmiumCompressor;
    public final CachedIntValue combiner;
    public final CachedIntValue crusher;
    public final CachedIntValue metallurgicInfuser;
    public final CachedIntValue purificationChamber;
    public final CachedIntValue energizedSmelter;
    public final CachedIntValue digitalMiner;
    public final CachedIntValue electricPump;
    public final CachedIntValue chargePad;
    public final CachedIntValue rotaryCondensentrator;
    public final CachedIntValue chemicalOxidizer;
    public final CachedIntValue chemicalInfuser;
    public final CachedIntValue chemicalInjectionChamber;
    public final CachedIntValue precisionSawmill;
    public final CachedIntValue chemicalDissolutionChamber;
    public final CachedIntValue chemicalWasher;
    public final CachedIntValue chemicalCrystallizer;
    public final CachedIntValue seismicVibrator;
    public final CachedIntValue pressurizedReactionBase;
    public final CachedIntValue fluidicPlenisher;
    public final CachedIntValue laser;
    public final CachedIntValue formulaicAssemblicator;
    public final CachedIntValue modificationStation;
    public final CachedIntValue isotopicCentrifuge;
    public final CachedIntValue nutritionalLiquifier;
    public final CachedIntValue antiprotonicNucleosynthesizer;
    public final CachedIntValue pigmentExtractor;
    public final CachedIntValue pigmentMixer;
    public final CachedIntValue paintingMachine;
    public final CachedIntValue dimensionalStabilizer;
    public final CachedBooleanValue randomizedConsumption;
    public final CachedIntValue teleporterBase;
    public final CachedIntValue teleporterDistance;
    public final CachedIntValue teleporterDimensionPenalty;

    UsageConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        enrichmentChamber = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_ENRICHMENT_CHAMBER.applyToBuilder(builder)
              .defineInRange("enrichmentChamber", 5, 0, Integer.MAX_VALUE));
        osmiumCompressor = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_COMPRESSOR.applyToBuilder(builder)
              .defineInRange("osmiumCompressor", 15, 0, Integer.MAX_VALUE));
        combiner = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_COMBINER.applyToBuilder(builder)
              .defineInRange("combiner", 5, 0, Integer.MAX_VALUE));
        crusher = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_CRUSHER.applyToBuilder(builder)
              .defineInRange("crusher", 8, 0, Integer.MAX_VALUE));
        metallurgicInfuser = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_METALLURGIC_INFUSER.applyToBuilder(builder)
              .defineInRange("metallurgicInfuser", 5, 0, Integer.MAX_VALUE));
        purificationChamber = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_PURIFICATION_CHAMBER.applyToBuilder(builder)
              .defineInRange("purificationChamber", 10, 0, Integer.MAX_VALUE));
        energizedSmelter = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_SMELTER.applyToBuilder(builder)
              .defineInRange("energizedSmelter", 5, 0, Integer.MAX_VALUE));
        digitalMiner = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_MINER.applyToBuilder(builder)
              .defineInRange("digitalMiner", 40, 0, Integer.MAX_VALUE));
        electricPump = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_PUMP.applyToBuilder(builder)
              .defineInRange("electricPump", 2, 0, Integer.MAX_VALUE));
        chargePad = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_CHARGEPAD.applyToBuilder(builder)
              .defineInRange("chargepad", 1_024, 0, Integer.MAX_VALUE));
        rotaryCondensentrator = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_CONDENSENTRATOR.applyToBuilder(builder)
              .defineInRange("rotaryCondensentrator", 2, 0, Integer.MAX_VALUE));
        chemicalOxidizer = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_OXIDIZER.applyToBuilder(builder)
              .defineInRange("chemicalOxidizer", 15, 0, Integer.MAX_VALUE));
        chemicalInfuser = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_CHEMICAL_INFUSER.applyToBuilder(builder)
              .defineInRange("chemicalInfuser", 15, 0, Integer.MAX_VALUE));
        chemicalInjectionChamber = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_INJECTION_CHAMBER.applyToBuilder(builder)
              .defineInRange("chemicalInjectionChamber", 15, 0, Integer.MAX_VALUE));
        precisionSawmill = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_SAWMILL.applyToBuilder(builder)
              .defineInRange("precisionSawmill", 5, 0, Integer.MAX_VALUE));
        chemicalDissolutionChamber = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_CDC.applyToBuilder(builder)
              .defineInRange("chemicalDissolutionChamber", 16, 0, Integer.MAX_VALUE));
        chemicalWasher = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_WASHER.applyToBuilder(builder)
              .defineInRange("chemicalWasher", 5, 0, Integer.MAX_VALUE));
        chemicalCrystallizer = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_CRYSTALLIZER.applyToBuilder(builder)
              .defineInRange("chemicalCrystallizer", 15, 0, Integer.MAX_VALUE));
        seismicVibrator = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_VIBRATOR.applyToBuilder(builder)
              .defineInRange("seismicVibrator", 5, 0, Integer.MAX_VALUE));
        pressurizedReactionBase = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_PRC.applyToBuilder(builder)
              .defineInRange("pressurizedReactionBase", 2, 0, Integer.MAX_VALUE));
        fluidicPlenisher = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_PLENISHER.applyToBuilder(builder)
              .defineInRange("fluidicPlenisher", 2, 0, Integer.MAX_VALUE));
        laser = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_LASER.applyToBuilder(builder)
              .defineInRange("laser", 20, 0, Integer.MAX_VALUE));
        formulaicAssemblicator = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_ASSEMBLICATOR.applyToBuilder(builder)
              .defineInRange("formulaicAssemblicator", 5, 0, Integer.MAX_VALUE));
        modificationStation = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_MODIFICATION_STATION.applyToBuilder(builder)
              .defineInRange("modificationStation", 10, 0, Integer.MAX_VALUE));
        isotopicCentrifuge = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_CENTRIFUGE.applyToBuilder(builder)
              .defineInRange("isotopicCentrifuge", 8, 0, Integer.MAX_VALUE));
        nutritionalLiquifier = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_LIQUIFIER.applyToBuilder(builder)
              .defineInRange("nutritionalLiquifier", 8, 0, Integer.MAX_VALUE));
        antiprotonicNucleosynthesizer = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_NUCLEOSYNTHESIZER.applyToBuilder(builder)
              .defineInRange("antiprotonicNucleosynthesizer", 15, 0, Integer.MAX_VALUE));
        pigmentExtractor = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_PIGMENT_EXTRACTOR.applyToBuilder(builder)
              .defineInRange("pigmentExtractor", 5, 0, Integer.MAX_VALUE));
        pigmentMixer = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_PIGMENT_MIXER.applyToBuilder(builder)
              .defineInRange("pigmentMixer", 5, 0, Integer.MAX_VALUE));
        paintingMachine = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_PAINTING.applyToBuilder(builder)
              .defineInRange("paintingMachine", 5, 0, Integer.MAX_VALUE));
        dimensionalStabilizer = CachedIntValue.wrap(this, MekanismConfigTranslations.ENERGY_USAGE_DIMENSIONAL_STABILIZER.applyToBuilder(builder)
              .defineInRange("dimensionalStabilizer", 15, 0, Integer.MAX_VALUE));
        randomizedConsumption = CachedBooleanValue.wrap(this, MekanismConfigTranslations.SECONDARY_CHEMICAL_USAGE_RANDOMIZED.applyToBuilder(builder)
              .worldRestart()
              .define("randomizedConsumption", true));

        MekanismConfigTranslations.USAGE_TELEPORTER.applyToBuilder(builder).push("teleporter");
        teleporterBase = CachedIntValue.wrap(this, MekanismConfigTranslations.USAGE_TELEPORTER_BASE.applyToBuilder(builder)
              .defineInRange("base", 100, 0, Integer.MAX_VALUE));
        teleporterDistance = CachedIntValue.wrap(this, MekanismConfigTranslations.USAGE_TELEPORTER_DISTANCE.applyToBuilder(builder)
              .defineInRange("distance", 2, 0, Integer.MAX_VALUE));
        teleporterDimensionPenalty = CachedIntValue.wrap(this, MekanismConfigTranslations.USAGE_TELEPORTER_PENALTY.applyToBuilder(builder)
              .defineInRange("dimensionPenalty", 1000, 0, Integer.MAX_VALUE));
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