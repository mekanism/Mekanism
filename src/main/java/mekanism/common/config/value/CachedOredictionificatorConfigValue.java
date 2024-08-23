package mekanism.common.config.value;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public class CachedOredictionificatorConfigValue extends CachedMapConfigValue<String, List<String>> {

    private CachedOredictionificatorConfigValue(IMekanismConfig config, ConfigValue<List<? extends String>> internal) {
        super(config, internal);
    }

    public static CachedOredictionificatorConfigValue define(IMekanismConfig config, ModConfigSpec.Builder builder, String path,
          Supplier<Map<String, List<String>>> defaults) {
        return new CachedOredictionificatorConfigValue(config, builder.defineListAllowEmpty(path,
              () -> encodeStatic(defaults.get(), CachedOredictionificatorConfigValue::encodeStatic),
              () -> "c:ingots/",
              o -> o instanceof String string && ResourceLocation.tryParse(string.toLowerCase(Locale.ROOT)) != null));
    }

    @Override
    protected void resolve(String encoded, Map<String, List<String>> resolved) {
        //We ignore any strings that are invalid resource locations
        // validation should have happened before we got here, but in case something went wrong we don't want to crash and burn
        ResourceLocation rl = ResourceLocation.tryParse(encoded.toLowerCase(Locale.ROOT));
        if (rl != null) {
            resolved.computeIfAbsent(rl.getNamespace(), r -> new ArrayList<>()).add(rl.getPath());
        }
    }

    @Override
    protected void encode(String key, List<String> values, Consumer<String> adder) {
        encodeStatic(key, values, adder);
    }

    private static void encodeStatic(String key, List<String> values, Consumer<String> adder) {
        //Ensure it is all lower case
        String namespace = key.toLowerCase(Locale.ROOT);
        for (String path : values) {
            //Try to create a resource location from it to ensure all characters are valid
            ResourceLocation rl = ResourceLocation.tryBuild(namespace, path.toLowerCase(Locale.ROOT));
            if (rl != null) {
                // if they are, add it
                adder.accept(rl.toString());
            }
        }
    }
}