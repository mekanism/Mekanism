package mekanism.generators.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedLongValue;
import net.minecraft.SharedConstants;
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

        heatGenerator = CachedLongValue.definedMin(this, builder, GeneratorsConfigTranslations.ENERGY_STORAGE_GENERATOR_HEAT, "heatGenerator",
              12 * SharedConstants.TICKS_PER_SECOND, 1);
        bioGenerator = CachedLongValue.definedMin(this, builder, GeneratorsConfigTranslations.ENERGY_STORAGE_GENERATOR_BIO, "bioGenerator",
              30 * SharedConstants.TICKS_PER_SECOND, 1);
        solarGenerator = CachedLongValue.definedMin(this, builder, GeneratorsConfigTranslations.ENERGY_STORAGE_GENERATOR_SOLAR, "solarGenerator",
              2 * SharedConstants.TICKS_PER_SECOND, 1);
        advancedSolarGenerator = CachedLongValue.definedMin(this, builder, GeneratorsConfigTranslations.ENERGY_STORAGE_GENERATOR_SOLAR_ADVANCED, "advancedSolarGenerator",
              12 * SharedConstants.TICKS_PER_SECOND, 1);
        windGenerator = CachedLongValue.definedMin(this, builder, GeneratorsConfigTranslations.ENERGY_STORAGE_GENERATOR_WIND, "windGenerator",
              5 * SharedConstants.TICKS_PER_SECOND, 1);

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
        return Type.SYNCED;
    }
}