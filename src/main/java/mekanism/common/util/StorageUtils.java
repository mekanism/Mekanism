package mekanism.common.util;

import net.minecraft.world.item.Item;

public class StorageUtils {

    private StorageUtils() {
    }

    public static int getBarWidth(double ratio) {
        return Math.clamp(Math.round(Item.MAX_BAR_WIDTH * (float) ratio), 0, Item.MAX_BAR_WIDTH);
    }
}