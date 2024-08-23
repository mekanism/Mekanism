package mekanism.common.world.height;

import java.util.function.IntSupplier;
import java.util.function.Supplier;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.config.MekanismConfigTranslations.OreVeinConfigTranslations;
import mekanism.common.config.value.CachedEnumValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.resource.ore.BaseOreConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public record ConfigurableHeightRange(Supplier<HeightShape> shape, ConfigurableVerticalAnchor minInclusive, ConfigurableVerticalAnchor maxInclusive,
                                      IntSupplier plateau) {

    public static ConfigurableHeightRange create(IMekanismConfig config, ModConfigSpec.Builder builder, OreVeinConfigTranslations translations, BaseOreConfig baseConfig) {
        CachedEnumValue<HeightShape> shape = CachedEnumValue.wrap(config, translations.distributionShape().applyToBuilder(builder)
              .defineEnum("shape", baseConfig.shape()));
        CachedIntValue plateau = CachedIntValue.wrap(config, MekanismConfigTranslations.WORLD_HEIGHT_RANGE_PLATEAU.applyToBuilder(builder)
              .define("plateau", baseConfig.plateau(), o -> {
                  if (o instanceof Integer value) {
                      if (value == 0) {
                          return true;
                      }
                      return value > 0 && shape.getOrDefault() == HeightShape.TRAPEZOID;
                  }
                  return false;
              }));
        ConfigurableVerticalAnchor minInclusive = ConfigurableVerticalAnchor.create(config, builder, "minInclusive", translations.minInclusive(), baseConfig.min(), null);
        ConfigurableVerticalAnchor maxInclusive = ConfigurableVerticalAnchor.create(config, builder, "maxInclusive", translations.maxInclusive(), baseConfig.max(), minInclusive);
        return new ConfigurableHeightRange(shape, minInclusive, maxInclusive, plateau);
    }
}