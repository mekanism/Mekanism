package mekanism.api.inventory;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Represents an "item type" for comparing {@link ItemStack ItemStack's} without size.
 *
 * @since 10.3.6
 */
@MethodsReturnNonnullByDefault
public interface IHashedItem {

    /**
     * Gets the internal {@link ItemStack} that backs this item type. It is <strong>IMPORTANT</strong> to not modify the returned value.
     *
     * @return The internal {@link ItemStack} that backs this item type.
     *
     * @apiNote Do not modify the returned value. This is only exposed for cases where the stack is needed for performance reasons and mutation is not needed.
     */
    ItemStack getInternalStack();

    /**
     * Creates a mutable {@link ItemStack} of this type with the given size.
     *
     * @param size Size of the stack to create.
     *
     * @return A new {@link ItemStack} of this type.
     */
    ItemStack createStack(int size);

    /**
     * Helper to get the {@link Item} that this item type represents.
     *
     * @return The {@link Item} that this item type represents.
     */
    default Item getItem() {
        return getInternalStack().getItem();
    }

    /**
     * Helper to get the holder for the {@link Item} that this item type represents.
     *
     * @return The holder for the {@link Item} that this item type represents.
     * @since 10.7.11
     */
    default Holder<Item> getItemHolder() {
        return getInternalStack().getItemHolder();
    }

    /**
     * Helper to get the max stack size of the {@link Item} that this item type represents.
     *
     * @return Max stack size of the {@link Item} that this item type represents.
     */
    default int getMaxStackSize() {
        return getInternalStack().getMaxStackSize();
    }
}