package mekanism.common.config;

import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedEnumValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class LocalConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final CachedEnumValue<TemperatureUnit> tempUnit;
    public final CachedLongValue compactEnergyThreshold;
    public final CachedBooleanValue enableDecayTimers;
    public final CachedBooleanValue copyBlockData;
    public final CachedBooleanValue holidays;

    LocalConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        //Default to only displaying energy in compact form for 10,000 and higher. Note we cut off the config at 1,000 as there is no reason to try and compact values smaller than that
        compactEnergyThreshold = CachedLongValue.definedMin(this, builder, MekanismConfigTranslations.COMMON_COMPACT_DISPLAY_ENERGY_THRESHOLD,
              "compactEnergyThreshold", 10_000, 1_000);
        tempUnit = CachedEnumValue.wrap(this, MekanismConfigTranslations.COMMON_UNIT_TEMPERATURE.applyToBuilder(builder)
              .defineEnum("temperatureUnit", TemperatureUnit.KELVIN));
        enableDecayTimers = CachedBooleanValue.wrap(this, MekanismConfigTranslations.COMMON_DECAY_TIMERS.applyToBuilder(builder)
              .define("enableDecayTimers", true));
        copyBlockData = CachedBooleanValue.wrap(this, MekanismConfigTranslations.COMMON_COPY_BLOCK_DATA.applyToBuilder(builder)
              .define("copyBlockData", true));
        holidays = CachedBooleanValue.wrap(this, MekanismConfigTranslations.COMMON_HOLIDAYS.applyToBuilder(builder)
              .define("holidays", true));

        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "local";
    }

    @Override
    public String getTranslation() {
        return "Local Config";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.LOCAL;
    }
}