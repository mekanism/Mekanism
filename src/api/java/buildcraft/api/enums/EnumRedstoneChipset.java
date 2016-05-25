package buildcraft.api.enums;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum EnumRedstoneChipset {
    RED,
    IRON,
    GOLD,
    DIAMOND,
    PULSATING,
    QUARTZ,
    COMP,
    EMERALD;

    public ItemStack getStack(int stackSize) {
        Item chipset = Item.getByNameOrId("buildcraft|silicon:redstoneChipset");
        if (chipset == null) {
            return null;
        }

        return new ItemStack(chipset, stackSize, ordinal());
    }

    public ItemStack getStack() {
        return getStack(1);
    }

    public static EnumRedstoneChipset fromStack(ItemStack stack) {
        if (stack == null) {
            return RED;
        }
        return fromOrdinal(stack.getMetadata());
    }

    public static EnumRedstoneChipset fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            return RED;
        }
        return values()[ordinal];
    }
}
