package mekanism.generators.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedLongValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class GeneratorsGearConfig extends BaseMekanismConfig {

    private static final String MEKASUIT_CATEGORY = "mekasuit";
    private static final String MEKASUIT_DAMAGE_CATEGORY = "damage_absorption";

    private final ModConfigSpec configSpec;

    //MekaSuit
    public final CachedLongValue mekaSuitGeothermalChargingRate;
    public final CachedFloatValue mekaSuitHeatDamageReductionRatio;

    GeneratorsGearConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        GeneratorsConfigTranslations.GEAR_TOP_LEVEL.applyToBuilder(builder).push("generators-gear");

        GeneratorsConfigTranslations.GEAR_MEKA_SUIT.applyToBuilder(builder).push(MEKASUIT_CATEGORY);
        mekaSuitGeothermalChargingRate = CachedLongValue.wrap(this, GeneratorsConfigTranslations.GEAR_MEKA_SUIT_GEOTHERMAL.applyToBuilder(builder)
              .defineInRange("geothermalChargingRate", 10L, 0, Long.MAX_VALUE / 8));
        builder.push(MEKASUIT_DAMAGE_CATEGORY);
        mekaSuitHeatDamageReductionRatio = CachedFloatValue.wrap(this, GeneratorsConfigTranslations.GEAR_MEKA_SUIT_HEAT_DAMAGE.applyToBuilder(builder)
              .defineInRange("heatDamageReductionRatio", 0.8, 0, 1));
        builder.pop(2);

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "generators-gear";
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