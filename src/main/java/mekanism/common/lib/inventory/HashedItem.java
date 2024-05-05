package mekanism.common.lib.inventory;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IHashedItem;
import mekanism.common.util.StackUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper of an ItemStack which tests equality and hashes based on item type and NBT data, ignoring stack size.
 *
 * @author aidancbrady
 */
@NothingNullByDefault
public class HashedItem implements IHashedItem {

    /**
     * @implNote This codec does not copy any uuid information if the hashed item is a {@link UUIDAwareHashedItem}
     */
    public static final Codec<HashedItem> CODEC = ItemStack.CODEC.xmap(HashedItem::raw, HashedItem::getInternalStack);
    /**
     * @implNote This codec does not copy any uuid information if the hashed item is a {@link UUIDAwareHashedItem}
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, HashedItem> STREAM_CODEC = ItemStack.STREAM_CODEC.map(HashedItem::raw, HashedItem::getInternalStack);

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
        this.hashCode = ItemStack.hashItemAndComponents(itemStack);
    }

    protected HashedItem(HashedItem other) {
        this(other.itemStack, other.hashCode);
    }

    protected HashedItem(ItemStack stack, int hashCode) {
        this.itemStack = stack;
        this.hashCode = hashCode;
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
    public Tag internalToNBT(HolderLookup.Provider provider) {
        return itemStack.save(provider);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return obj instanceof IHashedItem other && ItemStack.isSameItemSameComponents(itemStack, other.getInternalStack());
    }

    @Override
    public int hashCode() {
        return hashCode;
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