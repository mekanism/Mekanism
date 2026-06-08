package mekanism.common.content.qio;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.qio.DriveContents;
import mekanism.common.attachments.qio.DriveMetadata;
import mekanism.common.inventory.slot.QIODriveSlot;
import mekanism.common.registries.MekanismDataComponents;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.VisibleForTesting;

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
        // or was this to account for when a type is no longer valid?
        //QIODriveSlot slot = key.holder().getDriveSlots().get(key.driveSlot());
        //slot.updateSaveData(slot.resource().with(MekanismDataComponents.DRIVE_METADATA, asDriveMetadata()));
    }

    private void addTypeFromSaved(ItemResource itemType, long amount) {
        if (!itemType.isEmpty() && amount > 0) {
            //Only add the item if the item type is known. If it can't that means the mod adding the item was probably removed
            //TODO: Eventually we may want to keep the UUID so that if the mod gets added back it exists again?
            itemMap.put(itemType, new StoredAmountJournal(itemType, amount));
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
            storedAmount = itemMap.computeIfAbsent(type, t -> new StoredAmountJournal(t, 0));
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
            //If the state changed, update the metadata and stored data on the physical drive so that if it is removed it will have valid data
            //TODO: Is there a way we can just have the diff of what happened get applied?
            key.save(this);
        }
    }

    @VisibleForTesting
    public boolean hasCache(ItemResource itemType) {
        return itemMap.get(itemType) != null;
    }

    @VisibleForTesting
    public boolean isStoringEmpty(ItemResource itemType) {
        StoredAmountJournal storedAmount = itemMap.get(itemType);
        return storedAmount != null && storedAmount.stored == 0;
    }

    public boolean isStoring(ItemResource type) {
        StoredAmountJournal storedAmount = itemMap.get(type);
        return storedAmount != null && storedAmount.stored > 0;
    }

    public <DATA> void forDriveContents(DATA data, DriveContentConsumer<DATA> consumer) {
        for (Map.Entry<ItemResource, StoredAmountJournal> entry : itemMap.entrySet()) {
            StoredAmountJournal storedAmount = entry.getValue();
            if (storedAmount.stored > 0) {
                consumer.accept(data, entry.getKey(), storedAmount.stored);
            }
        }
    }

    private DriveMetadata asDriveMetadata() {
        return new DriveMetadata(itemCount, itemTypes);
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

    private class StoredAmountJournal extends SnapshotJournal<StoredAmountJournal.Snapshot> {

        private final ItemResource itemType;
        private boolean justAdded;
        private long stored;

        public StoredAmountJournal(ItemResource itemType, long stored) {
            this.itemType = itemType;
            this.stored = stored;
            this.justAdded = this.stored == 0;
        }

        @Override
        public void updateSnapshots(TransactionContext transaction) {
            super.updateSnapshots(transaction);
            justAdded = false;
        }

        @Override
        protected StoredAmountJournal.Snapshot createSnapshot() {
            return new Snapshot(stored, justAdded);
        }

        @Override
        protected void revertToSnapshot(StoredAmountJournal.Snapshot snapshot) {
            stored = snapshot.stored();
            justAdded = snapshot.justAdded();
            if (stored == 0 && justAdded) {
                //If we end up with having nothing stored, and we were just added in the snapshot we were reverted to, remove the tracking for this item type
                itemMap.remove(itemType);
            }
        }

        @Override
        protected void onRootCommit(StoredAmountJournal.Snapshot originalState) {
            super.onRootCommit(originalState);
            if (stored == 0) {
                //If we end up with having nothing when the transactional context is complete, remove the tracking for this item type
                itemMap.remove(itemType);
            }
        }

        public record Snapshot(long stored, boolean justAdded) {
        }
    }

    @FunctionalInterface
    public interface DriveContentConsumer<DATA> {

        void accept(DATA data, ItemResource itemType, long stored);
    }

    public record QIODriveKey(IQIODriveHolder holder, int driveSlot) {

        public void save(QIODriveData data) {
            QIODriveSlot slot = holder.getDriveSlots().get(driveSlot);
            //Update stored items and metadata
            slot.updateSaveData(slot.resource()
                  .with(MekanismDataComponents.DRIVE_CONTENTS, DriveContents.create(data))
                  .with(MekanismDataComponents.DRIVE_METADATA, data.asDriveMetadata())
            );
        }
    }
}
