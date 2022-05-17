package mekanism.common.content.qio;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.inventory.qio.IQIOFrequency;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.TagCache;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.lib.collection.BiMultimap;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import mekanism.common.network.to_client.PacketQIOItemViewerGuiSync;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class QIOFrequency extends Frequency implements IColorableFrequency, IQIOFrequency {

    private static final Random rand = new Random();

    private final Map<QIODriveKey, QIODriveData> driveMap = new LinkedHashMap<>();
    private final Map<HashedItem, QIOItemTypeData> itemDataMap = new LinkedHashMap<>();
    private final Set<IQIODriveHolder> driveHolders = new HashSet<>();
    // efficiently keep track of the tags utilized by the items stored
    private final BiMultimap<String, HashedItem> tagLookupMap = new BiMultimap<>();
    // efficiently keep track of the modids utilized by the items stored
    private final Map<String, Set<HashedItem>> modIDLookupMap = new HashMap<>();
    // efficiently keep track of the items for use in fuzzy lookup utilized by the items stored
    private final Map<Item, Set<HashedItem>> fuzzyItemLookupMap = new HashMap<>();
    // keep track of a UUID for each hashed item
    private final BiMap<HashedItem, UUID> itemTypeLookup = HashBiMap.create();
    // allows for lazily removing the UUIDs assigned to items in the itemTypeLookup BiMap without having any issues
    // come up related to updatedItems being a set and UUIDAwareHashedItems server side intentionally not comparing
    // the UUIDs, so then if multiple add/remove calls happened at once the items in need of updating potentially
    // would sync using a different UUID to the client, causing the client to not know the old stack needed to be removed
    private final Set<UUID> uuidsToInvalidate = new HashSet<>();
    // a sensitive cache for wildcard tag lookups (wildcard -> [matching tags])
    private final SetMultimap<String, String> tagWildcardCache = HashMultimap.create();
    private final Set<String> failedWildcardTags = new HashSet<>();
    // a sensitive cache for wildcard modid lookups (wildcard -> [matching modids])
    private final SetMultimap<String, String> modIDWildcardCache = HashMultimap.create();
    private final Set<String> failedWildcardModIDs = new HashSet<>();

    private final Set<UUIDAwareHashedItem> updatedItems = new HashSet<>();
    private final Set<ServerPlayer> playersViewingItems = new HashSet<>();

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

    @Override
    public void forAllStored(ObjLongConsumer<ItemStack> consumer) {
        itemDataMap.forEach((type, data) -> consumer.accept(type.createStack(1), data.getCount()));
    }

    @Nullable
    public HashedItem getTypeByUUID(@Nullable UUID uuid) {
        return uuid == null ? null : itemTypeLookup.inverse().get(uuid);
    }

    @Nullable
    public UUID getUUIDForType(HashedItem item) {
        return itemTypeLookup.get(item);
    }

    @Override
    public long massInsert(ItemStack stack, long amount, Action action) {
        if (stack.isEmpty() || amount <= 0) {
            return 0;
        }
        HashedItem type = action.execute() ? HashedItem.create(stack) : HashedItem.raw(stack);
        // these checks are extremely important; they prevent us from wasting CPU searching for a place to put the new items,
        // and they also prevent us from adding a ghost type to the itemDataMap if nothing is inserted
        if (totalCount == totalCountCapacity || (!itemDataMap.containsKey(type) && itemDataMap.size() == totalTypeCapacity)) {
            return 0;
        }
        // at this point we're guaranteed at least part of the input stack will be inserted
        QIOItemTypeData data;
        if (action.execute()) {
            data = itemDataMap.computeIfAbsent(type, this::createTypeDataForAbsent);
        } else {
            //If we are simulating, look it up
            data = itemDataMap.get(type);
            if (data == null) {
                // if it doesn't already have that type, fall back to a new item type data that doesn't actually get added
                data = new QIOItemTypeData(type);
            }
        }
        return amount - data.add(amount, action);
    }

    public ItemStack addItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        HashedItem type = HashedItem.create(stack);
        // these checks are extremely important; they prevent us from wasting CPU searching for a place to put the new items,
        // and they also prevent us from adding a ghost type to the itemDataMap if nothing is inserted
        if (totalCount == totalCountCapacity || (!itemDataMap.containsKey(type) && itemDataMap.size() == totalTypeCapacity)) {
            return stack;
        }
        // at this point we're guaranteed at least part of the input stack will be inserted
        QIOItemTypeData data = itemDataMap.computeIfAbsent(type, this::createTypeDataForAbsent);
        return type.createStack(MathUtils.clampToInt(data.add(stack.getCount(), Action.EXECUTE)));
    }

    private QIOItemTypeData createTypeDataForAbsent(HashedItem type) {
        ItemStack stack = type.getStack();
        List<String> tags = TagCache.getItemTags(stack);
        if (!tags.isEmpty()) {
            boolean hasAllKeys = tagLookupMap.hasAllKeys(tags);
            if (tagLookupMap.putAll(tags, type) && !hasAllKeys) {
                //If we added any tag item combinations, and we didn't have all the keys for tags this item has,
                // then we need to clear our wildcard cache as our new tags may be valid for some of our wildcards
                tagWildcardCache.clear();
                failedWildcardTags.clear();
            }
        }
        modIDLookupMap.computeIfAbsent(MekanismUtils.getModId(stack), modID -> {
            //If we added a new modid to the lookup map we also want to make sure that we clear our modid wildcard cache
            // as our new modid may be valid for some of our wildcards
            modIDWildcardCache.clear();
            failedWildcardModIDs.clear();
            return new HashSet<>();
        }).add(type);
        //Fuzzy item lookup has no wildcard cache related to it
        fuzzyItemLookupMap.computeIfAbsent(stack.getItem(), item -> new HashSet<>()).add(type);
        UUID oldUUID = getUUIDForType(type);
        if (oldUUID != null) {
            //If there was a UUID stored and prepped to be invalidated, remove it from the UUIDS we are trying to invalidate
            // so that it is able to continue being used/sync'd to the client
            uuidsToInvalidate.remove(oldUUID);
        } else {
            // otherwise, create a new uuid for use with this item type
            itemTypeLookup.put(type, UUID.randomUUID());
        }
        return new QIOItemTypeData(type);
    }

    @Override
    public long massExtract(ItemStack stack, long amount, Action action) {
        if (amount <= 0 || stack.isEmpty() || itemDataMap.isEmpty()) {
            return 0;
        }
        HashedItem type = HashedItem.raw(stack);
        QIOItemTypeData data = itemDataMap.get(type);
        if (data == null) {
            return 0;
        }
        long removed = data.remove(amount, action);
        // remove this item type if it's now empty
        if (action.execute() && data.count == 0) {
            removeItemData(data.itemType);
        }
        return removed;
    }

    public ItemStack removeItem(int amount) {
        return removeByType(null, amount);
    }

    public ItemStack removeItem(ItemStack stack, int amount) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return removeByType(HashedItem.raw(stack), amount);
    }

    public ItemStack removeByType(@Nullable HashedItem itemType, int amount) {
        if (itemDataMap.isEmpty() || amount <= 0) {
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
            removeItemData(data.itemType);
        }
        return removed;
    }

    private void removeItemData(HashedItem type) {
        itemDataMap.remove(type);
        //If the item has a UUID that corresponds to it, add that UUID to our list of uuids to invalidate
        UUID toInvalidate = getUUIDForType(type);
        if (toInvalidate != null) {
            uuidsToInvalidate.add(toInvalidate);
        }
        //Note: We need to copy the tags to a new collection as otherwise when we start removing them from the lookup
        // they will also get removed from this view
        Set<String> tags = new HashSet<>(tagLookupMap.getKeys(type));
        if (tagLookupMap.removeValue(type) && !tagLookupMap.hasAllKeys(tags)) {
            //If we completely removed any tags clear our wildcard cache as it may have some wildcards that are
            // matching a tag that is no longer stored
            tagWildcardCache.clear();
            //Note: We don't need to clear the failed wildcard tags as if we are removing tags they still won't have any matches
        }
        ItemStack stack = type.getStack();
        String modID = MekanismUtils.getModId(stack);
        Set<HashedItem> itemsForMod = modIDLookupMap.get(modID);
        //In theory if we are removing an item, and it existed we should have a set corresponding to it,
        // but double check that it is not null just in case
        // Next if we removed the item successfully, check if the items for that mod is now empty, and if they are
        // remove the modid from the lookup map, and clear our wildcard cache as it may have some wildcards that are
        // matching a modid that is no longer stored
        if (itemsForMod != null && itemsForMod.remove(type) && itemsForMod.isEmpty()) {
            modIDLookupMap.remove(modID);
            modIDWildcardCache.clear();
            //Note: We don't need to clear the failed wildcard modids as if we are removing tags they still won't have any matches
        }
        Item item = stack.getItem();
        Set<HashedItem> itemsByFuzzy = fuzzyItemLookupMap.get(item);
        //In theory if we are removing an item, and it existed we should have a set corresponding to it,
        // but double check that it is not null just in case
        // Next if we removed the item successfully, check if the "fuzzy" items for that item is now empty, and if they are
        // remove the item completely from the lookup map
        if (itemsByFuzzy != null && itemsByFuzzy.remove(type) && itemsByFuzzy.isEmpty()) {
            fuzzyItemLookupMap.remove(item);
        }
    }

    public Set<HashedItem> getTypesForItem(Item item) {
        return Collections.unmodifiableSet(fuzzyItemLookupMap.getOrDefault(item, Collections.emptySet()));
    }

    public Object2LongMap<HashedItem> getStacksByItem(Item item) {
        return getStacksWithCounts(fuzzyItemLookupMap.get(item));
    }

    public Object2LongMap<HashedItem> getStacksByTag(String tag) {
        return getStacksWithCounts(tagLookupMap.getValues(tag));
    }

    public Object2LongMap<HashedItem> getStacksByModID(String modID) {
        return getStacksWithCounts(modIDLookupMap.get(modID));
    }

    private Object2LongMap<HashedItem> getStacksWithCounts(@Nullable Set<HashedItem> items) {
        if (items == null || items.isEmpty()) {
            return Object2LongMaps.emptyMap();
        }
        Object2LongMap<HashedItem> ret = new Object2LongOpenHashMap<>();
        for (HashedItem item : items) {
            ret.put(item, getStored(item));
        }
        return ret;
    }

    public Object2LongMap<HashedItem> getStacksByTagWildcard(String wildcard) {
        if (hasMatchingElements(tagWildcardCache, failedWildcardTags, wildcard, tagLookupMap::getAllKeys)) {
            Object2LongMap<HashedItem> ret = new Object2LongOpenHashMap<>();
            for (String match : tagWildcardCache.get(wildcard)) {
                for (HashedItem item : tagLookupMap.getValues(match)) {
                    //If our return map doesn't already have the stored value in it, calculate it.
                    // The case where it may have the stored value in it is if an item has multiple
                    // tags that all match the wildcard
                    ret.computeIfAbsent(item, (HashedItem type) -> getStored(type));
                }
            }
            return ret;
        }
        return Object2LongMaps.emptyMap();
    }

    public Object2LongMap<HashedItem> getStacksByModIDWildcard(String wildcard) {
        if (hasMatchingElements(modIDWildcardCache, failedWildcardModIDs, wildcard, modIDLookupMap::keySet)) {
            Object2LongMap<HashedItem> ret = new Object2LongOpenHashMap<>();
            for (String match : modIDWildcardCache.get(wildcard)) {
                for (HashedItem item : modIDLookupMap.get(match)) {
                    //Note: Unlike in getStacksByTagWildcard, we don't use computeLongIfAbsent here because
                    // each stack only has one modid, so while we may have multiple modids that match our
                    // wildcard, the stacks that correspond to said modids will be unique
                    ret.put(item, getStored(item));
                }
            }
            return ret;
        }
        return Object2LongMaps.emptyMap();
    }

    private boolean hasMatchingElements(SetMultimap<String, String> wildcardCache, Set<String> failedWildcards, String wildcard, Supplier<Set<String>> entriesSupplier) {
        if (failedWildcards.contains(wildcard)) {
            //If we already know this wildcard has no matching things, fail fast
            return false;
        }
        //If we don't have a cached value for the given wildcard, try to build up the corresponding cache
        if (!wildcardCache.containsKey(wildcard) && !buildWildcardMapping(wildcardCache, wildcard, entriesSupplier.get())) {
            // If we don't actually have any matches, mark that the wildcard failed, and return false
            failedWildcards.add(wildcard);
            return false;
        }
        return true;
    }

    /**
     * @return {@code true} if any wildcards were added.
     */
    private boolean buildWildcardMapping(SetMultimap<String, String> wildcardCache, String wildcard, Set<String> entries) {
        boolean added = false;
        for (String entry : entries) {
            if (WildcardMatcher.matches(wildcard, entry)) {
                added |= wildcardCache.put(wildcard, entry);
            }
        }
        return added;
    }

    public void openItemViewer(ServerPlayer player) {
        playersViewingItems.add(player);
        Object2LongMap<UUIDAwareHashedItem> map = new Object2LongOpenHashMap<>();
        for (QIOItemTypeData data : itemDataMap.values()) {
            map.put(new UUIDAwareHashedItem(data.itemType, getUUIDForType(data.itemType)), data.count);
        }
        Mekanism.packetHandler().sendTo(PacketQIOItemViewerGuiSync.batch(map, totalCountCapacity, totalTypeCapacity), player);
    }

    public void closeItemViewer(ServerPlayer player) {
        playersViewingItems.remove(player);
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
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

    @Override
    public long getStored(ItemStack type) {
        return getStored(HashedItem.raw(type));
    }

    public long getStored(HashedItem itemType) {
        QIOItemTypeData data = itemDataMap.get(itemType);
        return data == null ? 0 : data.count;
    }

    public QIODriveData getDriveData(QIODriveKey key) {
        return driveMap.get(key);
    }

    /**
     * This is mainly for use by things that need to do simulation, and should not have any of the values of the drive get changed directly.
     */
    public Collection<QIODriveData> getAllDrives() {
        return driveMap.values();
    }

    @Override
    public void tick() {
        super.tick();
        if (!uuidsToInvalidate.isEmpty()) {
            //If we have uuids we need to invalidate the Item UUID pairing of them
            for (UUID uuidToInvalidate : uuidsToInvalidate) {
                itemTypeLookup.inverse().remove(uuidToInvalidate);
            }
            uuidsToInvalidate.clear();
        }
        if (!updatedItems.isEmpty() || needsUpdate) {
            Object2LongMap<UUIDAwareHashedItem> map = new Object2LongOpenHashMap<>();
            updatedItems.forEach(type -> {
                QIOItemTypeData data = itemDataMap.get(type);
                map.put(type, data == null ? 0 : data.count);
            });
            // flush players that somehow didn't send a container close packet
            playersViewingItems.removeIf(player -> !(player.containerMenu instanceof QIOItemViewerContainer));
            playersViewingItems.forEach(player -> Mekanism.packetHandler().sendTo(PacketQIOItemViewerGuiSync.update(map, totalCountCapacity, totalTypeCapacity), player));
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
            //Note: We only need to clear tags here as the modids cannot change just because a reload happened
            tagLookupMap.clear();
            tagWildcardCache.clear();
            itemDataMap.values().forEach(item -> tagLookupMap.putAll(TagCache.getItemTags(item.itemType.getStack()), item.itemType));
        }
    }

    @Override
    public void onDeactivate(BlockEntity tile) {
        super.onDeactivate(tile);

        if (tile instanceof IQIODriveHolder holder) {
            for (int i = 0; i < holder.getDriveSlots().size(); i++) {
                QIODriveKey key = new QIODriveKey(holder, i);
                removeDrive(key, true);
                driveMap.remove(key);
            }
        }
    }

    @Override
    public void update(BlockEntity tile) {
        super.update(tile);
        if (tile instanceof IQIODriveHolder holder && !driveHolders.contains(holder)) {
            addHolder(holder);
        }
    }

    @Override
    public void onRemove() {
        super.onRemove();
        // copy keys to avoid CME
        Set<QIODriveKey> keys = new HashSet<>(driveMap.keySet());
        keys.forEach(key -> removeDrive(key, false));
        driveMap.clear();
        playersViewingItems.forEach(player -> Mekanism.packetHandler().sendTo(PacketQIOItemViewerGuiSync.kill(), player));
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
    public void write(FriendlyByteBuf buf) {
        super.write(buf);
        buf.writeVarLong(totalCount);
        buf.writeVarLong(totalCountCapacity);
        buf.writeVarInt(itemDataMap.size());
        buf.writeVarInt(totalTypeCapacity);
        buf.writeEnum(color);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        super.read(buf);
        totalCount = buf.readVarLong();
        totalCountCapacity = buf.readVarLong();
        clientTypes = buf.readVarInt();
        totalTypeCapacity = buf.readVarInt();
        setColor(buf.readEnum(EnumColor.class));
    }

    @Override
    public void write(CompoundTag nbtTags) {
        super.write(nbtTags);
        NBTUtils.writeEnum(nbtTags, NBTConstants.COLOR, color);
    }

    @Override
    protected void read(CompoundTag nbtTags) {
        super.read(nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.COLOR, EnumColor::byIndexStatic, this::setColor);
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
                itemDataMap.computeIfAbsent(storedKey, this::createTypeDataForAbsent).addFromDrive(data, value);
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
                    updatedItems.add(new UUIDAwareHashedItem(storedKey, getUUIDForType(storedKey)));
                    // remove this entry from the item data map if it's now empty
                    if (itemData.containingDrives.isEmpty() || itemData.count == 0) {
                        removeItemData(storedKey);
                    }
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

        private long add(long amount, Action action) {
            long toAdd = amount;
            // first we try to add the items to an already-containing drive
            for (QIODriveKey key : containingDrives) {
                toAdd = addItemsToDrive(toAdd, driveMap.get(key), action);
                if (toAdd == 0) {
                    break;
                }
            }
            // next, we add the items to any drive that will take it
            if (toAdd > 0) {
                for (QIODriveData data : driveMap.values()) {
                    if (!containingDrives.contains(data.getKey())) {
                        toAdd = addItemsToDrive(toAdd, data, action);
                        if (toAdd == 0) {
                            break;
                        }
                    }
                }
            }
            if (action.execute()) {
                // update internal/core values
                count += amount - toAdd;
                totalCount += amount - toAdd;
                setNeedsUpdate(itemType);
            }
            return toAdd;
        }

        private long addItemsToDrive(long toAdd, QIODriveData data, Action action) {
            long rejects = data.add(itemType, toAdd, action);
            if (action.execute() && rejects < toAdd) {
                containingDrives.add(data.getKey());
            }
            return rejects;
        }

        private long remove(long amount, Action action) {
            long removed = 0;
            for (Iterator<QIODriveKey> iter = containingDrives.iterator(); iter.hasNext(); ) {
                QIODriveData data = driveMap.get(iter.next());
                removed += data.remove(itemType, amount - removed, action);
                // remove this drive from containingDrives if it doesn't have this item anymore
                if (action.execute() && data.getStored(itemType) == 0) {
                    iter.remove();
                }
                // break early if we found enough items
                if (removed == amount) {
                    break;
                }
            }
            if (action.execute()) {
                count -= removed;
                totalCount -= removed;
                setNeedsUpdate(itemType);
            }
            return removed;
        }

        private ItemStack remove(int amount) {
            int removed = MathUtils.clampToInt(remove(amount, Action.EXECUTE));
            return removed == 0 ? ItemStack.EMPTY : itemType.createStack(removed);
        }

        public long getCount() {
            return count;
        }
    }
}
