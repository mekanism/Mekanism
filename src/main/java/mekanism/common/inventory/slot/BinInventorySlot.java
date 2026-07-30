package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.ResourceContainerWrapper;
import mekanism.common.component.containers.item.ComponentBackedBinInventorySlot;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.tier.BinTier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

public class BinInventorySlot extends BasicInventorySlot {

    public static final Predicate<ItemResource> validator = itemType -> !(itemType.getItem() instanceof ItemBlockBin);

    public static BinInventorySlot create(@Nullable IContentsListener listener, BinTier tier) {
        Objects.requireNonNull(tier, "Bin tier cannot be null");
        return new BinInventorySlot(listener, tier);
    }

    private final LockTypeJournal lockTypeJournal = new LockTypeJournal();
    private final boolean isCreative;

    private BinInventorySlot(@Nullable IContentsListener listener, BinTier tier) {
        super(tier.getCapacity(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), validator, null, null, listener, 0, 0);
        isCreative = tier.isCreative();
        obeyStackLimit = false;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(ItemResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            //"Fail quick" if there is nothing to add
            return 0;
        }
        if (isEmpty()) {
            if (isLocked() && !lockTypeJournal.lockType.equals(resource)) {
                // When locked, we need to make sure the correct item type is being inserted
                return 0;
            } else if (isCreative && automationType.isManual()) {
                //If a player manually inserts into a creative bin, that is empty we need to allow setting the type,
                // Note: We check that it is not external insertion because an empty creative bin acts as a "void" for automation
                try (Transaction simulation = Transaction.open(transaction)) {
                    if (super.insert(resource, amount, simulation, automationType) == 0) {
                        return 0;
                    }
                }
                //If we managed to insert anything, set the contents to the maximum amount of that item type
                // Note: We just set it as unchecked as we have already validated it
                setContents(resource, capacityAsLong(resource), transaction);
                //Return that we accepted the entire amount we were passed
                return amount;
            }
        }
        if (isCreative) {
            //Return the result without actually changing the contents (accepting without providing any changes)
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.insert(resource, amount, simulation, automationType);
            }
        }
        return super.insert(resource, amount, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(ItemResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (isEmpty() || amount == 0) {
            //"Fail quick" if we are empty, nothing is being extracted
            return 0;
        }
        if (isCreative) {
            try (Transaction simulation = Transaction.open(transaction)) {
                //Use a sub transaction that is not committed to effectively just simulate what will happen without making any changes
                return super.extract(resource, amount, simulation, automationType);
            }
        }
        return super.extract(resource, amount, transaction, automationType);
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot() {
        return null;
    }

    /// Modifies the lock state of the slot.
    ///
    /// @param lock if the slot should be locked
    ///
    /// @return if the lock state was modified
    public boolean setLocked(boolean lock) {
        // Don't lock if:
        // - We are a creative bin
        // - We already have the same state as the one we're supposed to switch to
        // - We were asked to lock, but we're empty
        if (isCreative || isLocked() == lock || (lock && isEmpty())) {
            return false;
        }
        setLockType(lock ? resource() : ItemResource.EMPTY, null);
        return true;
    }

    /// For use by tier installers and parsing placement data, do not use this in place of [#setLocked(boolean)]
    public void setLockType(ItemResource lockType, @Nullable TransactionContext transaction) {
        lockTypeJournal.setLockType(lockType, transaction);
    }

    public boolean isLocked() {
        return !lockTypeJournal.lockType.isEmpty();
    }

    public ItemResource getBinItemType() {
        return isLocked() ? lockTypeJournal.lockType : resource();
    }

    public ItemResource getLockType() {
        return lockTypeJournal.lockType;
    }

    @Override
    public void copyContents(IResourceContainer<ItemResource> other, @Nullable TransactionContext transaction) {
        if (other instanceof ResourceContainerWrapper<ItemResource, ?> wrapper) {
            other = wrapper.getInternal();
        }
        super.copyContents(other, transaction);
        if (other instanceof BinInventorySlot otherSlot) {
            setLockType(otherSlot.getLockType(), transaction);
        } else if (other instanceof ComponentBackedBinInventorySlot otherSlot) {
            setLockType(otherSlot.getLockType(), transaction);
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        //Note: While we are able to store this extra data for saving and stuff, when converting to an item we need to have
        // the tile copy the lock stack as a component
        super.serialize(output);
        if (isLocked()) {
            output.store(SerializationConstants.LOCK_TYPE, ItemResource.CODEC, lockTypeJournal.lockType);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        setLockType(input.read(SerializationConstants.LOCK_TYPE, ItemResource.CODEC).orElse(ItemResource.EMPTY), null);
        super.deserialize(input);
    }

    private static class LockTypeJournal extends SnapshotJournal<ItemResource> {

        private ItemResource lockType = ItemResource.EMPTY;

        public void setLockType(ItemResource lockType, @Nullable TransactionContext transaction) {
            if (transaction != null) {
                updateSnapshots(transaction);
            }
            this.lockType = lockType;
        }

        @Override
        protected ItemResource createSnapshot() {
            return lockType;
        }

        @Override
        protected void revertToSnapshot(ItemResource snapshot) {
            lockType = snapshot;
        }
    }
}