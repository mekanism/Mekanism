package mekanism.common.util;

import java.util.Map.Entry;
import javax.annotation.Nonnull;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public final class ItemRegistryUtils {

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