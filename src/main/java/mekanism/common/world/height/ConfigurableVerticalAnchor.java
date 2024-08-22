package mekanism.common.world.height;

import mekanism.common.config.IConfigTranslation;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.config.value.CachedEnumValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.resource.ore.OreAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import org.jetbrains.annotations.Nullable;

public record ConfigurableVerticalAnchor(CachedEnumValue<AnchorType> anchorType, CachedIntValue value) {

    public static ConfigurableVerticalAnchor create(IMekanismConfig config, ModConfigSpec.Builder builder, String path, IConfigTranslation translation, OreAnchor defaultAnchor,
          @Nullable ConfigurableVerticalAnchor minAnchor) {
        translation.applyToBuilder(builder).push(path);
        CachedEnumValue<AnchorType> type = CachedEnumValue.wrap(config, MekanismConfigTranslations.WORLD_ANCHOR_TYPE.applyToBuilder(builder)
              //Note: Add extra comments so that when viewing from the file, people can know what each option does
              .comment(
                    "Absolute (y = value)",
                    "Above Bottom (y = minY + value)",
                    "Below Top (y = depth - 1 + minY - value)"
              ).defineEnum("type", defaultAnchor.type()));
        ConfigValue<Integer> value;
        if (minAnchor == null) {
            value = MekanismConfigTranslations.WORLD_ANCHOR_VALUE.applyToBuilder(builder).define("value", defaultAnchor.value());
        } else {
            value = MekanismConfigTranslations.WORLD_ANCHOR_VALUE.applyToBuilder(builder).define("value", defaultAnchor.value(), o -> {
                if (o instanceof Integer v) {
                    return minAnchor.anchorType.getOrDefault() != type.getOrDefault() || v >= minAnchor.value.getOrDefault();
                }
                return false;
            });
        }
        builder.pop();
        return new ConfigurableVerticalAnchor(type, CachedIntValue.wrap(config, value));
    }

    public int resolveY(WorldGenerationContext context) {
        return anchorType.get().resolveY(context, value.getAsInt());
    }

    @Override
    public String toString() {
        return switch (anchorType.get()) {
            case ABSOLUTE -> value.getAsInt() + " absolute";
            case ABOVE_BOTTOM -> value.getAsInt() + " above bottom";
            case BELOW_TOP -> value.getAsInt() + " below top";
        };
    }
}