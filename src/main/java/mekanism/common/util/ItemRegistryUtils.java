package mekanism.common.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.registries.ForgeRegistries;
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

    @Nonnull
    public static Item getByName(String name) {
        name = name.toLowerCase();
        ResourceLocation resource = new ResourceLocation(name);
        Item value = ForgeRegistries.ITEMS.getValue(resource);
        if (value != null) {
            return value;
        }
        for (Entry<ResourceLocation, Item> entry : ForgeRegistries.ITEMS.getEntries()) {
            if (entry.getKey().getPath().equals(name)) {
                return entry.getValue();
            }
        }
        return Items.AIR;
    }
}