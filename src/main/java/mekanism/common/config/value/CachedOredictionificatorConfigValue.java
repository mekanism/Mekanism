package mekanism.common.config.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedOredictionificatorConfigValue extends CachedResolvableConfigValue<Map<String, List<String>>, List<? extends String>> {

    private CachedOredictionificatorConfigValue(IMekanismConfig config, ConfigValue<List<? extends String>> internal) {
        super(config, internal);
    }

    public static CachedOredictionificatorConfigValue define(IMekanismConfig config, ForgeConfigSpec.Builder builder, String path,
          Supplier<Map<String, List<String>>> defaults) {
        return new CachedOredictionificatorConfigValue(config, builder.defineListAllowEmpty(Collections.singletonList(path), () -> encodeStatic(defaults.get()), o -> {
            if (o instanceof String string) {
                return ResourceLocation.tryParse(string.toLowerCase(Locale.ROOT)) != null;
            }
            return false;
        }));
    }

    @Override
    protected Map<String, List<String>> resolve(List<? extends String> encoded) {
        //We ignore any strings that are invalid resource locations
        // validation should have happened before we got here, but in case something went wrong we don't want to crash and burn
        Map<String, List<String>> resolved = new HashMap<>(encoded.size());
        for (String s : encoded) {
            ResourceLocation rl = ResourceLocation.tryParse(s.toLowerCase(Locale.ROOT));
            if (rl != null) {
                resolved.computeIfAbsent(rl.getNamespace(), r -> new ArrayList<>()).add(rl.getPath());
            }
        }
        return resolved;
    }

    @Override
    protected List<? extends String> encode(Map<String, List<String>> values) {
        return encodeStatic(values);
    }

    private static List<? extends String> encodeStatic(Map<String, List<String>> values) {
        List<String> encoded = new ArrayList<>(values.size());
        for (Map.Entry<String, List<String>> entry : values.entrySet()) {
            //Ensure it is all lower case
            String namespace = entry.getKey().toLowerCase(Locale.ROOT);
            for (String path : entry.getValue()) {
                try {
                    //Try to create a resource location from it to ensure all characters are valid
                    ResourceLocation rl = new ResourceLocation(namespace + ":" + path.toLowerCase(Locale.ROOT));
                    // if they are, add it
                    encoded.add(rl.toString());
                } catch (ResourceLocationException ignored) {
                }
            }
        }
        //Sort it so that it is deterministic
        Collections.sort(encoded);
        return encoded;
    }
}