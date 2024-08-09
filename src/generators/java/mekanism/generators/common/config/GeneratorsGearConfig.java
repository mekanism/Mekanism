package mekanism.generators.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.GearConfig;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedLongValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class GeneratorsGearConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    //MekaSuit
    public final CachedLongValue mekaSuitGeothermalChargingRate;
    public final CachedFloatValue mekaSuitHeatDamageReductionRatio;

    GeneratorsGearConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        MekanismConfigTranslations.GEAR_MEKA_SUIT.applyToBuilder(builder).push(GearConfig.MEKASUIT_CATEGORY);
        mekaSuitGeothermalChargingRate = CachedLongValue.wrap(this, GeneratorsConfigTranslations.GEAR_MEKA_SUIT_GEOTHERMAL.applyToBuilder(builder)
              .defineInRange("geothermalChargingRate", 10L, 0, Long.MAX_VALUE / 8));

        MekanismConfigTranslations.GEAR_MEKA_SUIT_DAMAGE_ABSORPTION.applyToBuilder(builder).push(GearConfig.MEKASUIT_DAMAGE_CATEGORY);
        mekaSuitHeatDamageReductionRatio = CachedFloatValue.wrap(this, GeneratorsConfigTranslations.GEAR_MEKA_SUIT_HEAT_DAMAGE.applyToBuilder(builder)
              .defineInRange("heatDamageReductionRatio", 0.8, 0, 1));
        builder.pop(2);

        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "generators-gear";
    }

    @Override
    public String getTranslation() {
        return "Gear Config";
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