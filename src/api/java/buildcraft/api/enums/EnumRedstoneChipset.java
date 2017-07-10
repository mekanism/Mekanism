package buildcraft.api.enums;

import java.util.Locale;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import buildcraft.api.BCItems;

public enum EnumRedstoneChipset implements IStringSerializable {
    RED,
    IRON,
    GOLD,
    QUARTZ,
    DIAMOND;

    private final String name = name().toLowerCase(Locale.ROOT);

    public ItemStack getStack(int stackSize) {
        Item chipset = BCItems.SILICON_REDSTONE_CHIPSET;
        if (chipset == null) {
            return ItemStack.EMPTY;
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

    @Override
    public String getName() {
        return name;
    }
}
