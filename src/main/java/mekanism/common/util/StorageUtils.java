package mekanism.common.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class StorageUtils {

    private StorageUtils() {
    }

    public static int getBarWidth(double ratio) {
        return Math.clamp(Math.round(Item.MAX_BAR_WIDTH * (float) ratio), 0, Item.MAX_BAR_WIDTH);
    }

    public static boolean shouldDisplayBar(ItemStack stack) {
        //TODO - 26.3: Re-evaluate this, we now expose the capability when stacked, so we should potentially have the energy bar display
        //If we are currently stacked, don't display the bar as it will overlap the stack count
        return stack.count() == 1;
    }
}