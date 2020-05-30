package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.IQIODriveItem.DriveMetadata;
import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.item.ItemStack;

public class QIODriveData {

    private final QIODriveKey key;
    private final long countCapacity;
    private final int typeCapacity;
    private final Object2LongMap<HashedItem> itemMap = new Object2LongOpenHashMap<>();
    private long itemCount;

    public QIODriveData(QIODriveKey key) {
        this.key = key;
        ItemStack stack = key.getDriveStack();
        IQIODriveItem item = (IQIODriveItem) stack.getItem();
        // load capacity values
        countCapacity = item.getCountCapacity(stack);
        typeCapacity = item.getTypeCapacity(stack);
        // load item map from drive stack
        item.loadItemMap(stack, this);
        // update cached item count value
        itemCount = itemMap.values().stream().mapToLong(Long::longValue).sum();

        key.updateMetadata(this);
    }

    public long add(HashedItem type, long amount) {
        long stored = getStored(type);
        // fail if we've reached item count capacity or adding this item would make us exceed type capacity
        if (itemCount == countCapacity || (stored == 0 && itemMap.size() == typeCapacity)) {
            return amount;
        }
        long toAdd = Math.min(amount, countCapacity - itemCount);
        itemMap.put(type, stored + toAdd);
        itemCount += toAdd;
        key.updateMetadata(this);
        key.dataUpdate();
        return amount - toAdd;
    }

    public ItemStack remove(HashedItem type, int amount) {
        long stored = getStored(type);
        ItemStack ret = type.createStack(Math.min(amount, (int) stored));
        if (stored - ret.getCount() > 0) {
            itemMap.put(type, stored - ret.getCount());
        } else {
            itemMap.removeLong(type);
        }
        itemCount -= ret.getCount();
        key.updateMetadata(this);
        key.dataUpdate();
        return ret;
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

    public static class QIODriveKey {

        private final IQIODriveHolder holder;
        private final int driveSlot;

        public QIODriveKey(IQIODriveHolder holder, int driveSlot) {
            this.holder = holder;
            this.driveSlot = driveSlot;
        }

        public void save(QIODriveData data) {
            holder.save(driveSlot, data);
        }

        public void dataUpdate() {
            holder.onDataUpdate();
        }

        public void updateMetadata(QIODriveData data) {
            ItemStack stack = getDriveStack();
            if (!(stack.getItem() instanceof IQIODriveItem)) {
                Mekanism.logger.error("Tried to update QIO meta values on an invalid ItemStack. Something has gone very wrong!");
                return;
            }
            DriveMetadata meta = new DriveMetadata(data.itemCount, data.itemMap.size());
            meta.write(stack);
        }

        public ItemStack getDriveStack() {
            return holder.getDriveSlots().get(driveSlot).getStack();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + driveSlot;
            result = prime * result + ((holder == null) ? 0 : holder.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof QIODriveKey && ((QIODriveKey) obj).holder == holder && ((QIODriveKey) obj).driveSlot == driveSlot;
        }
    }
}
