package mekanism.common.content.qio;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SequencedMap;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import mekanism.api.SerializationConstants;
import mekanism.api.inventory.qio.IQIOFrequency;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.TagCache;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.slot.QIODriveSlot;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.lib.collection.BiMultimap;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.lib.inventory.UUIDItemResource;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.network.to_client.qio.PacketUpdateItemViewer;
import mekanism.common.util.MekanismUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class QIOFrequency extends Frequency implements IColorableFrequency, IQIOFrequency, TickableFrequency {

    private static final RandomSource rand = RandomSource.create();
    public static final Codec<QIOFrequency> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance)
          .and(EnumColor.CODEC.fieldOf(SerializationConstants.COLOR).forGetter(QIOFrequency::getColor))
          .apply(instance, (name, owner, securityMode, color) -> {
              QIOFrequency frequency = new QIOFrequency(name, owner.orElse(null), securityMode);
              frequency.color = color;
              return frequency;
          }));
    public static final StreamCodec<ByteBuf, QIOFrequency> STREAM_CODEC = StreamCodec.composite(
          baseStreamCodec(QIOFrequency::new), Function.identity(),
          ByteBufCodecs.VAR_LONG, QIOFrequency::getTotalItemCount,
          ByteBufCodecs.VAR_LONG, QIOFrequency::getTotalItemCountCapacity,
          ByteBufCodecs.VAR_INT, freq -> freq.getTotalItemTypes(false),
          ByteBufCodecs.VAR_INT, QIOFrequency::getTotalItemTypeCapacity,
          EnumColor.STREAM_CODEC, QIOFrequency::getColor,
          (frequency, totalCount, totalCountCapacity, totalTypes, totalTypeCapacity, color) -> {
              frequency.totalCount = totalCount;
              frequency.totalCountCapacity = totalCountCapacity;
              frequency.clientTypes = totalTypes;
              frequency.totalTypeCapacity = totalTypeCapacity;
              frequency.color = color;
              return frequency;
          }
    );

    private final SequencedMap<QIODriveKey, QIODriveData> driveMap = new LinkedHashMap<>();
    private final SequencedMap<ItemResource, QIOItemTypeData> itemDataMap = new LinkedHashMap<>();
    private final Set<IQIODriveHolder> driveHolders = new HashSet<>();
    // efficiently keep track of the tags utilized by the items stored
    private final BiMultimap<String, ItemResource> tagLookupMap = new BiMultimap<>();
    // efficiently keep track of the modids utilized by the items stored
    private final Map<String, Set<ItemResource>> modIDLookupMap = new HashMap<>();
    // efficiently keep track of the items for use in fuzzy lookup utilized by the items stored
    private final Map<Item, Set<ItemResource>> fuzzyItemLookupMap = new IdentityHashMap<>();
    // a sensitive cache for wildcard tag lookups (wildcard -> [matching tags])
    private final SetMultimap<String, String> tagWildcardCache = HashMultimap.create();
    private final Set<String> failedWildcardTags = new HashSet<>();
    // a sensitive cache for wildcard modid lookups (wildcard -> [matching modids])
    private final SetMultimap<String, String> modIDWildcardCache = HashMultimap.create();
    private final Set<String> failedWildcardModIDs = new HashSet<>();

    private final Set<UUID> updatedItems = new HashSet<>();
    private final Set<ServerPlayer> playersViewingItems = new HashSet<>();

    /** If we need to send a packet to viewing clients with changed item data. */
    private boolean needsUpdate;
    /** If we have new item changes that haven't been saved. */
    private boolean isDirty;//todo rename this so it's clearer what the difference is from Frequency.dirty

    private long totalCount, totalCountCapacity;
    private int totalTypeCapacity;
    // only used on client side, for server side we can just look at itemDataMap.size()
    private int clientTypes;
    private HolderLookup.Provider registries = null;//set by update

    private EnumColor color = EnumColor.INDIGO;

    /**
     * @param uuid Should only be null if we have incomplete data that we are loading
     */
    public QIOFrequency(String n, @Nullable UUID uuid, SecurityMode securityMode) {
        super(FrequencyTypes.QIO, n, uuid, securityMode);
    }

    private QIOFrequency(String name, @Nullable UUID owner, String ownerName, SecurityMode securityMode) {
        super(FrequencyTypes.QIO, name, owner, ownerName, securityMode);
    }

    /**
     * Dangerous function. Don't mess with this map.
     *
     * @return core item data map, tracking item types + their respective counts and containing drives
     */
    public Map<ItemResource, QIOItemTypeData> getItemDataMap() {
        return itemDataMap;
    }

    @Override
    public void forAllStored(ObjLongConsumer<ItemStack> consumer) {
        for (Entry<ItemResource, QIOItemTypeData> entry : itemDataMap.entrySet()) {
            consumer.accept(entry.getKey().toStack(), entry.getValue().getCount());
        }
    }

    @Override
    public void forAllStoredTypes(ObjLongConsumer<ItemResource> consumer) {
        for (Entry<ItemResource, QIOItemTypeData> entry : itemDataMap.entrySet()) {
            consumer.accept(entry.getKey(), entry.getValue().getCount());
        }
    }

    @Override
    public long massInsert(ItemResource itemType, long amount, TransactionContext transaction) {
        if (amount <= 0 || itemType.isEmpty()) {
            return 0;
        } else if (totalCount == totalCountCapacity) {
            // this check and the one below for if the map contains the type are extremely important; they prevent us from wasting CPU searching for a place to put the new items
            return 0;
        }
        QIOItemTypeData data = itemDataMap.get(itemType);
        if (data == null) {
            if (itemDataMap.size() == totalTypeCapacity) {
                //Don't add any ghost item types if there is no room for new ones. We do this inside of a computeIfAbsent
                // so that we don't have to check if the map contains it twice
                //Failed to insert
                return 0;
            } else {
                // at this point we're guaranteed at least part of the input stack will be inserted
                data = createTypeDataForAbsent(itemType);
                itemDataMap.put(itemType, data);
            }
        }
        long inserted = data.add(amount, transaction);
        //TODO - 26.1: Make this part of the snapshot as well
        totalCount += inserted;
        return inserted;
    }

    /**
     * @return Amount inserted
     */
    public int addItem(ItemResource itemType, int amount, TransactionContext transaction) {
        //Note: As the passed in amount is an int, this should always be able to be easily cast to an int
        // We clamp it just in case though
        return Ints.saturatedCast(massInsert(itemType, amount, transaction));
    }

    private QIOItemTypeData createTypeDataForAbsent(ItemResource type) {
        //TODO - 26.1: Do we only want to add these lookup maps when the transaction's root commit is done?
        List<String> tags = TagCache.getItemTags(type);
        if (!tags.isEmpty()) {
            boolean hasAllKeys = tagLookupMap.hasAllKeys(tags);
            if (tagLookupMap.putAll(tags, type) && !hasAllKeys) {
                //If we added any tag item combinations, and we didn't have all the keys for tags this item has,
                // then we need to clear our wildcard cache as our new tags may be valid for some of our wildcards
                tagWildcardCache.clear();
                failedWildcardTags.clear();
            }
        }
        String modID = MekanismUtils.getModId(this.registries, type.toStack());
        Set<ItemResource> modItems = modIDLookupMap.get(modID);
        if (modItems == null) {
            //If we added a new modid to the lookup map we also want to make sure that we clear our modid wildcard cache
            // as our new modid may be valid for some of our wildcards
            modIDWildcardCache.clear();
            failedWildcardModIDs.clear();
            modItems = new HashSet<>();
            modIDLookupMap.put(modID, modItems);
        }
        modItems.add(type);
        //Fuzzy item lookup has no wildcard cache related to it
        fuzzyItemLookupMap.computeIfAbsent(type.getItem(), _ -> new HashSet<>()).add(type);
        QIOItemTypeData data = new QIOItemTypeData(type);
        //Ensure we have a matching uuid for this item
        data.getItemUUID();
        return data;
    }

    @Override
    public long massExtract(ItemResource itemType, long amount, TransactionContext transaction) {
        if (amount <= 0 || itemType.isEmpty() || itemDataMap.isEmpty()) {
            return 0;
        }
        QIOItemTypeData data = itemDataMap.get(itemType);
        if (data == null) {
            return 0;
        }
        long extracted = data.remove(amount, transaction);
        //TODO - 26.1: Make this part of the snapshot as well
        totalCount -= extracted;
        return extracted;
    }

    public int removeByType(ItemResource itemType, int amount, TransactionContext transaction) {
        //Note: As the passed in amount is an int, this should always be able to be easily cast to an int
        // We clamp it just in case though
        return Ints.saturatedCast(massExtract(itemType, amount, transaction));
    }

    private void removeItemData(ItemResource type) {
        itemDataMap.remove(type);
        //Note: We need to copy the tags to a new collection as otherwise when we start removing them from the lookup
        // they will also get removed from this view
        Set<String> tags = new HashSet<>(tagLookupMap.getKeys(type));
        if (tagLookupMap.removeValue(type) && !tagLookupMap.hasAllKeys(tags)) {
            //If we completely removed any tags clear our wildcard cache as it may have some wildcards that are
            // matching a tag that is no longer stored
            tagWildcardCache.clear();
            //Note: We don't need to clear the failed wildcard tags as if we are removing tags they still won't have any matches
        }
        String modID = MekanismUtils.getModId(this.registries, type.toStack());
        Set<ItemResource> itemsForMod = modIDLookupMap.get(modID);
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
        Item item = type.getItem();
        Set<ItemResource> itemsByFuzzy = fuzzyItemLookupMap.get(item);
        //In theory if we are removing an item, and it existed we should have a set corresponding to it,
        // but double check that it is not null just in case
        // Next if we removed the item successfully, check if the "fuzzy" items for that item is now empty, and if they are
        // remove the item completely from the lookup map
        if (itemsByFuzzy != null && itemsByFuzzy.remove(type) && itemsByFuzzy.isEmpty()) {
            fuzzyItemLookupMap.remove(item);
        }
    }

    public Set<ItemResource> getTypesForItem(Item item) {
        return fuzzyItemLookupMap.getOrDefault(item, Collections.emptySet());
    }

    public Object2LongMap<ItemResource> getStacksByItem(Item item) {
        return getStacksWithCounts(fuzzyItemLookupMap.get(item));
    }

    public Object2LongMap<ItemResource> getStacksByTag(String tag) {
        return getStacksWithCounts(tagLookupMap.getValues(tag));
    }

    public Object2LongMap<ItemResource> getStacksByModID(String modID) {
        return getStacksWithCounts(modIDLookupMap.get(modID));
    }

    private Object2LongMap<ItemResource> getStacksWithCounts(@Nullable Set<ItemResource> items) {
        if (items == null || items.isEmpty()) {
            return Object2LongMaps.emptyMap();
        }
        Object2LongMap<ItemResource> ret = new Object2LongOpenHashMap<>();
        for (ItemResource item : items) {
            ret.put(item, getStored(item));
        }
        return ret;
    }

    public Object2LongMap<ItemResource> getStacksByTagWildcard(String wildcard) {
        if (hasMatchingElements(tagWildcardCache, failedWildcardTags, wildcard, tagLookupMap::getAllKeys)) {
            Object2LongMap<ItemResource> ret = new Object2LongOpenHashMap<>();
            ToLongFunction<ItemResource> storedFunction = this::getStored;
            for (String match : tagWildcardCache.get(wildcard)) {
                for (ItemResource item : tagLookupMap.getValues(match)) {
                    //If our return map doesn't already have the stored value in it, calculate it.
                    // The case where it may have the stored value in it is if an item has multiple
                    // tags that all match the wildcard
                    ret.computeIfAbsent(item, storedFunction);
                }
            }
            return ret;
        }
        return Object2LongMaps.emptyMap();
    }

    public Object2LongMap<ItemResource> getStacksByModIDWildcard(String wildcard) {
        if (hasMatchingElements(modIDWildcardCache, failedWildcardModIDs, wildcard, modIDLookupMap::keySet)) {
            Object2LongMap<ItemResource> ret = new Object2LongOpenHashMap<>();
            for (String match : modIDWildcardCache.get(wildcard)) {
                for (ItemResource item : modIDLookupMap.get(match)) {
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
        if (this.color != color) {
            this.color = color;
            this.dirty = true;
        }
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
    public long getStored(ItemResource itemType) {
        if (itemType.isEmpty()) {
            return 0;
        }
        QIOItemTypeData data = itemDataMap.get(itemType);
        return data == null ? 0 : data.count;
    }

    public boolean isStoring(ItemResource itemType) {
        return getStored(itemType) > 0;
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
    public boolean tick(boolean tickingNormally) {
        if (getSecurity() == SecurityMode.TRUSTED && !playersViewingItems.isEmpty()) {
            //TODO - 1.20.4: Only perform every so often?
            SecurityFrequency security = FrequencyTypes.SECURITY.getLookup(null, SecurityMode.PUBLIC).getFrequency(getOwner());
            if (security != null) {
                for (ServerPlayer player : new HashSet<>(playersViewingItems)) {
                    if (!ownerMatches(player.getUUID()) && !security.isTrusted(player.getUUID()) && player.containerMenu instanceof QIOItemViewerContainer) {
                        player.closeContainer();
                        closeItemViewer(player);
                    }
                }
            }
        }

        if (!updatedItems.isEmpty() || needsUpdate) {
            //Only calculate the packet and the update map if there are actually players viewing this frequency,
            // otherwise we can just skip looking up UUIDs and counts
            Lazy<PacketUpdateItemViewer> lazyPacket = Lazy.of(() -> {
                Object2LongMap<UUIDItemResource> map = new Object2LongOpenHashMap<>(updatedItems.size());
                for (UUID uuid : updatedItems) {
                    ItemResource type = QIOGlobalItemLookup.instance().getTypeByUUID(uuid);
                    if (!type.isEmpty()) {//The type should never be empty as we create a UUID if there isn't one before adding but validate it
                        QIOItemTypeData data = itemDataMap.get(type);
                        map.put(new UUIDItemResource(uuid, type), data == null ? 0 : data.count);
                    }
                }
                return new PacketUpdateItemViewer(totalCountCapacity, totalTypeCapacity, map);
            });
            for (Iterator<ServerPlayer> viewingIterator = playersViewingItems.iterator(); viewingIterator.hasNext(); ) {
                ServerPlayer player = viewingIterator.next();
                if (player.containerMenu instanceof QIOItemViewerContainer) {
                    PacketDistributor.sendToPlayer(player, lazyPacket.get());
                } else {
                    //flush players that somehow didn't send a container close packet
                    viewingIterator.remove();
                }
            }
            updatedItems.clear();
            needsUpdate = false;
        }
        // if something has changed, we'll subsequently randomly run a save operation in the next 100 ticks.
        // the random factor helps us avoid bogging down the CPU by saving all QIO frequencies at once
        // this isn't a fully necessary operation, but it'll help avoid all item data getting lost if the server
        // is forcibly shut down.
        if (isDirty && rand.nextInt(5 * SharedConstants.TICKS_PER_SECOND) == 0) {
            //Note: We don't have this affect our super dirty value as this is for if the drives are dirty,
            // not for if the frequency is dirty
            saveAll();
            isDirty = false;
        }

        if (CommonWorldTickHandler.flushTagAndRecipeCaches) {
            //Note: We only need to clear tags here as the modids cannot change just because a reload happened
            tagLookupMap.clear();
            tagWildcardCache.clear();
            for (QIOItemTypeData item : itemDataMap.values()) {
                tagLookupMap.putAll(TagCache.getItemTags(item.itemType), item.itemType);
            }
        }
        return dirty;
    }

    @Override
    public boolean onDeactivate(BlockEntity tile) {
        boolean changedData = super.onDeactivate(tile);
        if (tile instanceof IQIODriveHolder holder) {
            for (int i = 0, size = holder.getDriveSlots().size(); i < size; i++) {
                QIODriveKey key = new QIODriveKey(holder, i);
                removeDrive(key, true);
            }
            //Uncache the holder when it stops being part of the frequency
            driveHolders.remove(holder);
        }
        return changedData;
    }

    @Override
    public boolean update(BlockEntity tile) {
        this.registries = tile.getLevel().registryAccess();
        boolean changedData = super.update(tile);
        if (tile instanceof IQIODriveHolder holder && driveHolders.add(holder)) {
            List<QIODriveSlot> driveSlots = holder.getDriveSlots();
            for (int i = 0, slots = driveSlots.size(); i < slots; i++) {
                addDrive(new QIODriveKey(holder, i), driveSlots.get(i).getResource());
            }
        }
        return changedData;
    }

    @Override
    public void onRemove() {
        super.onRemove();
        // copy keys to avoid CME
        Set<QIODriveKey> keys = new HashSet<>(driveMap.keySet());
        for (QIODriveKey key : keys) {
            removeDrive(key, false);
        }
        driveMap.clear();
        for (ServerPlayer player : playersViewingItems) {
            Mekanism.packetHandler().killItemViewer(player);
        }
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

    public void addDrive(QIODriveKey key, ItemResource driveData) {
        if (driveData.getItem() instanceof IQIODriveItem) {
            // if a drive in this position is already in the system, we remove it before adding this one
            if (driveMap.containsKey(key)) {
                removeDrive(key, true);
            }
            // add drive and capacity info to core tracking
            QIODriveData data = new QIODriveData(key, driveData);
            totalCountCapacity += data.getCountCapacity();
            totalTypeCapacity += data.getTypeCapacity();
            driveMap.put(key, data);
            data.forDriveContents(key, this::addDriveContent);
            setNeedsUpdate();
        }
    }

    private void addDriveContent(QIODriveKey key, ItemResource storedType, long amountStored) {
        QIOItemTypeData typeData = itemDataMap.computeIfAbsent(storedType, this::createTypeDataForAbsent);
        totalCount += amountStored;
        typeData.driveAdded(key, amountStored);
        markForUpdate(storedType);
    }

    @Deprecated(forRemoval = true)//TODO - 26.1: Re-evaluate usages and fix things so that the proper stack gets updated
    public void removeDrive(QIODriveKey key, boolean updateItemMap) {
        removeDrive(key, updateItemMap, key.holder().getDriveSlots().get(key.driveSlot()).getResource());
    }

    public void removeDrive(QIODriveKey key, boolean updateItemMap, ItemResource driveData) {
        if (!driveMap.containsKey(key)) {
            return;
        }
        QIODriveData data = driveMap.get(key);
        if (updateItemMap) {
            data.forDriveContents(key, this::removeDriveContent);
            setNeedsUpdate();
        }
        // remove drive and capacity info from core tracking
        totalCountCapacity -= data.getCountCapacity();
        totalTypeCapacity -= data.getTypeCapacity();
        driveMap.remove(key);
        // save the item list onto the physical drive
        //TODO - 26.1: When doing it from the drive slot, we need to do this before extraction (and maybe even getting of the resource type??)
        key.save(data);
    }

    private void removeDriveContent(QIODriveKey key, ItemResource storedType, long amountStored) {
        QIOItemTypeData itemData = itemDataMap.get(storedType);
        if (itemData != null) {
            itemData.containingDrives.remove(key);
            itemData.count -= amountStored;
            totalCount -= amountStored;
            markForUpdate(storedType);
            // remove this entry from the item data map if it's now empty
            if (itemData.containingDrives.isEmpty() || itemData.count == 0) {
                removeItemData(storedType);
            }
        }
    }

    public void saveAll() {
        for (Entry<QIODriveKey, QIODriveData> entry : driveMap.entrySet()) {
            QIODriveKey key = entry.getKey();
            QIODriveData value = entry.getValue();
            key.save(value);
        }
    }

    private void setNeedsUpdate(@Nullable ItemResource changedItem) {
        isDirty = true;
        if (!playersViewingItems.isEmpty()) {//Skip marking for update if there are no players viewing the items
            needsUpdate = true;
            if (changedItem != null) {
                UUID uuid = QIOGlobalItemLookup.instance().getUUIDForType(changedItem);
                if (uuid != null) {
                    updatedItems.add(uuid);
                }
            }
        }
    }

    private void markForUpdate(ItemResource changedItem) {
        if (!playersViewingItems.isEmpty()) {//Skip marking for update if there are no players viewing the items
            UUID uuid = QIOGlobalItemLookup.instance().getUUIDForType(changedItem);
            if (uuid != null) {
                updatedItems.add(uuid);
            }
        }
    }

    private void setNeedsUpdate() {
        setNeedsUpdate(null);
    }

    public class QIOItemTypeData extends SnapshotJournal<QIOItemTypeData.Snapshot> {

        private final Set<QIODriveKey> containingDrives = new HashSet<>();
        private final ItemResource itemType;

        @Nullable
        private UUID itemUUID;
        private long count = 0;

        public QIOItemTypeData(ItemResource itemType) {
            this.itemType = itemType;
        }

        private void driveAdded(QIODriveKey driveKey, long toAdd) {
            count += toAdd;
            containingDrives.add(driveKey);
        }

        private long add(long amount, TransactionContext transaction) {
            long added = 0;
            boolean hasUpdated = false;
            // first we try to add the items to an already-containing drive
            for (QIODriveKey key : containingDrives) {
                added += driveMap.get(key).add(itemType, amount - added, transaction);
                if (added == amount) {
                    break;
                }
            }
            // next, we add the items to any drive that will take it
            if (added < amount) {
                for (Map.Entry<QIODriveKey, QIODriveData> entry : driveMap.entrySet()) {
                    QIODriveKey driveKey = entry.getKey();
                    QIODriveData data = entry.getValue();
                    if (!containingDrives.contains(driveKey)) {
                        long addedToDrive = data.add(itemType, amount - added, transaction);
                        if (addedToDrive > 0) {
                            if (!hasUpdated) {
                                //If we haven't updated the snapshot for this item type yet, do so
                                updateSnapshots(transaction);
                                hasUpdated = true;
                            }
                            //Add the drive to the list of drives that contain this item
                            containingDrives.add(driveKey);
                        }
                        added += addedToDrive;
                        if (added == amount) {
                            break;
                        }
                    }
                }
            }
            if (added > 0) {
                if (!hasUpdated) {
                    //If we haven't updated the snapshot for this item type yet, do so
                    updateSnapshots(transaction);
                }
                // update internal/core values
                count += added;
            }
            return added;
        }

        private long remove(long amount, TransactionContext transaction) {
            long removed = 0;
            boolean hasUpdated = false;
            for (Iterator<QIODriveKey> iter = containingDrives.iterator(); iter.hasNext(); ) {
                QIODriveData data = driveMap.get(iter.next());
                removed += data.remove(itemType, amount - removed, transaction);
                // remove this drive from containingDrives if it doesn't have this item anymore
                if (data.getStored(itemType) == 0) {
                    if (!hasUpdated) {
                        //If we haven't updated the snapshot for this item type yet, do so
                        updateSnapshots(transaction);
                        hasUpdated = true;
                    }
                    iter.remove();
                }
                // break early if we found enough items
                if (removed == amount) {
                    break;
                }
            }
            if (removed > 0) {
                if (!hasUpdated) {
                    //If we haven't updated the snapshot for this item type yet, do so
                    updateSnapshots(transaction);
                }
                count -= removed;
            }
            return removed;
        }

        public long getCount() {
            return count;
        }

        public UUID getItemUUID() {
            if (itemUUID == null) {
                //Lazily cache what the uuid for the stack is
                itemUUID = QIOGlobalItemLookup.instance().getOrTrackUUID(itemType);
            }
            return itemUUID;
        }

        public ItemResource getItemType() {
            return itemType;
        }

        @Override
        protected QIOItemTypeData.Snapshot createSnapshot() {
            return new QIOItemTypeData.Snapshot(count, new HashSet<>(containingDrives));
        }

        @Override
        protected void revertToSnapshot(QIOItemTypeData.Snapshot snapshot) {
            //TODO - 26.1: Re-evaluate this impl
            count = snapshot.count();
            containingDrives.clear();
            containingDrives.addAll(snapshot.containingDrives());
            //TODO - 26.1: Do we also need to roll back whether the itemType is present or not for the frequency?
            if (count == 0) {
                //TODO - 26.1: Is this correct to do it when reverting? Or is it supposed to still be present for a bit
                //If we end up with having nothing remove the tracking for this item type
                removeItemData(itemType);
            }
        }

        @Override
        protected void onRootCommit(QIOItemTypeData.Snapshot originalState) {
            super.onRootCommit(originalState);
            if (originalState.count() != count || !containingDrives.equals(originalState.containingDrives())) {
                if (count == 0) {
                    // remove this item type if it's now empty
                    removeItemData(itemType);
                }

                //TODO - 26.1: is marking it as needing an update handled by the remove item data path?
                // Also should we mark this at the start of this method or at the end here
                setNeedsUpdate(itemType);
            }
        }

        public record Snapshot(long count, Set<QIODriveKey> containingDrives) {
        }
    }
}
