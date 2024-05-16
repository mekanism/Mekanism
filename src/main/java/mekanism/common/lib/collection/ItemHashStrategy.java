package mekanism.common.lib.collection;

import it.unimi.dsi.fastutil.Hash.Strategy;
import net.minecraft.world.item.ItemStack;

public class ItemHashStrategy implements Strategy<ItemStack> {

    public static final ItemHashStrategy INSTANCE = new ItemHashStrategy();

    private ItemHashStrategy() {
    }

    @Override
    public int hashCode(ItemStack stack) {
        return stack == null ? 0 : ItemStack.hashItemAndComponents(stack);
    }

    @Override
    public boolean equals(ItemStack a, ItemStack b) {
        if (a == b) {
            return true;
        } else if (a == null || b == null) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(a, b);
    }
}