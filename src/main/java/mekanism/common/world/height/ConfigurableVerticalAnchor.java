package mekanism.common.world.height;

import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedEnumValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.resource.ore.OreAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public record ConfigurableVerticalAnchor(Supplier<AnchorType> anchorType, IntSupplier value) {

    public static ConfigurableVerticalAnchor create(IMekanismConfig config, ForgeConfigSpec.Builder builder, String path, String comment, OreAnchor defaultAnchor,
          @Nullable ConfigurableVerticalAnchor minAnchor) {
        builder.comment(comment).push(path);
        CachedEnumValue<AnchorType> type = CachedEnumValue.wrap(config, builder.comment("Type of anchor.",
              "Absolute -> y = value",
              "Above Bottom -> y = minY + value",
              "Below Top -> y = depth - 1 + minY - value").defineEnum("type", defaultAnchor.type()));
        ForgeConfigSpec.Builder valueBuilder = builder.comment("Value used for calculating y for the anchor based on the type.");
        ConfigValue<Integer> value;
        if (minAnchor == null) {
            value = valueBuilder.define("value", defaultAnchor.value());
        } else {
            value = valueBuilder.define("value", defaultAnchor.value(), o -> {
                if (o instanceof Integer v) {
                    return minAnchor.anchorType.get() != type.get() || v >= minAnchor.value.getAsInt();
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