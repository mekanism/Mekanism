package mekanism.common.config.value;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedOredictionificatorConfigValue extends CachedMapConfigValue<String, List<String>> {

    private CachedOredictionificatorConfigValue(IMekanismConfig config, ConfigValue<List<? extends String>> internal) {
        super(config, internal);
    }

    public static CachedOredictionificatorConfigValue define(IMekanismConfig config, ForgeConfigSpec.Builder builder, String path,
          Supplier<Map<String, List<String>>> defaults) {
        return new CachedOredictionificatorConfigValue(config, builder.defineListAllowEmpty(path,
              () -> encodeStatic(defaults.get(), CachedOredictionificatorConfigValue::encodeStatic),
              o -> o instanceof String string && ResourceLocation.isValidResourceLocation(string.toLowerCase(Locale.ROOT))));
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
            try {
                //Try to create a resource location from it to ensure all characters are valid
                ResourceLocation rl = new ResourceLocation(namespace, path.toLowerCase(Locale.ROOT));
                // if they are, add it
                adder.accept(rl.toString());
            } catch (ResourceLocationException ignored) {
            }
        }
    }
}