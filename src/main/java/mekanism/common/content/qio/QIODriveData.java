package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import mekanism.api.Action;
import mekanism.common.Mekanism;
import mekanism.common.attachments.qio.DriveContents;
import mekanism.common.attachments.qio.DriveMetadata;
import mekanism.common.inventory.slot.QIODriveSlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismDataComponents;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class QIODriveData {

    private final QIODriveKey key;
    private final long countCapacity;
    private final int typeCapacity;
    private final Object2LongMap<HashedItem> itemMap = new Object2LongOpenHashMap<>();
    private long itemCount;

    public QIODriveData(QIODriveKey key, ItemResource driveData) {
        this.key = key;
        IQIODriveItem driveItem = (IQIODriveItem) driveData.getItem();
        // load capacity values
        countCapacity = driveItem.getCountCapacity();
        typeCapacity = driveItem.getTypeCapacity();
        // load item map from drive stack
        driveData.getOrDefault(MekanismDataComponents.DRIVE_CONTENTS, DriveContents.EMPTY).loadItemMap(this);
        // update cached item count value
        itemCount = itemMap.values().longStream().sum();

        key.updateMetadata(this);
    }

    public long add(HashedItem type, long amount, Action action) {
        long stored = getStored(type);
        // fail if we've reached item count capacity or adding this item would make us exceed type capacity
        if (itemCount == countCapacity || (stored == 0 && itemMap.size() == typeCapacity)) {
            return amount;
        }
        long toAdd = Math.min(amount, countCapacity - itemCount);
        if (action.execute()) {
            itemMap.put(type, stored + toAdd);
            itemCount += toAdd;
            key.updateMetadata(this);
            key.dataUpdate();
        }
        return amount - toAdd;
    }

    public long remove(HashedItem type, long amount, Action action) {
        long stored = getStored(type);
        long removed = Math.min(amount, stored);
        if (action.execute()) {
            long remaining = stored - removed;
            if (remaining > 0) {
                itemMap.put(type, remaining);
            } else {
                itemMap.removeLong(type);
            }
            itemCount -= removed;
            key.updateMetadata(this);
            key.dataUpdate();
        }
        return removed;
    }

    public long getStored(HashedItem type) {
        return itemMap.getOrDefault(type, 0L);
    }

    public Object2LongMap<HashedItem> getItemMap() {
        return itemMap;
    }

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
        return itemMap.size();
    }

    public record QIODriveKey(IQIODriveHolder holder, int driveSlot) {

        public void save(QIODriveData data) {
            //TODO - 26.1: Evaluate callers to make sure that this is updating the correct stack
            // Also do we need to make sure the slot calls onContentsChanged?
            QIODriveSlot slot = holder.getDriveSlots().get(driveSlot);
            ItemResource itemType = slot.getResource();
            if (itemType.value() instanceof IQIODriveItem) {
                //Update stored items and metadata
                ItemResource updatedItem = itemType.with(MekanismDataComponents.DRIVE_CONTENTS, DriveContents.create(data))
                      .with(MekanismDataComponents.DRIVE_METADATA, new DriveMetadata(data));
                slot.setStack(updatedItem, slot.getCount());
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
            ItemResource itemType = slot.getResource();
            if (itemType.value() instanceof IQIODriveItem) {
                slot.setStack(itemType.with(MekanismDataComponents.DRIVE_METADATA, new DriveMetadata(data)), slot.getCount());
            } else {
                Mekanism.logger.error("Tried to update QIO meta values on an invalid Item ({}). Something has gone very wrong!", itemType);
            }
        }
    }
}
