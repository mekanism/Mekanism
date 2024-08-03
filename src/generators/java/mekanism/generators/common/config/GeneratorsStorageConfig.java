package mekanism.generators.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.config.value.CachedLongValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class GeneratorsStorageConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final CachedLongValue heatGenerator;
    public final CachedLongValue bioGenerator;
    public final CachedLongValue solarGenerator;
    public final CachedLongValue advancedSolarGenerator;
    public final CachedLongValue windGenerator;

    GeneratorsStorageConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        GeneratorsConfigTranslations.STORAGE_TOP_LEVEL.applyToBuilder(builder).push("storage");

        heatGenerator = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "heatGenerator",
              160_000L, 1);
        bioGenerator = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "bioGenerator",
              160_000L, 1);
        solarGenerator = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "solarGenerator",
              96_000L, 1);
        advancedSolarGenerator = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "advancedSolarGenerator",
              200_000L, 1);
        windGenerator = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.BASE_ENERGY_STORAGE_JOULES, "windGenerator",
              200_000L, 1);

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "generator-storage";
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