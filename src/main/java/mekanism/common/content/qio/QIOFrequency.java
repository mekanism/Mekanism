package mekanism.common.content.qio;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import mekanism.common.content.transporter.HashedItem;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyType;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.network.PacketQIOItemViewerGuiSync;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

public class QIOFrequency extends Frequency {

    private static final NumberFormat intFormatter = NumberFormat.getIntegerInstance();
    private static final Random rand = new Random();

    private Map<QIODriveKey, QIODriveData> driveMap = new Object2ObjectOpenHashMap<>();
    private Map<HashedItem, QIOItemTypeData> itemDataMap = new Object2ObjectOpenHashMap<>();
    private Set<IQIODriveHolder> driveHolders = new HashSet<>();

    private Set<HashedItem> updatedItems = new HashSet<>();
    private Set<ServerPlayerEntity> playersViewingItems = new HashSet<>();

    /** If we need to send a packet to viewing clients with changed item data. */
    private boolean needsUpdate;
    /** If we have new item changes that haven't been saved. */
    private boolean isDirty;

    private long totalCount, totalCountCapacity;
    private int totalTypeCapacity;
    // only used on client side, for server side we can just look at itemDataMap.size()
    private int clientTypes;

    public QIOFrequency(String n, UUID uuid) {
        super(FrequencyType.QIO, n, uuid);
    }

    public QIOFrequency() {
        super(FrequencyType.QIO);
    }

    public ItemStack addItem(ItemStack stack) {
        HashedItem type = new HashedItem(stack);
        // these checks are extremely important; they prevent us from wasting CPU searching for a place to put the new items,
        // and they also prevent us from adding a ghost type to the itemDataMap if nothing is inserted
        if (totalCount == totalCountCapacity || (!itemDataMap.containsKey(type) && itemDataMap.size() == totalTypeCapacity))
            return stack;
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

        ItemStack removed = data.remove(amount);
        // remove this item type if it's now empty
        if (data.count == 0)
            itemDataMap.remove(data.itemType);
        return removed;
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
        Mekanism.packetHandler.sendTo(PacketQIOItemViewerGuiSync.batch(map, totalCountCapacity, totalTypeCapacity), player);
    }

    public void closeItemViewer(ServerPlayerEntity player) {
        playersViewingItems.remove(player);
    }

    // utility methods for accessing descriptors
    public long getTotalItemCount() { return totalCount; }
    public long getTotalItemCountCapacity() { return totalCountCapacity; }
    public int getTotalItemTypes(boolean remote) { return remote ? clientTypes : itemDataMap.size(); }
    public int getTotalItemTypeCapacity() { return totalTypeCapacity; }

    @Override
    public void tick() {
        super.tick();
        if (!updatedItems.isEmpty() || needsUpdate) {
            Map<HashedItem, Long> map = new Object2ObjectOpenHashMap<>();
            updatedItems.forEach(type -> {
                QIOItemTypeData data = itemDataMap.get(type);
                map.put(type, data == null ? 0 : data.count);
            });
            // flush players that somehow didn't send a container close packet
            playersViewingItems.removeIf(player -> !(player.openContainer instanceof QIOItemViewerContainer));
            playersViewingItems.forEach(player -> {
                Mekanism.packetHandler.sendTo(PacketQIOItemViewerGuiSync.update(map, totalCountCapacity, totalTypeCapacity), player);
            });
            updatedItems.clear();
            needsUpdate = false;
        }
        // if something has changed, we'll subsequently randomly run a save operation in the next 100 ticks.
        // the random factor helps us avoid bogging down the CPU by saving all QIO frequencies at once
        // this isn't a fully necessary operation, but it'll help avoid all item data getting lost if the server
        // is forcibly shut down.
        if (isDirty && rand.nextInt(100) == 0) {
            saveAll();
            isDirty = false;
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
        playersViewingItems.forEach(player -> Mekanism.packetHandler.sendTo(PacketQIOItemViewerGuiSync.kill(), player));
    }

    @Override
    public int hashCode() {
        int code = super.hashCode();
        code = 31 * code + Long.hashCode(totalCount);
        code = 31 * code + Long.hashCode(totalCountCapacity);
        code = 31 * code + itemDataMap.size();
        code = 31 * code + totalTypeCapacity;
        return code;
    }

    @Override
    public void write(PacketBuffer buf) {
        super.write(buf);
        buf.writeVarLong(totalCount);
        buf.writeVarLong(totalCountCapacity);
        buf.writeVarInt(itemDataMap.size());
        buf.writeVarInt(totalTypeCapacity);
    }

    @Override
    public void read(PacketBuffer buf) {
        super.read(buf);
        totalCount = buf.readVarLong();
        totalCountCapacity = buf.readVarLong();
        clientTypes = buf.readVarInt();
        totalTypeCapacity = buf.readVarInt();
    }

    public void addDrive(QIODriveKey key) {
        if (key.getDriveStack().getItem() instanceof IQIODriveItem) {
            // if a drive in this position is already in the system, we remove it before adding this one
            if (driveMap.containsKey(key)) {
                removeDrive(key, true);
            }
            // add drive and capacity info to core tracking
            QIODriveData data = new QIODriveData(key);
            totalCountCapacity += data.getCountCapacity();
            totalTypeCapacity += data.getTypeCapacity();
            driveMap.put(key, data);
            data.getItemMap().entrySet().forEach(entry -> {
                itemDataMap.computeIfAbsent(entry.getKey(), e -> new QIOItemTypeData(entry.getKey())).addFromDrive(data, entry.getValue());
                updatedItems.add(entry.getKey());
            });
            setNeedsUpdate();
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
                    totalCount -= entry.getValue();
                    // remove this entry from the item data map if it's now empty
                    if (itemData.containingDrives.isEmpty() || itemData.count == 0) {
                        itemDataMap.remove(entry.getKey());
                    }
                    updatedItems.add(entry.getKey());
                }
            });
            setNeedsUpdate();
        }
        // remove drive and capacity info from core tracking
        totalCountCapacity -= data.getCountCapacity();
        totalTypeCapacity -= data.getTypeCapacity();
        driveMap.remove(key);
        // save the item list onto the physical drive
        key.updateMetadata(data);
        key.save(data);
    }

    public void saveAll() {
        driveMap.entrySet().forEach(e -> {
            e.getKey().updateMetadata(e.getValue());
            e.getKey().save(e.getValue());
        });
    }

    private void addHolder(IQIODriveHolder holder) {
        driveHolders.add(holder);
        for (int i = 0; i < holder.getDriveSlots().size(); i++) {
            addDrive(new QIODriveKey(holder, i));
        }
    }

    private void setNeedsUpdate(@Nullable HashedItem changedItem) {
        needsUpdate = true;
        isDirty = true;
        if (changedItem != null)
            updatedItems.add(changedItem);
    }

    private void setNeedsUpdate() {
        setNeedsUpdate(null);
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
            totalCount += toAdd;
            containingDrives.add(data.getKey());
            setNeedsUpdate();
        }

        private long add(long amount) {
            long toAdd = amount;
            // first we try to add the items to an already-containing drive
            for (QIODriveKey key : containingDrives) {
                toAdd = addItemsToDrive(toAdd, driveMap.get(key));
                if (toAdd == 0)
                    break;
            }
            // next, we add the items to any drive that will take it
            if (toAdd > 0) {
                for (QIODriveData data : driveMap.values()) {
                    if (containingDrives.contains(data.getKey()))
                        continue;
                    toAdd = addItemsToDrive(toAdd, data);
                    if (toAdd == 0)
                        break;
                }
            }
            // update internal/core values and return
            count += amount - toAdd;
            totalCount += amount - toAdd;
            setNeedsUpdate(itemType);
            return toAdd;
        }

        private long addItemsToDrive(long toAdd, QIODriveData data) {
            long rejects = data.add(itemType, toAdd);
            if (rejects < toAdd)
                containingDrives.add(data.getKey());
            return rejects;
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
            totalCount -= ret.getCount();
            setNeedsUpdate(itemType);
            return ret;
        }
    }

    public static String formatItemCount(long count) {
        return intFormatter.format(count);
    }

    public static String formatItemTypes(int types) {
        return intFormatter.format(types);
    }
}
