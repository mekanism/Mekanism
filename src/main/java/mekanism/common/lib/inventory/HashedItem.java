package mekanism.common.lib.inventory;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;

/**
 * A wrapper of an ItemStack which tests equality and hashes based on item type and NBT data, ignoring stack size.
 *
 * @author aidancbrady
 */
public class HashedItem {

    public static HashedItem create(@Nonnull ItemStack stack) {
        //TODO - 10.1: Evaluate uses of this and potentially switch some over to using raw
        return new HashedItem(StackUtils.size(stack, 1));
    }

    /**
     * Uses the passed in stack as the raw stack, instead of making a copy of it with a size of one.
     *
     * @apiNote When using this, you should be very careful to not accidentally modify the backing stack, this is mainly for use where we want to use an {@link ItemStack}
     * as a key in a map that is local to a single method, and don't want the overhead of copying the stack when it is not needed.
     */
    public static HashedItem raw(@Nonnull ItemStack stack) {
        return new HashedItem(stack);
    }

    @Nonnull
    private final ItemStack itemStack;
    private final int hashCode;

    protected HashedItem(@Nonnull ItemStack stack) {
        this.itemStack = stack;
        this.hashCode = initHashCode();
    }

    protected HashedItem(HashedItem other) {
        this.itemStack = other.itemStack;
        this.hashCode = other.hashCode;
    }

    @Nonnull
    public ItemStack getStack() {
        return itemStack;
    }

    @Nonnull
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

    public static class UUIDAwareHashedItem extends HashedItem {

        private final UUID uuid;
        private final boolean overrideHash;

        /**
         * @apiNote For use on the client side, hash is taken into account for equals and hashCode
         */
        public UUIDAwareHashedItem(ItemStack stack, UUID uuid) {
            super(StackUtils.size(stack, 1));
            this.uuid = uuid;
            this.overrideHash = true;
        }

        public UUIDAwareHashedItem(HashedItem other, UUID uuid) {
            super(other);
            this.uuid = uuid;
            this.overrideHash = false;
        }

        @Nullable
        public UUID getUUID() {
            return uuid;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (overrideHash && uuid != null) {
                return obj instanceof UUIDAwareHashedItem && uuid.equals(((UUIDAwareHashedItem) obj).uuid) && super.equals(obj);
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            if (overrideHash && uuid != null) {
                return 31 * super.hashCode() + uuid.hashCode();
            }
            return super.hashCode();
        }
    }
}