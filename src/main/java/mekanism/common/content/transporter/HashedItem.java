package mekanism.common.content.transporter;

import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;

/**
 * A wrapper of an ItemStack which tests equality and hashes based on item type, damage and NBT data, ignoring stack size.
 *
 * @author aidancbrady
 */
public class HashedItem {

    private final ItemStack itemStack;
    private final int hashCode;

    public HashedItem(ItemStack stack) {
        itemStack = StackUtils.size(stack, 1);
        hashCode = initHashCode();
    }

    public ItemStack getStack() {
        return itemStack;
    }

    public ItemStack createStack(int size) {
        return StackUtils.size(itemStack, size);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof HashedItem) {
            HashedItem other = (HashedItem) obj;
            return InventoryUtils.areItemsStackable(itemStack, other.itemStack);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private int initHashCode() {
        int code = 1;
        code = 31 * code + itemStack.getItem().hashCode();
        if (itemStack.hasTag()) {
            code = 31 * code + itemStack.getTag().hashCode();
        }
        return code;
    }
}