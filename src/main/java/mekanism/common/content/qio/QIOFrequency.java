package mekanism.common.content.qio;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.TagCache;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.BiMultimap;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import mekanism.common.network.PacketQIOItemViewerGuiSync;
import mekanism.common.util.NBTUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

public class QIOFrequency extends Frequency {

    private static final Random rand = new Random();

    private final Map<QIODriveKey, QIODriveData> driveMap = new LinkedHashMap<>();
    private final Map<HashedItem, QIOItemTypeData> itemDataMap = new LinkedHashMap<>();
    private final Set<IQIODriveHolder> driveHolders = new HashSet<>();
    // efficiently keep track of the tags utilized by the items stored
    private final BiMultimap<String, HashedItem> tagLookupMap = new BiMultimap<>();
    //Keep track of a UUID for each hashed item
    private final BiMap<HashedItem, UUID> itemTypeLookup = HashBiMap.create();
    // a sensitive cache for wildcard tag lookups (wildcard -> [matching tags])
    private final SetMultimap<String, String> tagWildcardCache = HashMultimap.create();

    private final Set<UUIDAwareHashedItem> updatedItems = new HashSet<>();
    private final Set<ServerPlayerEntity> playersViewingItems = new HashSet<>();

    /** If we need to send a packet to viewing clients with changed item data. */
    private boolean needsUpdate;
    /** If we have new item changes that haven't been saved. */
    private boolean isDirty;

    private long totalCount, totalCountCapacity;
    private int totalTypeCapacity;
    // only used on client side, for server side we can just look at itemDataMap.size()
    private int clientTypes;

    private EnumColor color = EnumColor.INDIGO;

    /**
     * @param uuid Should only be null if we have incomplete data that we are loading
     */
    public QIOFrequency(String n, @Nullable UUID uuid) {
        super(FrequencyType.QIO, n, uuid);
    }

    public QIOFrequency() {
        super(FrequencyType.QIO);
    }

    /**
     * Dangerous function. Don't mess with this map.
     *
     * @return core item data map, tracking item types + their respective counts and containing drives
     */
    public Map<HashedItem, QIOItemTypeData> getItemDataMap() {
        return itemDataMap;
    }

    @Nullable
    public HashedItem getTypeByUUID(@Nullable UUID uuid) {
        return uuid == null ? null : itemTypeLookup.inverse().get(uuid);
    }

    @Nullable
    public UUID getUUIDForType(HashedItem item) {
        return itemTypeLookup.get(item);
    }

    public ItemStack addItem(ItemStack stack) {
        HashedItem type = HashedItem.create(stack);
        // these checks are extremely important; they prevent us from wasting CPU searching for a place to put the new items,
        // and they also prevent us from adding a ghost type to the itemDataMap if nothing is inserted
        if (totalCount == totalCountCapacity || (!itemDataMap.containsKey(type) && itemDataMap.size() == totalTypeCapacity)) {
            return stack;
        }
        // at this point we're guaranteed at least part of the input stack will be inserted
        QIOItemTypeData data = itemDataMap.computeIfAbsent(type, t -> {
            tagLookupMap.putAll(TagCache.getItemTags(stack), t);
            tagWildcardCache.clear();
            itemTypeLookup.put(t, UUID.randomUUID());
            return new QIOItemTypeData(t);
        });
        return type.createStack((int) data.add(stack.getCount()));
    }

    public ItemStack removeItem(int amount) {
        return removeByType(null, amount);
    }

    public ItemStack removeItem(ItemStack stack, int amount) {
        return removeByType(HashedItem.create(stack), amount);
    }

    public ItemStack removeByType(@Nullable HashedItem itemType, int amount) {
        if (itemDataMap.isEmpty()) {
            return ItemStack.EMPTY;
        }

        QIOItemTypeData data;
        if (itemType == null) {
            Map.Entry<HashedItem, QIOItemTypeData> entry = itemDataMap.entrySet().iterator().next();
            itemType = entry.getKey();
            data = entry.getValue();
        } else {
            data = itemDataMap.get(itemType);
            if (data == null) {
                return ItemStack.EMPTY;
            }
        }

        ItemStack removed = data.remove(amount);
        // remove this item type if it's now empty
        if (data.count == 0) {
            itemDataMap.remove(data.itemType);
            tagLookupMap.removeValue(data.itemType);
            itemTypeLookup.remove(data.itemType);
            tagWildcardCache.clear();
        }
        return removed;
    }

    public Object2LongMap<HashedItem> getStacksByTag(String tag) {
        Set<HashedItem> items = tagLookupMap.getValues(tag);
        Object2LongMap<HashedItem> ret = new Object2LongOpenHashMap<>();
        items.forEach(item -> ret.put(item, getStored(item)));
        return ret;
    }

    public Object2LongMap<HashedItem> getStacksByWildcard(String wildcard) {
        if (!tagWildcardCache.containsKey(wildcard)) {
            buildWildcardMapping(wildcard);
        }
        Set<String> matchingTags = tagWildcardCache.get(wildcard);
        Object2LongMap<HashedItem> ret = new Object2LongOpenHashMap<>();
        matchingTags.forEach(tag -> ret.putAll(getStacksByTag(tag)));
        return ret;
    }

    private void buildWildcardMapping(String wildcard) {
        for (String tag : tagLookupMap.getAllKeys()) {
            if (WildcardMatcher.matches(wildcard, tag)) {
                tagWildcardCache.put(wildcard, tag);
            }
        }
    }

    public void openItemViewer(ServerPlayerEntity player) {
        playersViewingItems.add(player);
        Object2LongMap<UUIDAwareHashedItem> map = new Object2LongOpenHashMap<>();
        itemDataMap.values().forEach(d -> map.put(new UUIDAwareHashedItem(d.itemType, getUUIDForType(d.itemType)), d.count));
        Mekanism.packetHandler.sendTo(PacketQIOItemViewerGuiSync.batch(map, totalCountCapacity, totalTypeCapacity), player);
    }

    public void closeItemViewer(ServerPlayerEntity player) {
        playersViewingItems.remove(player);
    }

    public EnumColor getColor() {
        return color;
    }

    public void setColor(EnumColor color) {
        this.color = color;
    }

    // utility methods for accessing descriptors
    public long getTotalItemCount() {
        return totalCount;
    }

    public long getTotalItemCountCapacity() {
        return totalCountCapacity;
    }

    public int getTotalItemTypes(boolean remote) {
        return remote ? clientTypes : itemDataMap.size();
    }

    public int getTotalItemTypeCapacity() {
        return totalTypeCapacity;
    }

    public long getStored(HashedItem itemType) {
        QIOItemTypeData data = itemDataMap.get(itemType);
        return data != null ? data.count : 0;
    }

    public QIODriveData getDriveData(QIODriveKey key) {
        return driveMap.get(key);
    }

    @Override
    public void tick() {
        super.tick();
        if (!updatedItems.isEmpty() || needsUpdate) {
            Object2LongMap<UUIDAwareHashedItem> map = new Object2LongOpenHashMap<>();
            updatedItems.forEach(type -> {
                QIOItemTypeData data = itemDataMap.get(type);
                map.put(type, data == null ? 0 : data.count);
            });
            // flush players that somehow didn't send a container close packet
            playersViewingItems.removeIf(player -> !(player.openContainer instanceof QIOItemViewerContainer));
            playersViewingItems.forEach(player -> Mekanism.packetHandler.sendTo(PacketQIOItemViewerGuiSync.update(map, totalCountCapacity, totalTypeCapacity), player));
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

        if (CommonWorldTickHandler.flushTagAndRecipeCaches) {
            tagLookupMap.clear();
            tagWildcardCache.clear();
            itemDataMap.values().forEach(item -> tagLookupMap.putAll(TagCache.getItemTags(item.itemType.getStack()), item.itemType));
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
        // copy keys to avoid CME
        Set<QIODriveKey> keys = new HashSet<>(driveMap.keySet());
        keys.forEach(key -> removeDrive(key, false));
        driveMap.clear();
        playersViewingItems.forEach(player -> Mekanism.packetHandler.sendTo(PacketQIOItemViewerGuiSync.kill(), player));
    }

    @Override
    public int getSyncHash() {
        int code = super.getSyncHash();
        code = 31 * code + Long.hashCode(totalCount);
        code = 31 * code + Long.hashCode(totalCountCapacity);
        code = 31 * code + itemDataMap.size();
        code = 31 * code + totalTypeCapacity;
        code = 31 * code + color.ordinal();
        return code;
    }

    @Override
    public void write(PacketBuffer buf) {
        super.write(buf);
        buf.writeVarLong(totalCount);
        buf.writeVarLong(totalCountCapacity);
        buf.writeVarInt(itemDataMap.size());
        buf.writeVarInt(totalTypeCapacity);
        buf.writeEnumValue(color);
    }

    @Override
    public void read(PacketBuffer buf) {
        super.read(buf);
        totalCount = buf.readVarLong();
        totalCountCapacity = buf.readVarLong();
        clientTypes = buf.readVarInt();
        totalTypeCapacity = buf.readVarInt();
        color = buf.readEnumValue(EnumColor.class);
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt(NBTConstants.COLOR, color.ordinal());
    }

    @Override
    protected void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.COLOR, EnumColor::byIndexStatic, value -> color = value);
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
            data.getItemMap().forEach((storedKey, value) -> {
                itemDataMap.computeIfAbsent(storedKey, e -> {
                    tagWildcardCache.clear();
                    tagLookupMap.putAll(TagCache.getItemTags(e.getStack()), e);
                    itemTypeLookup.put(e, UUID.randomUUID());
                    return new QIOItemTypeData(e);
                }).addFromDrive(data, value);
                updatedItems.add(new UUIDAwareHashedItem(storedKey, getUUIDForType(storedKey)));
            });
            setNeedsUpdate();
        }
    }

    public void removeDrive(QIODriveKey key, boolean updateItemMap) {
        if (!driveMap.containsKey(key)) {
            return;
        }
        QIODriveData data = driveMap.get(key);
        if (updateItemMap) {
            data.getItemMap().forEach((storedKey, value) -> {
                QIOItemTypeData itemData = itemDataMap.get(storedKey);
                if (itemData != null) {
                    itemData.containingDrives.remove(key);
                    itemData.count -= value;
                    totalCount -= value;
                    // remove this entry from the item data map if it's now empty
                    if (itemData.containingDrives.isEmpty() || itemData.count == 0) {
                        itemDataMap.remove(storedKey);
                        tagWildcardCache.clear();
                    }
                    updatedItems.add(new UUIDAwareHashedItem(storedKey, getUUIDForType(storedKey)));
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
        driveMap.forEach((key, value) -> {
            key.updateMetadata(value);
            key.save(value);
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
        if (changedItem != null) {
            updatedItems.add(new UUIDAwareHashedItem(changedItem, getUUIDForType(changedItem)));
        }
    }

    private void setNeedsUpdate() {
        setNeedsUpdate(null);
    }

    public class QIOItemTypeData {

        private final HashedItem itemType;
        private long count = 0;
        private final Set<QIODriveKey> containingDrives = new HashSet<>();

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
                if (toAdd == 0) {
                    break;
                }
            }
            // next, we add the items to any drive that will take it
            if (toAdd > 0) {
                for (QIODriveData data : driveMap.values()) {
                    if (containingDrives.contains(data.getKey())) {
                        continue;
                    }
                    toAdd = addItemsToDrive(toAdd, data);
                    if (toAdd == 0) {
                        break;
                    }
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
            if (rejects < toAdd) {
                containingDrives.add(data.getKey());
            }
            return rejects;
        }

        private ItemStack remove(int amount) {
            ItemStack ret = ItemStack.EMPTY;
            for (Iterator<QIODriveKey> iter = containingDrives.iterator(); iter.hasNext(); ) {
                QIODriveData data = driveMap.get(iter.next());
                ItemStack stack = data.remove(itemType, amount - ret.getCount());
                if (ret.isEmpty()) {
                    ret = stack;
                } else {
                    ret.grow(stack.getCount());
                }
                // remove this drive from containingDrives if it doesn't have this item anymore
                if (data.getStored(itemType) == 0) {
                    iter.remove();
                }
                // break early if we found enough items
                if (ret.getCount() == amount) {
                    break;
                }
            }
            count -= ret.getCount();
            totalCount -= ret.getCount();
            setNeedsUpdate(itemType);
            return ret;
        }

        public long getCount() {
            return count;
        }
    }
}
