package mekanism.common.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.lang3.text.WordUtils;

public final class ItemRegistryUtils {

    private static final Map<String, String> modIDMap = new HashMap<>();

    private static void populateMap() {
        for (Entry<String, ModContainer> entry : Loader.instance().getIndexedModList().entrySet()) {
            modIDMap.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue().getName());
        }
    }

    /* Mod ID lookup thanks to JEI */
    public static String getMod(ItemStack stack) {
        if (stack.isEmpty()) {
            return "null";
        }

        if (modIDMap.isEmpty()) {
            populateMap();
        }

        ResourceLocation itemResourceLocation = stack.getItem().getRegistryName();

        if (itemResourceLocation == null) {
            return "null";
        }

        String modId = itemResourceLocation.getNamespace();
        String lowercaseModId = modId.toLowerCase(Locale.ENGLISH);
        return modIDMap.computeIfAbsent(lowercaseModId, k -> WordUtils.capitalize(modId));
    }
}