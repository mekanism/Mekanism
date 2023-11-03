package mekanism.common.config.value;

import it.unimi.dsi.fastutil.floats.FloatPredicate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public class CachedRL2FloatMapConfigValue extends CachedMapConfigValue<ResourceLocation, Float> {

    private CachedRL2FloatMapConfigValue(IMekanismConfig config, ConfigValue<List<? extends String>> internal) {
        super(config, internal);
    }

    public static CachedRL2FloatMapConfigValue define(IMekanismConfig config, ModConfigSpec.Builder builder, String path, Supplier<Map<ResourceLocation, Float>> defaults) {
        return define(config, builder, path, defaults, f -> true);
    }

    public static CachedRL2FloatMapConfigValue define(IMekanismConfig config, ModConfigSpec.Builder builder, String path, Supplier<Map<ResourceLocation, Float>> defaults,
          FloatPredicate range) {
        return new CachedRL2FloatMapConfigValue(config, builder.defineListAllowEmpty(path,
              () -> encodeStatic(defaults.get(), CachedRL2FloatMapConfigValue::encodeStatic),
              o -> {
                  if (o instanceof String string) {
                      String[] parts = string.split(",", 2);
                      if (parts.length == 2 && ResourceLocation.isValidResourceLocation(parts[0].toLowerCase(Locale.ROOT))) {
                          try {
                              float f = Float.parseFloat(parts[1]);
                              return range.test(f);
                          } catch (NumberFormatException ignored) {
                          }
                      }
                  }
                  return false;
              }));
    }

    @Override
    protected void resolve(String encoded, Map<ResourceLocation, Float> resolved) {
        //We ignore any strings that are invalid, validation should have happened before we got here,
        // but in case something went wrong we don't want to crash and burn
        String[] parts = encoded.split(",", 2);
        if (parts.length == 2) {
            ResourceLocation rl = ResourceLocation.tryParse(parts[0].toLowerCase(Locale.ROOT));
            if (rl != null) {
                try {
                    float value = Float.parseFloat(parts[1]);
                    //First entry wins if for some reason it is there multiple times
                    resolved.putIfAbsent(rl, value);
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    @Override
    protected void encode(ResourceLocation key, Float value, Consumer<String> adder) {
        encodeStatic(key, value, adder);
    }

    private static void encodeStatic(ResourceLocation key, Float value, Consumer<String> adder) {
        if (value != null) {
            //The value should never be null but validate so
            adder.accept(key + "," + value);
        }
    }
}