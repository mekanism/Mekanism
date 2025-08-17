package mekanism.common.lib.multiblock;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.api.SerializationConstants;
import mekanism.common.Mekanism;
import mekanism.common.lib.MekanismSavedData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = Mekanism.MODID)
public class MultiblockManager<T extends MultiblockData> {

    private static final Set<MultiblockManager<?>> managers = new HashSet<>();

    private final String name;
    private final String nameLower;

    private final Supplier<MultiblockCache<T>> cacheSupplier;
    private final Supplier<IStructureValidator<T>> validatorSupplier;

    /**
     * A map containing references to all multiblock inventory caches.
     */
    private final Map<UUID, MultiblockCache<T>> caches = new HashMap<>();

    private final Queue<T> multiblocksTicked = new ArrayDeque<>();

    /**
     * Note: This can and will be null on the client side
     */
    @Nullable
    private MultiblockCacheDataHandler dataHandler;

    public MultiblockManager(String name, Supplier<MultiblockCache<T>> cacheSupplier, Supplier<IStructureValidator<T>> validatorSupplier) {
        this.name = name;
        this.nameLower = name.toLowerCase(Locale.ROOT);
        this.cacheSupplier = cacheSupplier;
        this.validatorSupplier = validatorSupplier;
        managers.add(this);
    }

    /**
     * Note: It is important that callers also call {@link #trackCache(UUID, MultiblockCache)} after initializing any data the cache might require.
     */
    public MultiblockCache<T> createCache() {
        return cacheSupplier.get();
    }

    /**
     * Adds a cache as tracked and marks the manager as dirty.
     */
    public void trackCache(UUID id, MultiblockCache<T> cache) {
        caches.put(id, cache);
        markDirty();
    }

    @Nullable
    public MultiblockCache<T> getCache(UUID multiblockID) {
        return caches.get(multiblockID);
    }

    public IStructureValidator<T> createValidator() {
        return validatorSupplier.get();
    }

    public String getName() {
        return name;
    }

    public String getNameLower() {
        return nameLower;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isCompatible(BlockEntity tile) {
        if (tile instanceof IMultiblock<?> multiblock) {
            return multiblock.getManager() == this;
        }
        return false;
    }

    public static void reset() {
        for (MultiblockManager<?> manager : managers) {
            manager.caches.clear();
            manager.dataHandler = null;
        }
    }

    /**
     * Replaces and invalidates all the caches with the given ids with a new cache with the given id.
     */
    public void replaceCaches(Set<UUID> staleIds, UUID id, MultiblockCache<T> cache) {
        for (UUID staleId : staleIds) {
            caches.remove(staleId);
        }
        trackCache(id, cache);
    }

    public void handleDirtyMultiblock(T multiblock) {
        //Validate the multiblock is actually dirty and needs saving
        if (multiblock.isDirty()) {
            MultiblockCache<T> cache = getCache(multiblock.inventoryID);
            //Validate the multiblock's cache exists as if it doesn't we want to ignore it
            // in theory this method should only be called if the multiblock is valid and formed
            // but in case something goes wrong, don't let it
            if (cache != null) {
                cache.sync(multiblock);
                //If the multiblock is dirty mark the manager's data handler as dirty to ensure that we save
                markDirty();
                // next we can reset the dirty state of the multiblock
                multiblock.resetDirty();
            }
        }
    }

    public void markTicked(T multiblock) {
        multiblocksTicked.add(multiblock);
    }

    /**
     * Grabs a unique inventory ID for a multiblock.
     *
     * @return unique inventory ID
     */
    public UUID getUniqueInventoryID() {
        return UUID.randomUUID();
    }

    private void markDirty() {
        if (dataHandler != null) {
            dataHandler.setDirty();
        }
    }

    /**
     * Note: This should only be called from the server side
     */
    public static void createOrLoadAll() {
        for (MultiblockManager<?> manager : managers) {
            manager.createOrLoad();
        }
    }

    /**
     * Note: This should only be called from the server side
     */
    private void createOrLoad() {
        if (dataHandler == null) {
            //Always associate the world with the overworld as we base it on a manager wide state
            dataHandler = MekanismSavedData.createSavedData(MultiblockCacheDataHandler::new, getNameLower());
        }
    }

    /**
     * Bit of a hack, really the multiblock system needs to not have a 'cache' middle-man.
     * <p></p>
     * Causes any multiblocks that became dirty after the master ticked to have their contents synced and thus saved (if one occurs after this tick)
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void endOfTickEvent(ServerTickEvent.Post event) {
        for (MultiblockManager<?> manager : managers) {
            manager.endOfTick();
        }
    }

    /**
     * syncs any multiblocks if they're dirty
     */
    private void endOfTick() {
        T item;
        while ((item = multiblocksTicked.poll()) != null) {
            handleDirtyMultiblock(item);
        }
    }

    private class MultiblockCacheDataHandler extends MekanismSavedData {

        @Override
        public void load(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
            if (nbt.contains(SerializationConstants.CACHE, Tag.TAG_LIST)) {
                ListTag cachesNbt = nbt.getList(SerializationConstants.CACHE, Tag.TAG_COMPOUND);
                for (int i = 0; i < cachesNbt.size(); i++) {
                    CompoundTag cacheTags = cachesNbt.getCompound(i);
                    if (cacheTags.hasUUID(SerializationConstants.INVENTORY_ID)) {
                        UUID id = cacheTags.getUUID(SerializationConstants.INVENTORY_ID);
                        MultiblockCache<T> cachedData = cacheSupplier.get();
                        cachedData.load(provider, cacheTags);
                        caches.put(id, cachedData);
                    }
                }
            }
        }

        @NotNull
        @Override
        public CompoundTag save(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
            ListTag cachesNbt = new ListTag(caches.size());
            for (Map.Entry<UUID, MultiblockCache<T>> entry : caches.entrySet()) {
                CompoundTag cacheTags = new CompoundTag();
                //Note: We can just store the inventory id in the same compound tag as the rest of the cache data
                // as none of the caches save anything to this tag
                cacheTags.putUUID(SerializationConstants.INVENTORY_ID, entry.getKey());
                entry.getValue().save(provider, cacheTags);
                cachesNbt.add(cacheTags);
            }
            nbt.put(SerializationConstants.CACHE, cachesNbt);
            return nbt;
        }
    }
}