package mekanism.common.content.transporter;

import mekanism.common.util.InventoryUtils;
import net.minecraft.item.ItemStack;

/**
 * A wrapper of an ItemStack which tests equality and hashes based on item type, damage and NBT
 * data, ignoring stack size.
 * 
 * @author aidancbrady
 *
 */
public class HashedItem {
    private final ItemStack itemStack;
    private final int hashCode;

    public HashedItem(ItemStack stack) {
        itemStack = stack;
        hashCode = initHashCode();
    }

    public ItemStack getStack() {
        return itemStack;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
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
        if (itemStack.hasTagCompound())
            code = 31 * code + itemStack.getTagCompound().hashCode();
        code = 31 * code + itemStack.getItemDamage();
        return code;
    }
}