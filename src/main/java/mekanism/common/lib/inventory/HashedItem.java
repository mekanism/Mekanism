package mekanism.common.lib.inventory;

import java.util.Objects;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IHashedItem;
import mekanism.common.util.StackUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper of an ItemStack which tests equality and hashes based on item type and NBT data, ignoring stack size.
 *
 * @author aidancbrady
 */
@NothingNullByDefault
public class HashedItem implements IHashedItem {

    public static HashedItem create(ItemStack stack) {
        return new HashedItem(stack.copyWithCount(1));
    }

    /**
     * Uses the passed in stack as the raw stack, instead of making a copy of it with a size of one.
     *
     * @apiNote When using this, you should be very careful to not accidentally modify the backing stack, this is mainly for use where we want to use an {@link ItemStack}
     * as a key in a map that is local to a single method, and don't want the overhead of copying the stack when it is not needed.
     */
    public static HashedItem raw(ItemStack stack) {
        return new HashedItem(stack);
    }

    private final ItemStack itemStack;
    private final int hashCode;

    protected HashedItem(ItemStack stack) {
        this.itemStack = stack;
        this.hashCode = initHashCode();
    }

    protected HashedItem(HashedItem other) {
        this(other.itemStack, other.hashCode);
    }

    protected HashedItem(ItemStack stack, int hashCode) {
        this.itemStack = stack;
        this.hashCode = hashCode;
    }

    //TODO: Deprecate in favor of getInternalStack?
    @Deprecated(forRemoval = true, since = "10.3.6")
    public ItemStack getStack() {
        return itemStack;
    }

    @Override
    public ItemStack getInternalStack() {
        return itemStack;
    }

    @Override
    public ItemStack createStack(int size) {
        return StackUtils.size(itemStack, size);
    }

    /**
     * @apiNote Main use is to ensure that this HashedItem is not raw, but to allow skipping recalculating the hash code. This will cause a stack copy if used on an
     * already not-raw HashedItem, so ideally this should only be called on raw stacks and otherwise properly kept track of by the caller.
     */
    public HashedItem recreate() {
        return new HashedItem(createStack(1), hashCode);
    }

    /**
     * Helper to serialize the internal stack to nbt.
     */
    @NotNull
    public CompoundTag internalToNBT() {
        return itemStack.serializeNBT();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return obj instanceof IHashedItem other && ItemHandlerHelper.canItemStacksStack(itemStack, other.getInternalStack());
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private int initHashCode() {
        int code = itemStack.getItem().hashCode();
        if (itemStack.hasTag()) {
            code = 31 * code + itemStack.getTag().hashCode();
        }
        //TODO: Eventually it may be worth also hashing the capability NBT, but as there is no way to access it
        // without reflection we don't do that for now as odds are grabbing it would have more of a performance
        // impact than comparing the cap nbt in equals for the few items from mods that do make use of it
        return code;
    }

    public static class UUIDAwareHashedItem extends HashedItem {

        @Nullable
        private final UUID uuid;
        private final int uuidBasedHash;
        private final boolean overrideHash;

        /**
         * @param uuid Should not be null unless something went wrong reading the packet.
         *
         * @apiNote For use on the client side, hash is taken into account for equals and hashCode
         */
        public UUIDAwareHashedItem(ItemStack stack, @Nullable UUID uuid) {
            super(stack.copyWithCount(1));
            this.uuid = uuid;
            if (this.uuid == null) {
                this.overrideHash = false;
                this.uuidBasedHash = super.hashCode();
            } else {
                this.overrideHash = true;
                this.uuidBasedHash = Objects.hash(super.hashCode(), this.uuid);
            }
        }

        public UUIDAwareHashedItem(HashedItem other, @NotNull UUID uuid) {
            super(other);
            this.uuid = uuid;
            this.uuidBasedHash = super.hashCode();
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
            } else if (overrideHash) {
                //Note: UUID cannot be null if overrideHash is true
                //noinspection DataFlowIssue
                return obj instanceof UUIDAwareHashedItem uuidAware && uuid.equals(uuidAware.uuid) && super.equals(obj);
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return uuidBasedHash;
        }

        /**
         * Converts this to a raw HashedItem that doesn't care about UUID anymore.
         */
        public HashedItem asRawHashedItem() {
            return new HashedItem(this);
        }
    }
}