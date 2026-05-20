package mekanism.common.content.qio;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.attachments.qio.DriveContents;
import mekanism.common.attachments.qio.DriveMetadata;
import mekanism.common.inventory.slot.QIODriveSlot;
import mekanism.common.registries.MekanismDataComponents;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

@NothingNullByDefault
public class QIODriveData extends SnapshotJournal<QIODriveData.Snapshot> {

    private final QIODriveKey key;
    private final long countCapacity;
    private final int typeCapacity;
    private final Map<ItemResource, StoredAmountJournal> itemMap = new HashMap<>();
    private int itemTypes;
    private long itemCount;

    public QIODriveData(QIODriveKey key, ItemResource driveData) {
        this.key = key;
        IQIODriveItem driveItem = (IQIODriveItem) driveData.getItem();
        // load capacity values
        countCapacity = driveItem.getCountCapacity();
        typeCapacity = driveItem.getTypeCapacity();
        // load item map from drive stack
        driveData.getOrDefault(MekanismDataComponents.DRIVE_CONTENTS, DriveContents.EMPTY).loadItemMap(this::addTypeFromSaved);
        //TODO - 26.1: Re-evaluate this, theoretically as we just loaded this the metadata should be correct?
        // or is this to account for when a type is no longer valid?
        key.updateMetadata(this);
    }

    private void addTypeFromSaved(ItemResource itemType, long amount) {
        if (!itemType.isEmpty() && amount > 0) {
            //Only add the item if the item type is known. If it can't that means the mod adding the item was probably removed
            //TODO: Eventually we may want to keep the UUID so that if the mod gets added back it exists again?
            StoredAmountJournal storedAmount = new StoredAmountJournal(itemType);
            storedAmount.stored = amount;
            itemMap.put(itemType, storedAmount);
            // update cached item count and type value
            itemTypes++;
            itemCount += amount;
        }
    }

    public long add(ItemResource type, long amount, TransactionContext transaction) {
        // fail if we've reached item count capacity or adding this item would make us exceed type capacity
        if (itemCount == countCapacity) {
            return 0;
        }
        StoredAmountJournal storedAmount;
        if (itemTypes == typeCapacity) {
            storedAmount = itemMap.get(type);
            if (storedAmount == null) {//No space for more item types
                return 0;
            }
        } else {
            storedAmount = itemMap.computeIfAbsent(type, StoredAmountJournal::new);
        }
        updateSnapshots(transaction);
        long toAdd = Math.min(amount, countCapacity - itemCount);
        if (storedAmount.stored == 0) {
            //Type was not counted, we need to add it to the number of item types we have
            itemTypes++;
        }
        storedAmount.updateSnapshots(transaction);
        storedAmount.stored += toAdd;
        itemCount += toAdd;
        return toAdd;
    }

    public long remove(ItemResource type, long amount, TransactionContext transaction) {
        StoredAmountJournal storedAmount = itemMap.get(type);
        if (storedAmount == null || storedAmount.stored == 0) {
            //None of that item type is currently stored, so just short circuit and return that nothing could be removed
            return 0;
        }
        updateSnapshots(transaction);
        storedAmount.updateSnapshots(transaction);
        long toRemove = Math.min(amount, storedAmount.stored);
        storedAmount.stored -= toRemove;
        itemCount -= toRemove;
        if (storedAmount.stored == 0) {
            //The item type is back to not actually being stored anymore
            itemTypes--;
        }
        return toRemove;
    }

    @Override
    protected QIODriveData.Snapshot createSnapshot() {
        return new Snapshot(itemCount, itemTypes);
    }

    @Override
    protected void revertToSnapshot(QIODriveData.Snapshot snapshot) {
        itemCount = snapshot.itemCount();
        itemTypes = snapshot.itemTypes();
    }

    @Override
    protected void onRootCommit(QIODriveData.Snapshot originalState) {
        super.onRootCommit(originalState);
        if (originalState.itemCount() != itemCount || originalState.itemTypes() != itemTypes) {
            //If the state changed, update the metadata and stored data
            key.updateMetadata(this);
            key.dataUpdate();
        }
    }

    //TODO - 26.1: Re-evaluate callers
    public long getStored(ItemResource type) {
        StoredAmountJournal storedAmount = itemMap.get(type);
        return storedAmount == null ? 0 : storedAmount.stored;
    }

    public <DATA> void forDriveContents(DATA data, DriveContentConsumer<DATA> consumer) {
        for (Map.Entry<ItemResource, StoredAmountJournal> entry : itemMap.entrySet()) {
            StoredAmountJournal storedAmount = entry.getValue();
            if (storedAmount.stored > 0) {
                consumer.accept(data, entry.getKey(), storedAmount.stored);
            }
        }
    }

    //TODO - 26.1: Do we want to remove this method, it no longer is used
    public QIODriveKey getKey() {
        return key;
    }

    public long getCountCapacity() {
        return countCapacity;
    }

    public int getTypeCapacity() {
        return typeCapacity;
    }

    public long getTotalCount() {
        return itemCount;
    }

    public int getTotalTypes() {
        return itemTypes;
    }

    public record Snapshot(long itemCount, int itemTypes) {
    }

    private class StoredAmountJournal extends SnapshotJournal<Long> {

        private final ItemResource itemType;
        private long stored;

        public StoredAmountJournal(ItemResource itemType) {
            this.itemType = itemType;
        }

        @Override
        protected Long createSnapshot() {
            return stored;
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            stored = snapshot;
            if (stored == 0) {
                //TODO - 26.1: Is this correct to do it when reverting? Or is it supposed to still be present for a bit
                //If we end up with having nothing remove the tracking for this item type
                itemMap.remove(itemType);
            }
        }

        @Override
        protected void onRootCommit(Long originalState) {
            super.onRootCommit(originalState);
            if (stored == 0) {
                //If we end up with having nothing remove the tracking for this item type
                itemMap.remove(itemType);
            }
        }
    }

    @FunctionalInterface
    public interface DriveContentConsumer<DATA> {

        void accept(DATA data, ItemResource itemType, long stored);
    }

    public record QIODriveKey(IQIODriveHolder holder, int driveSlot) {

        public void save(QIODriveData data) {
            //TODO - 26.1: Evaluate callers to make sure that this is updating the correct stack
            // Also do we need to make sure the slot calls onContentsChanged?
            QIODriveSlot slot = holder.getDriveSlots().get(driveSlot);
            ItemResource itemType = slot.resource();
            if (itemType.value() instanceof IQIODriveItem) {
                //Update stored items and metadata
                ItemResource updatedItem = itemType.with(MekanismDataComponents.DRIVE_CONTENTS, DriveContents.create(data))
                      .with(MekanismDataComponents.DRIVE_METADATA, new DriveMetadata(data));
                //TODO - 26.1: Would it be useful to have a method to transform the stored type rather than having to set and query what the stored amount is?
                slot.setContents(updatedItem, slot.amountAsLong(), null);
            } else {
                Mekanism.logger.error("Tried to save data map to an invalid item ({}). Something has gone very wrong!", itemType.getItem());
            }
        }

        public void dataUpdate() {
            holder.onDataUpdate();
        }

        public void updateMetadata(QIODriveData data) {
            //TODO - 26.1: Evaluate callers to make sure that this is updating the correct stack
            // Also do we need to make sure the slot calls onContentsChanged?
            QIODriveSlot slot = holder.getDriveSlots().get(driveSlot);
            ItemResource itemType = slot.resource();
            if (itemType.value() instanceof IQIODriveItem) {
                slot.setContents(itemType.with(MekanismDataComponents.DRIVE_METADATA, new DriveMetadata(data)), slot.amountAsLong(), null);
            } else {
                Mekanism.logger.error("Tried to update QIO meta values on an invalid Item ({}). Something has gone very wrong!", itemType);
            }
        }
    }
}
