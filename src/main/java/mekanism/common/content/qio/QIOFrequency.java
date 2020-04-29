package mekanism.common.content.qio;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import mekanism.common.content.transporter.HashedItem;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyType;
import mekanism.common.network.PacketQIOItemViewerGuiSync;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class QIOFrequency extends Frequency {

    private Map<QIODriveKey, QIODriveData> driveMap = new Object2ObjectOpenHashMap<>();
    private Map<HashedItem, QIOItemTypeData> itemDataMap = new Object2ObjectOpenHashMap<>();
    private Set<IQIODriveHolder> driveHolders = new HashSet<>();

    private Set<HashedItem> updatedItems = new HashSet<>();
    private Set<ServerPlayerEntity> playersViewingItems = new HashSet<>();

    public QIOFrequency(String n, UUID uuid) {
        super(FrequencyType.QIO, n, uuid);
    }

    public QIOFrequency() {
        super(FrequencyType.QIO);
    }

    public ItemStack addItem(ItemStack stack) {
        HashedItem type = new HashedItem(stack);
        QIOItemTypeData data = itemDataMap.computeIfAbsent(type, t -> new QIOItemTypeData(type));
        return type.createStack((int) data.add(stack.getCount()));
    }

    public ItemStack removeByType(@Nullable HashedItem itemType, int amount) {
        if (itemDataMap.isEmpty())
            return ItemStack.EMPTY;

        QIOItemTypeData data = null;
        if (itemType == null) {
            Map.Entry<HashedItem, QIOItemTypeData> entry = itemDataMap.entrySet().iterator().next();
            itemType = entry.getKey();
        } else {
            data = itemDataMap.get(itemType);
            if (data == null)
                return ItemStack.EMPTY;
        }

        return data.remove(amount);
    }

    public ItemStack removeItem(int amount) {
        return removeByType(null, amount);
    }

    public ItemStack removeItem(ItemStack stack, int amount) {
        return removeByType(new HashedItem(stack), amount);
    }

    public void openItemViewer(ServerPlayerEntity player) {
        playersViewingItems.add(player);
        Map<HashedItem, Long> map = new Object2ObjectOpenHashMap<>();
        itemDataMap.values().forEach(d -> map.put(d.itemType, d.count));
        Mekanism.packetHandler.sendTo(PacketQIOItemViewerGuiSync.batch(map), player);
    }

    public void closeItemViewer(ServerPlayerEntity player) {
        playersViewingItems.remove(player);
    }

    @Override
    public void tick() {
        super.tick();
        if (!updatedItems.isEmpty()) {
            Map<HashedItem, Long> map = new Object2ObjectOpenHashMap<>();
            updatedItems.forEach(type -> {
                QIOItemTypeData data = itemDataMap.get(type);
                map.put(type, data == null ? 0 : data.count);
            });
            playersViewingItems.forEach(player -> {
                Mekanism.packetHandler.sendTo(PacketQIOItemViewerGuiSync.update(map), player);
            });
        }
    }

    @Override
    public void onDeactivate(TileEntity tile) {
        super.onDeactivate(tile);

        if (tile instanceof IQIODriveHolder) {
            IQIODriveHolder holder = (IQIODriveHolder) tile;
            for (int i = 0; i < holder.getDriveSlots().size(); i++) {
                QIODriveKey key = new QIODriveKey(holder, i);
                removeDrive(key, true);
                driveMap.remove(key);
            }
        }
    }

    @Override
    public void update(TileEntity tile) {
        super.update(tile);
        if (tile instanceof IQIODriveHolder) {
            IQIODriveHolder holder = (IQIODriveHolder) tile;
            if (!driveHolders.contains(holder)) {
                addHolder(holder);
            }
        }
    }

    @Override
    public void onRemove() {
        super.onRemove();
        driveMap.keySet().forEach(key -> removeDrive(key, false));
        driveMap.clear();
    }

    public void addDrive(QIODriveKey key) {
        if (key.getDriveStack().getItem() instanceof IQIODriveItem) {
            // if a drive in this position is already in the system, we remove it before adding this one
            if (driveMap.containsKey(key)) {
                removeDrive(key, true);
            }

            QIODriveData data = new QIODriveData(key);
            driveMap.put(key, data);
            data.getItemMap().entrySet().forEach(entry -> {
                itemDataMap.computeIfAbsent(entry.getKey(), e -> new QIOItemTypeData(entry.getKey())).addFromDrive(data, entry.getValue());
                updatedItems.add(entry.getKey());
            });
        }
    }

    public void removeDrive(QIODriveKey key, boolean updateItemMap) {
        if (!driveMap.containsKey(key))
            return;
        QIODriveData data = driveMap.get(key);
        if (updateItemMap) {
            data.getItemMap().entrySet().forEach(entry -> {
                QIOItemTypeData itemData = itemDataMap.get(entry.getKey());
                if (itemData != null) {
                    itemData.containingDrives.remove(key);
                    itemData.count -= entry.getValue();
                    // remove this entry from the item data map if it's now empty
                    if (itemData.containingDrives.isEmpty() || itemData.count == 0) {
                        itemDataMap.remove(entry.getKey());
                    }
                    updatedItems.add(entry.getKey());
                }
            });
        }
        // save the item list onto the physical drive
        key.save(data);
    }

    private void addHolder(IQIODriveHolder holder) {
        driveHolders.add(holder);
        for (int i = 0; i < holder.getDriveSlots().size(); i++) {
            addDrive(new QIODriveKey(holder, i));
        }
    }

    public class QIOItemTypeData {

        private HashedItem itemType;
        private long count = 0;
        private Set<QIODriveKey> containingDrives = new HashSet<>();

        public QIOItemTypeData(HashedItem itemType) {
            this.itemType = itemType;
        }

        private void addFromDrive(QIODriveData data, long toAdd) {
            count += toAdd;
            containingDrives.add(data.getKey());
        }

        private long add(long amount) {
            long toAdd = amount;
            for (QIODriveData data : driveMap.values()) {
                long rejects = data.add(itemType, toAdd);
                if (rejects < toAdd)
                    containingDrives.add(data.getKey());
                toAdd = rejects;
                if (toAdd == 0)
                    break;
            }
            count += amount - toAdd;
            updatedItems.add(itemType);
            return amount;
        }

        private ItemStack remove(int amount) {
            ItemStack ret = ItemStack.EMPTY;
            for (Iterator<QIODriveKey> iter = containingDrives.iterator(); iter.hasNext();) {
                QIODriveData data = driveMap.get(iter.next());
                ItemStack stack = data.remove(itemType, amount - ret.getCount());
                if (ret.isEmpty()) {
                    ret = stack;
                } else {
                    ret.grow(stack.getCount());
                }
                // remove this drive from containingDrives if it doesn't have this item anymore
                if (data.getStored(itemType) == 0)
                    iter.remove();
                // break early if we found enough items
                if (ret.getCount() == amount)
                    break;
            }
            count -= ret.getCount();
            updatedItems.add(itemType);
            return ret;
        }
    }
}
